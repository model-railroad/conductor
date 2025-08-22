// noinspection DuplicatedCode

import {type ReactElement, useEffect, useRef, useState} from "react";
import {Button, Table} from "react-bootstrap";
import {DateTime} from "luxon";
import {getFromSimpleCache, storeInSimpleCache} from "./SimpleCache.ts";
import {
    type DazzEntryDict,
    type DazzJsonData,
    type DazzRoutePayload,
    fetchDazzData,
    LIVE_JSON_URL
} from "./DazzData.ts";

const SERVER_TZ = "America/Los_Angeles"; // PST or PDT
const REFRESH_KEY = "refresh-live"
const REFRESH_DATA_MINUTES = import.meta.env.DEV ? 1 : 10;
const WARN_RECENT_MINUTES = 10;
const WARN_OLD_MINUTES = 30;
const ROUTE_OLD_DAYS = 7;
const TOGGLES_MAP : Map<string, string> = new Map([
    ["pa", "toggle/passenger"],
    ["fr", "toggle/passenger"],
    ["bl", "toggle/branchline"],
    ["tl", "toggle/branchline"],
]);

// -- Interface for display in Wazz

interface WazzLiveToggle {
    label: string;
    ts: DateTime;
    st: boolean;
    warn?: boolean;
}

interface WazzLiveRoute {
    label: string;
    err: boolean;
    act?: number;
    finished: boolean;
    recovery: boolean;
    old: boolean;
    color?: string;
    sts: DateTime;
    ets?: DateTime;
}

interface WazzLiveData {
    refresh?: DateTime;
    toggles: WazzLiveToggle[];
    routes: WazzLiveRoute[];
}


function LiveViewer(): ReactElement {
    const [loading, setLoading] = useState(true);
    const [status, setStatus] = useState("Loading...");
    const [liveData, setLiveData] = useState<WazzLiveData>({ toggles: [], routes: [] });
    const [isTabVisible, setIsTabVisible] = useState<boolean>(document.visibilityState === "visible");
    const intervalRef = useRef<number | null>(null);

    useEffect(() => {
        fetchData().then(() => {
            // Setup refresh & monitor visibility
            document.addEventListener("visibilitychange", handleVisibilityChange);
            startRefreshTimer();
        });

        // Cleanup when component unmounts
        return () => {
            document.removeEventListener("visibilitychange", handleVisibilityChange);
            stopRefreshTimer();
        };
    }, []);

    function handleVisibilityChange() {
        const newVisibility = document.visibilityState === "visible";
        setIsTabVisible(newVisibility);
        console.log(`@@ Visibility changed to ${newVisibility}`);

        // Start-stop refresh based on tab visibility
        if (newVisibility) {
            startRefreshTimer();
        } else {
            stopRefreshTimer();
        }
    }

    function startRefreshTimer() {
        console.log(`@@ startRefreshTimer. Tab Visible: ${isTabVisible}`);
        if (intervalRef.current !== null) {
            clearInterval(intervalRef.current);
        }

        if (!isTabVisible) return;

        // If we reopen this tab after the elapsed refresh time, force a data fetch.
        const lastRefresh : DateTime|undefined = getFromSimpleCache(REFRESH_KEY);
        if (lastRefresh === undefined || lastRefresh.diffNow("minutes").minutes < -REFRESH_DATA_MINUTES) {
            fetchData();
        }

        const intervalMs = REFRESH_DATA_MINUTES * 60 * 1000;
        intervalRef.current = window.setInterval(fetchData, intervalMs);
    }

    function stopRefreshTimer() {
        console.log("@@ stopRefreshTimer");
        if (intervalRef.current !== null) {
            clearInterval(intervalRef.current);
            intervalRef.current = null;
        }
    }

    async function fetchData() {
        try {
            const refresh = DateTime.now();
            console.log(`@@ fetchData ${refresh}`);

            const jsonData = await fetchDazzData(LIVE_JSON_URL);
            const wazz = transformData(jsonData);

            wazz.refresh = refresh;
            storeInSimpleCache(REFRESH_KEY, wazz.refresh);

            setLiveData(wazz);
            setStatus("");
            setLoading(false);

        } catch (err) {
            console.error(err);
            setStatus(stringifyError(err));
            setLoading(false);
        }
    }

    function stringifyError(error: unknown) {
        if (error instanceof Error) {
            return error.message;
        } else {
            return String(error);
        }
    }

    function transformData(dazzLive: DazzJsonData ): WazzLiveData {
        const result: WazzLiveData = {
            toggles: [],
            routes: [],
        }

        function _addToggles(key: string, entries: DazzEntryDict) {
            for (const [isoTS, entry] of Object.entries(entries)) {
                const dt = DateTime.fromISO(isoTS);

                const t : WazzLiveToggle = {
                    label: key.replaceAll("/", " "),
                    ts: dt,
                    st: entry.st ?? false,
                    warn: undefined, // dt.diffNow("minutes").minutes <= -WARNING_MINUTES
                }
                result.toggles.push(t);
            }
        }

        function _addRoutes(key: string, entries: DazzEntryDict, togglesOn: Map<string, boolean>) {
            for (const [isoTS, entry] of Object.entries(entries)) {
                if (entry.d == null) {
                    continue;
                }
                const label = key.replace("route/", "").replaceAll("/", " ");
                const sdt = DateTime.fromISO(isoTS);
                const payload = JSON.parse(entry.d) as DazzRoutePayload
                const th = payload.th.toLowerCase()
                const finish = payload.run.toLowerCase() === "ended"
                const edt: DateTime | undefined = finish && payload.ets != null
                    ? DateTime.fromISO(payload.ets) : undefined;
                const isEnabled = togglesOn.get(TOGGLES_MAP.get(th) ?? "") ?? false;
                const isFirst = !result.routes.some(value => value.label === label)
                let color = "";
                const deltaStart = sdt.diffNow("minutes").minutes;
                if (isFirst && isEnabled) {
                    if (deltaStart >= -WARN_RECENT_MINUTES) {
                        color = "green";
                    } else if (deltaStart <= -WARN_OLD_MINUTES) {
                        color = "red";
                    } else {
                        color = "yellow";
                    }
                }

                // console.log(`Route ${key}: th ${th} / first: ${isFirst} / enabled: ${isEnabled} / color ${deltaStart} min, ${color}`);

                const r : WazzLiveRoute = {
                    label: label,
                    sts: sdt,
                    err: !entry.st,
                    finished: finish,
                    act: finish ? payload.act : undefined,
                    ets: edt,
                    old: sdt.diffNow("days").days <= -ROUTE_OLD_DAYS,
                    color: color,
                    recovery: key.includes("Recovery"),
                }

                result.routes.push(r);
            }
        }

        const keys = Object.keys(dazzLive).sort();
        const togglesOn = new Map<string, boolean>();

        for (const key of keys) {
            const entries = dazzLive[key];
            if (key.startsWith("toggle/")) {
                _addToggles(key, entries.entries);
                if (!togglesOn.has(key)) {
                    const v = Object.values(entries.entries);
                    togglesOn.set(key, v.at(0)?.st ?? false);
                }
            } else if (key.startsWith("computer/")) {
                _addToggles(key, entries.entries);
            }
        }

        // Add all routes only after we processed all toggles.
        // Routes are sorted in decreasing start timestamp order.
        for (const key of keys) {
            const entries = dazzLive[key];
            if (key.startsWith("route/")) {
                _addRoutes(key, entries.entries, togglesOn);
            }
        }
        result.routes.sort((a, b) =>
            b.sts.toMillis() - a.sts.toMillis() )

        return result;
    }

    function generateStatusLine() {
        return <div className="wazz-status-text"> {status} </div>;
    }

    function formatDate(dateTime: DateTime) {
        const serverDt = dateTime.setZone(SERVER_TZ);
        const dateString2 = serverDt.toLocaleString(DateTime.DATETIME_FULL_WITH_SECONDS);
        const relativeToNow = serverDt.toRelative();

        return (
            <>
            <span className="wazz-date" title={dateTime.toISO( {
                format: "extended",
                suppressMilliseconds: true
            }) ?? ""}>
                {dateString2}
            </span>
                { ' ' }
            <span className="wazz-rel-date">
                ({relativeToNow})
            </span>
            </>
        )
    }

    function formatDay(dateTime: DateTime) {
        const serverDt = dateTime.setZone(SERVER_TZ);
        const dateString2 = serverDt.toLocaleString(DateTime.DATE_SHORT);

        return (
            <span className="wazz-date" title={dateTime.toISO( {
                format: "extended",
                suppressMilliseconds: true
            }) ?? ""}>
                {dateString2}
            </span>
        )
    }

    function formatTime(dateTime: DateTime, timeStart?: DateTime) {
        const serverDt = dateTime.setZone(SERVER_TZ);
        const dateString2 = serverDt.toLocaleString(DateTime.TIME_WITH_SHORT_OFFSET);
        const relativeToNow = timeStart == null
            ? serverDt.toRelative()
            : `run for ${Math.round(dateTime.diff(timeStart, "minutes").minutes)} minutes` ;

        return (
            <>
            <span className="wazz-date" title={dateTime.toISO( {
                format: "extended",
                suppressMilliseconds: true
            }) ?? ""}>
                {dateString2}
            </span>
                { ' ' }
                <span className="wazz-rel-date">
                ({relativeToNow})
            </span>
            </>
        )
    }

    function formatStateButton(state: boolean|undefined, onLabel: string, offLabel: string) {
        if (state === undefined) {
            return <></>;
        }
        if (state) {
            return <Button className="wazz-btn" variant="success" size="sm">{onLabel}</Button>
        } else {
            return <Button className="wazz-btn" variant="danger" size="sm">{offLabel}</Button>
        }
    }

    function generateSystemStatus(data: WazzLiveData) {
        if (loading) {
            return <span className="wazz-loading">...</span>;
        }

        let lastLabel = "";

        return (
            <Table striped bordered variant="light" className="wazz-table wazz-system-table">
                <thead>
                <tr>
                    <th colSpan={2}>System Status</th>
                    <th>Last Updated</th>
                </tr>
                </thead>
                <tbody>
                { data.toggles.map((entry, index) => {
                    let label = entry.label;
                    if (label === lastLabel) {
                        label = "";
                    } else {
                        lastLabel = label;
                    }
                    return (
                    <tr key={`st-${index}`} className={`wazz-status-warning-${entry.warn ?? "undef"}`}>
                        <td className="wazz-route-name"> { label } </td>
                        <td> { formatStateButton(entry.st, "ON", "OFF") } </td>
                        <td> { formatDate(entry.ts) } </td>
                    </tr>
                ) } ) }
                </tbody>
            </Table>
        )
    }

    function generateRouteStatus(data: WazzLiveData) {
        if (loading) {
            return <span className="wazz-loading">...</span>;
        }

        let lastLabel = "";

        return (
            <Table striped bordered variant="light" className="wazz-table wazz-routes-table">
                <thead>
                <tr>
                    <th>Route</th>
                    <th colSpan={2}>Start</th>
                    <th>End</th>
                    <th>#</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                { data.routes.map((entry, index) => {
                    let label = entry.label;
                    if (label === lastLabel) {
                        label = "";
                    } else {
                        lastLabel = label;
                    }
                    return (
                    <tr key={`rt-${index}`} className={
                        `wazz-route-old-${entry.old} wazz-route-recovery-${entry.recovery} wazz-status-highlight-${entry.color ?? "undef"}`}>
                        <td className="wazz-route-name"> { label } </td>
                        <td> { formatDay(entry.sts) } </td>
                        <td> { formatTime(entry.sts) } </td>
                        <td> { entry.ets == null ? "-" : formatTime(entry.ets, entry.sts) } </td>
                        <td> { entry.act == null ? "-" : entry.act } </td>
                        <td> { formatStateButton(!entry.err, "OK", "ERR") } </td>
                    </tr>
                ) } ) }
                </tbody>
            </Table>
        )
    }

    function generateRefreshStatus(data: WazzLiveData) {
        const dt = data.refresh;

        if (dt === undefined) {
            return ( <div className="wazz-last-update-text"> -- </div> )
        }

        const serverDt =
            dt.zoneName === SERVER_TZ
            ? dt
            : dt.setZone(SERVER_TZ);

        return (
            <div className="wazz-last-update-text">
                Data Updated
                { ' ' }
                { serverDt.toLocaleString(DateTime.DATETIME_FULL_WITH_SECONDS) }
                { serverDt === dt ? ' ' : ` // ${dt.toLocaleString(DateTime.DATETIME_FULL_WITH_SECONDS)}` }
            </div>
        );
    }

    return (
    <>
        { generateStatusLine() }
        { generateRefreshStatus(liveData) }
        { generateSystemStatus(liveData) }
        { generateRouteStatus(liveData) }
    </>
    )
}

export default LiveViewer

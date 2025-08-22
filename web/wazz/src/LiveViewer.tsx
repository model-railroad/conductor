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
const WARNING_MINUTES = 30;
const ROUTE_OLD_DAYS = 7;

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
    warn?: boolean;
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

        // function _appendTsValue(key1: string, key2?: string, running?: boolean): boolean {
        //     try {
        //         // 2021-08-27 adjust computer names: "computer" alone is legacy for "computer-consist".
        //         let label = key1;
        //         if (label === "computer") {
        //             label = "computer-consist";
        //         }
        //         label = label.replaceAll("-", " ");
        //         const indent = (label === "depart");
        //
        //         let data = rtac[key1] as never;
        //         if (key2 !== undefined) {
        //             data = data[key2];
        //         }
        //         if (data === undefined) {
        //             console.log(`@@ transformData: ${key1}/${key2} not present. Skipping.`);
        //             return false;
        //         }
        //         const ts = data as Timestamp;
        //         const dt = DateTime.fromISO(ts.ts);
        //
        //         const state: boolean | undefined = ("value" in data)
        //             ? String(data["value"]).toLowerCase() === "on"
        //             : undefined;
        //
        //         const warning: boolean | undefined = running
        //             ? dt.diffNow("minutes").minutes <= -WARNING_MINUTES
        //             : undefined;
        //
        //         const entry: WazzStatusEntry = {
        //             ts: dt,
        //             indent: indent,
        //             state: state,
        //             warning: warning,
        //             label: label,
        //             sublabel: key2,
        //         };
        //
        //         result.status.push(entry);
        //
        //         return state ?? false;
        //     } catch (err) {
        //         console.error(err);
        //         setStatus(stringifyError(err));
        //         return false;
        //     }
        // }
        //
        // _appendTsValue("computer");
        // _appendTsValue("computer-vision");
        // const cond = _appendTsValue("conductor");
        //
        // let tog = _appendTsValue("toggle", "passenger");
        // _appendTsValue("depart", "passenger", cond && tog);
        // _appendTsValue("depart", "freight", cond && tog);
        // tog = _appendTsValue("toggle", "branchline");
        // _appendTsValue("depart", "branchline", cond && tog);
        // _appendTsValue("depart", "trolley", cond && tog);
        //
        //
        // function _appendRoute(data: RouteJsonData) {
        //     let nodes = "";
        //     for(const n of data.nodes) {
        //         if (nodes !== "") nodes += " > ";
        //         nodes += n.n + " = " + (n.ms / 1000).toFixed(1);
        //     }
        //
        //     const entry : WazzRouteEntry = {
        //         ts: data.ts,
        //         name: `${data.name} [${data.th}]`,
        //         error: data.err,
        //         runs: data.act,
        //         nodes: nodes,
        //         old: data.ts.diffNow("days").days <= -ROUTE_OLD_DAYS,
        //         recovery: data.name.includes("Recovery"),
        //     };
        //
        //     result.routes.push(entry);
        // }
        //
        // const rt_dict = rtac["route_stats"] as RouteStatsDict;
        // const rt_list = Object.values(rt_dict).map((e) => {
        //     const rt_data = JSON.parse(e.value) as RouteJsonData;
        //     rt_data.ts = DateTime.fromISO(e.ts);
        //     return rt_data;
        // });
        // rt_list.sort( (a, b) => b.ts.valueOf() - a.ts.valueOf() );
        // rt_list.forEach(entry => _appendRoute(entry));

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

        function _addRoutes(key: string, entries: DazzEntryDict) {
            for (const [isoTS, entry] of Object.entries(entries)) {
                if (entry.d == null) {
                    continue;
                }
                const sdt = DateTime.fromISO(isoTS);
                const payload = JSON.parse(entry.d) as DazzRoutePayload
                const finish = payload.run.toLowerCase() === "ended"

                const edt : DateTime | undefined = finish && payload.ets != null
                    ? DateTime.fromISO(payload.ets) : undefined;

                const r : WazzLiveRoute = {
                    label: key.replace("route/", "").replaceAll("/", " "),
                    sts: sdt,
                    err: !entry.st,
                    finished: finish,
                    act: finish ? payload.act : undefined,
                    ets: edt,
                    old: sdt.diffNow("days").days <= -ROUTE_OLD_DAYS,
                    warn: !finish && sdt.diffNow("minutes").minutes <= -WARNING_MINUTES ? true : undefined,
                    recovery: key.includes("Recovery"),
                }

                result.routes.push(r);
            }

        }

        const keys = Object.keys(dazzLive).sort();

        for (const key of keys) {
            const entries = dazzLive[key];
            if (key.startsWith("toggle/") || key.startsWith("computer/")) {
                _addToggles(key, entries.entries);
            } else if (key.startsWith("route/")) {
                _addRoutes(key, entries.entries);
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
                        `wazz-route-old-${entry.old} wazz-route-recovery-${entry.recovery} wazz-status-warning-${entry.warn ?? "undef"}`}>
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

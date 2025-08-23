// noinspection DuplicatedCode

import {type ReactElement, useEffect, useRef, useState} from "react";
import {Button, Table} from "react-bootstrap";
import {DateTime} from "luxon";
import {getFromSimpleCache, storeInSimpleCache} from "./SimpleCache.ts";
import {
    type DazzEntryDict,
    type DazzJsonData,
    type DazzRouteNode, type DazzRoutePayload,
    fetchDazzData,
    HISTORY_JSON_URL
} from "./DazzData.ts";

const SERVER_TZ = "America/Los_Angeles"; // PST or PDT
const REFRESH_KEY = "refresh-history"
const REFRESH_DATA_MINUTES = import.meta.env.DEV ? 1 : 10;
// const WARNING_MINUTES = 30;
// const ROUTE_OLD_DAYS = 7;

// -- Interface for display in Wazz

interface WazzHistRoute {
    label: string;
    err: boolean;
    act: number;
    finished: boolean;
    recovery: boolean;
    sts: DateTime;
    ets: DateTime;
    nodes: DazzRouteNode[];
}

type WazzHistRouteMap = Map<string, {
        label: string,
        list: WazzHistRoute[],
}>;

interface WazzHistData {
    refresh?: DateTime;
    routes: WazzHistRouteMap;
}


function HistoryViewer(): ReactElement {
    const [loading, setLoading] = useState(true);
    const [status, setStatus] = useState("Loading...");
    const [histData, setHistData] = useState<WazzHistData>({ routes: new Map() });
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

            const jsonData = await fetchDazzData(HISTORY_JSON_URL);
            const wazz = transformData(jsonData);

            wazz.refresh = refresh;
            storeInSimpleCache(REFRESH_KEY, wazz.refresh);

            setHistData(wazz);
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

    function transformData(dazzLive: DazzJsonData ): WazzHistData {
        const result: WazzHistData = {
            routes: new Map(),
        }

        function _addRoutes(key: string, entries: DazzEntryDict) {
            for (const [isoTS, entry] of Object.entries(entries)) {
                if (entry.d == null) {
                    console.log(`@@ ERROR missing entry.d: skip route ${JSON.stringify(entry)}`)
                    continue;
                }
                const label = key.replace("route/", "").replaceAll("/", " ");
                const payload = JSON.parse(entry.d) as DazzRoutePayload
                const finish = payload.run.toLowerCase() === "ended"
                // console.log(`@@ FOUND ${label}, ${finish}, ${JSON.stringify(payload)}`)
                if (payload.nodes == null || payload.nodes.length === 0 || !finish) {
                    console.log(`@@ ERROR missing nodes/finish: skip route ${JSON.stringify(payload)}`)
                    continue;
                }

                if (!result.routes.has(key)) {
                    result.routes.set(key, {
                        label: label,
                        list: [],
                    });
                }
                const routeList = result.routes.get(key);
                if (routeList == null) {
                    console.log(`@@ ERROR routeList in result.routes ${JSON.stringify(Object.fromEntries(result.routes))}`)
                    continue; // one of these "should never happen" safety checks
                }

                const r : WazzHistRoute = {
                    label: label,
                    sts: DateTime.fromISO(isoTS),
                    err: !entry.st,
                    finished: finish,
                    act: payload.act ?? -1,
                    ets: DateTime.fromISO(payload.ets ?? DateTime.now().toISO()),
                    recovery: key.includes("Recovery"),
                    nodes: payload.nodes ?? [],
                }

                routeList.list.push(r);
            }
        }

        const keys = Object.keys(dazzLive).sort();

        for (const key of keys) {
            const entries = dazzLive[key];
            if (key.startsWith("route/")) {
                _addRoutes(key, entries.entries);
            }
        }

        return result;
    }

    function generateStatusLine() {
        return <div className="wazz-status-text"> {status} </div>;
    }

    /*
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
    */

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

    function nodeColor(node: DazzRouteNode): string {
        const sec = node.ms / 1000;

        const mis = node.mis ?? 0;

        if (sec < mis) {
            return "red";
        }

        if (node.mas != null) {

            const delta = node.mas - mis;
            if (sec >= mis + delta * 0.90) {
                return "red";
            }
            if (sec >= mis + delta * 0.75) {
                return "yellow";
            }
            if (sec >= mis + delta * 0.50) {
                return "green";
            }
        }

        if (sec < mis * 1.50) {
            return "yellow";
        }

        return "";
    }

    function nodeText(node: DazzRouteNode): string {
        const mis = node.mis ?? 0;
        const mas = node.mas ?? 0;
        const mi2 = Math.round(mis * 1.5);
        const ma2 = Math.round(mis + (mas - mis) * 0.75);
        return `Min: ${mis} seconds\nIdeal: ${mi2}-${ma2} seconds\nMax: ${mas} seconds`;
    }

    function generateNode(key: string, index: number, indxN: number, node?: DazzRouteNode) {
        return node === undefined
            ? <td key={`n-${key}-${index}-${indxN}`}>
                -
            </td>
            : <td key={`n-${key}-${index}-${indxN}`} className={`wazz-node-${nodeColor(node)}`}>
                                <span title={nodeText(node)}>
                                { node.ms > 10000 ? Math.round(node.ms/1000) : (node.ms/1000).toFixed(1) } s
                                </span>
            </td>
    }

    function generateRouteTable(key: string, label: string, list: WazzHistRoute[]) {
        const nodes: string[] = []
        for (const entry of list) {
            for (const node of entry.nodes) {
                const n = node.n;
                if (!nodes.includes(n)) {
                    nodes.push(n);
                }
            }
        }
        const numNodes = nodes.length;

        return (
            <Table key={key} striped bordered variant="light" className="wazz-table wazz-routes-table">
                <thead>
                <tr>
                    <th colSpan={5+numNodes}>Route {label}</th>
                </tr>
                <tr>
                    <th colSpan={2}>Start</th>
                    <th>End</th>
                    <th>#</th>
                    <th>Status</th>
                    { nodes.map((node, index) => (
                        <th key={`th-n-${key}-${index}`}>{node}</th>
                    ) ) }
                </tr>
                </thead>
                <tbody>
                { list.map((entry, index) => (
                    <tr key={`rt-${key}-${index}`}>
                        <td> { formatDay(entry.sts) } </td>
                        <td> { formatTime(entry.sts) } </td>
                        <td> { formatTime(entry.ets, entry.sts) } </td>
                        <td> { entry.act } </td>
                        <td> { formatStateButton(!entry.err, "OK", "ERR") } </td>
                        { nodes.map((nodeName, indxN) =>
                            generateNode(
                                key,
                                index,
                                indxN,
                                entry.nodes.find(nd => nd.n === nodeName))
                        )}
                    </tr>
                    ) ) }
                </tbody>
            </Table>
        )
    }

    function generateRoutesTables(data: WazzHistData) {
        if (loading) {
            return <span className="wazz-loading">...</span>;
        }

        return (
            <>
            { Array.from(data.routes.entries()).map(([key, value]) =>
                generateRouteTable(key, value.label, value.list)
            )

            }
            </>
        );
    }

    function generateRefreshStatus(data: WazzHistData) {
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
        { generateRefreshStatus(histData) }
        { generateRoutesTables(histData) }
    </>
    )
}

export default HistoryViewer

// noinspection DuplicatedCode

import {type MouseEvent, type ReactElement, useEffect, useRef, useState} from "react";
import {Button, Table} from "react-bootstrap";
import {DateTime} from "luxon";
import {getFromSimpleCache, storeInSimpleCache} from "./SimpleCache.ts";
import {
    type DazzEntryDict,
    type DazzJsonData,
    type DazzRouteNode, type DazzRoutePayload,
    fetchDazzData,
    PERF_JSON_URL
} from "./DazzData.ts";
import {GTagRefreshEvent} from "./GTagHelpers.ts";

const SERVER_TZ = "America/Los_Angeles"; // PST or PDT
const REFRESH_KEY = "refresh-perf"
const REFRESH_DATA_MINUTES = import.meta.env.DEV ? 1 : 10;

// -- Interface for display in Wazz

interface WazzPerfRoute {
    err: boolean;
    act: number;
    finished: boolean;
    recovery: boolean;
    sts: DateTime;
    ets: DateTime;
    nodes: DazzRouteNode[];
}

interface WazzPerfNodeName {
    n: string;
    index: number;
    full: string;
}

interface WazzPerfRouteTable {
    label: string;
    anchor: string;
    update: DateTime;
    nodeNames: WazzPerfNodeName[];
    list: WazzPerfRoute[];
}

type WazzPerfRoutesMap = Map<string, WazzPerfRouteTable>;

interface WazzPerfData {
    refresh?: DateTime;
    routes: WazzPerfRoutesMap;
}


function PerfViewer(): ReactElement {
    const [loading, setLoading] = useState(true);
    const [status, setStatus] = useState("Loading...");
    const [histData, sethistData] = useState<WazzPerfData>({ routes: new Map() });
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

            const jsonData = await fetchDazzData(PERF_JSON_URL);
            const wazz = transformData(jsonData);

            wazz.refresh = refresh;
            storeInSimpleCache(REFRESH_KEY, wazz.refresh);
            GTagRefreshEvent(REFRESH_KEY);

            sethistData(wazz);
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

    function transformData(dazzLive: DazzJsonData ): WazzPerfData {
        const result: WazzPerfData = {
            routes: new Map(),
        }

        function _routeLabel(routeKey: string, payload: DazzRoutePayload): string {
            let label = routeKey.replace("route/", "").replaceAll("/", " ");
            if (payload.name.endsWith(payload.th)) {
                label = label.replace(`${payload.th} ${payload.th}`, payload.th);
            }
            return label;
        }

        function _collectNodeNames(routeTable: WazzPerfRouteTable, nodes: DazzRouteNode[]) {
            const count = new Map<string, number>();

            const pattern = /\.[0-9]$/;
            for (const node of nodes) {
                node.n = node.n.replace(pattern, "");
                count.set(node.n, 1 + (count.get(node.n) ?? 0));
                node.wIndex = count.get(node.n) ?? 0;
                node.wFull = `${node.n}.${node.wIndex}`;
            }

            // Merge all the node names together.
            // If a node name is already in the route's node list, keep its index and add afterward.
            // All node names are initially prepended untill we find an already known name, then we add afterward
            // that known position.
            let currentPos = 0;
            for (const node of nodes) {
                const nodeName: WazzPerfNodeName = {
                    n: node.n,
                    index: node.wIndex ?? 0,
                    full: `${node.n}.${node.wIndex}`,
                };
                const idx = routeTable.nodeNames.findIndex(nname => nname.full == nodeName.full);
                if (idx >= 0) {
                    currentPos = idx + 1;
                    continue;
                }
                routeTable.nodeNames.splice(currentPos, 0, nodeName);
                currentPos += 1;
            }
        }

        function _addRoutes(key: string, entries: DazzEntryDict) {
            for (const [isoTS, entry] of Object.entries(entries)) {
                if (entry.d == null) {
                    console.log(`@@ ERROR missing entry.d: skip route ${JSON.stringify(entry)}`)
                    continue;
                }
                const anchor = key.replaceAll(/[^a-zA-Z0-9]/g, "-");
                const payload = JSON.parse(entry.d) as DazzRoutePayload
                const label = _routeLabel(key, payload);
                const finish = payload.run.toLowerCase() === "ended"
                // console.log(`@@ FOUND ${label}, ${finish}, ${JSON.stringify(payload)}`)
                if (payload.nodes == null || payload.nodes.length === 0 || !finish) {
                    console.log(`@@ ERROR missing nodes/finish: skip route ${JSON.stringify(payload)}`)
                    continue;
                }

                if (!result.routes.has(key)) {
                    result.routes.set(key, {
                        label: label,
                        anchor: anchor,
                        update: DateTime.fromISO(isoTS),
                        nodeNames: [],
                        list: [],
                    } as WazzPerfRouteTable);
                }
                const routeTable = result.routes.get(key);
                if (routeTable == null) {
                    console.log(`@@ ERROR routeList in result.routes ${JSON.stringify(Object.fromEntries(result.routes))}`)
                    continue; // one of these "should never happen" safety checks
                }

                _collectNodeNames(routeTable, payload.nodes);

                const r : WazzPerfRoute = {
                    sts: DateTime.fromISO(isoTS),
                    err: !entry.st,
                    finished: finish,
                    act: payload.act ?? -1,
                    ets: DateTime.fromISO(payload.ets ?? DateTime.now().toISO()),
                    recovery: key.includes("Recovery"),
                    nodes: payload.nodes ?? [],
                }

                routeTable.list.push(r);
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

    function formatDay(dateTime: DateTime, routeSummary: string) {
        const serverDt = dateTime.setZone(SERVER_TZ);
        const dateString2 = serverDt.toLocaleString(DateTime.DATE_SHORT);

        const alt1 = dateTime.toISO( {
            format: "extended",
            suppressMilliseconds: true
        }) ?? "";

        return (
            <span className="wazz-date" title={`${alt1}\n${routeSummary}`}>
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

    function formatRelTime(dateTime: DateTime) {
        const serverDt = dateTime.setZone(SERVER_TZ);
        const relativeToNow = serverDt.toRelative()

        return (
            <>
            <span className="wazz-date" title={dateTime.toISO( {
                format: "extended",
                suppressMilliseconds: true
            }) ?? ""}>
                {relativeToNow}
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

    function generateNode(key: string, nname: WazzPerfNodeName, node?: DazzRouteNode) {
        return node === undefined
            ? <td key={`n-${key}-${nname.full}`}>
                -
            </td>
            : <td key={`n-${key}-${nname.full}`} className={`wazz-node-${nodeColor(node)}`}>
                                <span title={nodeText(node)}>
                                { node.ms > 10000 ? Math.round(node.ms/1000) : (node.ms/1000).toFixed(1) } s
                                </span>
            </td>
    }

    function routeSummary(entry: WazzPerfRoute): string {
        return entry.nodes.map(node => {
            const ms = node.ms;
            const s = ms > 10000 ? Math.round(ms/1000) : (ms/1000).toFixed(1);
            return `${node.n} (${s} s)`;
        }).join(" > ");
    }

    function generateRouteTable(key: string, table: WazzPerfRouteTable) {
        const nodeNames = table.nodeNames;
        const numNodes = nodeNames.length;

        return (
            <Table key={`rt-${key}-table`} striped bordered variant="light" className="wazz-table wazz-routes-table">
                <thead>
                <tr className="wazz-table-head">
                    <th colSpan={5+numNodes}><a id={table.anchor}></a>Route {table.label}</th>
                </tr>
                <tr className="wazz-table-head" key={`rt-${key}-th-`}>
                    <th colSpan={2}>Start</th>
                    <th>End</th>
                    <th>#</th>
                    <th>Status</th>
                    { nodeNames.map((nname) => (
                        <th key={`th-n-${key}-${nname.full}`}>{nname.n} <span className="wazz-node-index">{nname.index}</span></th>
                    ) ) }
                </tr>
                </thead>
                {/*<tbody>*/}
                { table.list.map((entry) => {
                    return (
                        <>
                            <tbody>
                            <tr key={`rt-${key}-${entry.sts}-${entry.ets ?? ""}-${entry.err}`}>
                                <td> { formatDay(entry.sts, routeSummary(entry)) } </td>
                                <td> { formatTime(entry.sts) } </td>
                                <td> { formatTime(entry.ets, entry.sts) } </td>
                                <td> { entry.act } </td>
                                <td> { formatStateButton(!entry.err, "OK", "ERR") } </td>
                                { nodeNames.map((nname) =>
                                    generateNode(
                                        key,
                                        nname,
                                        entry.nodes.find(nd => nd.wFull === nname.full))
                                )}
                            </tr>
                            </tbody>
                        </>
                    );
                } ) }
                {/*</tbody>*/}
            </Table>
        )
    }

    function generateRoutesTables(data: WazzPerfData) {
        if (loading) {
            return <span className="wazz-loading">...</span>;
        }

        return (
            <>
                { Array.from(data.routes.entries()).map(([key, value]) =>
                    generateRouteTable(key, value)
                ) }
            </>
        );
    }

    function scrollTo(evt: MouseEvent<HTMLAnchorElement>, anchor: string) {
        evt.preventDefault();
        const element = document.getElementById(anchor);
        if (element) {
            element.scrollIntoView({behavior: "smooth", block: "start"});
        }
    }

    function generateRouteLinks(data: WazzPerfData) {
        if (loading) {
            return <span className="wazz-loading">...</span>;
        }

        return (
            <Table striped bordered variant="light" className="wazz-table wazz-routes-links">
                <thead>
                <tr>
                    { Array.from(data.routes.entries()).map(([, table]) => (
                        <th key={`link-${table.anchor}`}>
                            <a href={`#/perf/#${table.anchor}`}
                               onClick={ (evt) =>
                                   scrollTo(evt, table.anchor) }>{table.label}</a>
                            <br/>
                            {formatRelTime(table.update)}
                        </th>
                    ) ) }
                </tr>
                </thead>
            </Table>
        );
    }

    function onButtonForceRefresh(evt: MouseEvent<HTMLButtonElement>) {
        evt.preventDefault();
        stopRefreshTimer()
        fetchData()
        startRefreshTimer()
    }

    function generateRefreshStatus(data: WazzPerfData) {
        const dt = data.refresh;

        if (dt === undefined) {
            return ( <div className="wazz-last-update-text"> -- </div> )
        }

        const serverDt =
            dt.zoneName === SERVER_TZ
            ? dt
            : dt.setZone(SERVER_TZ);

        return (
            <div className="d-flex">
                <div className="wazz-last-update-text flex-grow-1 align-content-center">
                    Data Updated
                    { ' ' }
                    { serverDt.toLocaleString(DateTime.DATETIME_FULL_WITH_SECONDS) }
                    { serverDt === dt ? ' ' : ` // ${dt.toLocaleString(DateTime.DATETIME_FULL_WITH_SECONDS)}` }
                </div>
                <div className="justify-content-end wazz-refresh">
                    <Button variant="link"
                            onClick={ (evt) => onButtonForceRefresh(evt) }>Refresh</Button>
                </div>
            </div>
        );
    }

    return (
    <>
        { generateStatusLine() }
        { generateRefreshStatus(histData) }
        { generateRouteLinks(histData) }
        { generateRoutesTables(histData) }
    </>
    )
}

export default PerfViewer

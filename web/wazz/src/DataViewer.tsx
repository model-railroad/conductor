import {type ReactElement, useEffect, useState} from "react";
import {Button, Table} from "react-bootstrap";
import {DateTime} from "luxon";

const RTAC_JSON_URL = "https://www.alfray.com/cgi/rtac_status.py"
const FAKE_JSON_URL = "fake_data.json"
const JSON_URL = FAKE_JSON_URL


// -- Interface from the JSON payload

interface Timestamp {
    "ts": string;       // ISO date
}

interface TimestampValue extends Timestamp{
    "value": string;
}

interface DepartDict {
    [key: string]: Timestamp;
}

interface ToggleDict {
    [key: string]: TimestampValue;
}

interface RouteStatsDict {
    [key: string]: TimestampValue;
}

interface RtacJsonData {
    [key: string]: TimestampValue|DepartDict|ToggleDict|RouteStatsDict;
}

// -- Interface for display in Wazz

interface WazzStatusEntry {
    indent: boolean;
    state?: boolean;    // (on, off) state, or just a timestamp
    label: string;
    sublabel?: string;
    ts: DateTime;
}

interface WazzRouteEntry {
    ts: DateTime;
    name: string;
    runs: number;
    error: boolean;
    nodes: string;
}

interface WazzData {
    status: WazzStatusEntry[];
    routes: WazzRouteEntry[];
}


function DataViewer(): ReactElement {
    const [loading, setLoading] = useState(true);
    const [status, setStatus] = useState("Loading...");
    const [wazzData, setWazzData] = useState<WazzData>({routes: [], status: []});

    useEffect(() => {
        fetchData();
    }, []);

    async function fetchData() {
        try {
            const jsonData = await fetch(JSON_URL);
            if (!jsonData.ok) {
                throw new Error(`Error reading data: ${jsonData.status}`);
            }
            const data = await jsonData.json() as RtacJsonData;
            const wazz = transformData(data);
            setWazzData(wazz);
            setStatus("");
            setLoading(false);

        } catch (err) {
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

    function transformData(rtac: RtacJsonData): WazzData {
        const result: WazzData = {
            routes: [],
            status: [],
        }

        function _appendTsValue(key1: string, key2?: string) {
            // 2021-08-27 adjust computer names: "computer" alone is legacy for "computer-consist".
            let label = key1;
            if (label === "computer") {
                label = "computer-consist";
            }
            label = label.replaceAll("-" , " ");
            const indent = (label === "depart");

            let data = rtac[key1] as never;
            if (key2 !== undefined) {
                data = data[key2];
            }
            const ts = data as Timestamp;
            const dt = DateTime.fromISO(ts.ts);

            const state: boolean|undefined = ("value" in data)
                ? String(data["value"]).toLowerCase() === "on"
                : undefined;

            const entry: WazzStatusEntry = {
                indent: indent,
                state: state,
                label: label,
                sublabel: key2,
                ts: dt,
            };

            result.status.push(entry);
        }

        _appendTsValue("computer");
        _appendTsValue("computer-vision");
        _appendTsValue("conductor");

        _appendTsValue("toggle", "passenger");
        _appendTsValue("depart", "passenger");
        _appendTsValue("depart", "freight");
        _appendTsValue("toggle", "branchline");
        _appendTsValue("depart", "branchline");
        _appendTsValue("depart", "trolley");

        return result;
    }

    function generateStatusLine() {
        return <div className="pcs-status"> {status} </div>;
    }

    function formatDate(dateTime: DateTime) {
        const pacificDt = dateTime.setZone("America/Los_Angeles"); // PST or PDT
        const dateString2 = pacificDt.toLocaleString(DateTime.DATETIME_FULL);
        const relativeToNow = pacificDt.toRelative();

        return (
            <span title={dateTime.toISO( {
                format: "extended",
                suppressMilliseconds: true
            })}>
                {dateString2} ({relativeToNow})
            </span>
        )
    }

    function formatButtonOnOff(state?: boolean) {
        if (state === undefined) {
            return <></>;
        }
        if (state) {
            return <Button className="wazz-btn" variant="success" size="sm">ON</Button>
        } else {
            return <Button className="wazz-btn" variant="danger" size="sm">OFF</Button>
        }
    }

    function generateSystemStatus() {
        if (loading) {
            return <span className="wazz-loading">...</span>;
        }

        return (
            <Table striped bordered hover className="wazz-table wazz-system-table">
                <thead>
                <tr>
                    <th colSpan={3}>System Status</th>
                    <th>Last Updated</th>
                </tr>
                </thead>
                <tbody>
                { wazzData.status.map((entry, index) => (
                    <tr key={index}>
                        <td className={`wazz-status-text wazz-indent-${entry.indent}`}> { entry.label } </td>
                        <td className="wazz-status-text"> { entry.sublabel } </td>
                        <td> { formatButtonOnOff(entry.state) } </td>
                        <td> { formatDate(entry.ts) } </td>
                    </tr>
                )) }
                </tbody>
            </Table>
        )
    }

    function generateRouteStatus() {
        if (loading) {
            return <span className="wazz-loading">...</span>;
        }

        return (
            <Table striped bordered hover className="wazz-table wazz-routes-table">
                <thead>
                <tr>
                    <th>Finished At</th>
                    <th>Route</th>
                    <th>#</th>
                    <th>Error</th>
                    <th>Nodes</th>
                </tr>
                </thead>
                <tbody>
                { wazzData.routes.map((entry, index) => (
                    <tr key={index}>
                        <td> { entry.ts.toLocaleString() } </td>
                        <td> { entry.name } </td>
                        <td> { entry.runs } </td>
                        <td> { entry.error } </td>
                        <td> { entry.nodes } </td>
                    </tr>
                )) }
                </tbody>
            </Table>
        )
    }

    return (
    <>
        { generateStatusLine() }
        { generateSystemStatus() }
        { generateRouteStatus() }
    </>
    )
}

export default DataViewer

import {type ReactElement, useEffect, useState} from "react";
import {Table} from "react-bootstrap";

const RTAC_JSON_URL = "https://www.alfray.com/cgi/rtac_status.py"
const FAKE_JSON_URL = "fake_data.json"
const JSON_URL = FAKE_JSON_URL


// -- Interface from the JSON payload

interface TimestampValue {
    "ts": string;       // ISO date
    "value": string;
}

interface Timestamp {
    "ts": string;       // ISO date
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
    "conductor"?: TimestampValue; // on or off values
    "computer"?: TimestampValue;
    "computer-vision"?: TimestampValue;
    depart?: DepartDict;
    toggle?: ToggleDict;
    route_stats?: RouteStatsDict;
}

// -- Interface for display in Wazz

interface WazzStatusEntry {
    state?: boolean;    // (on, off) state, or just a timestamp
    label: string;
    sublabel?: string;
    ts: Date;
}

interface WazzRouteEntry {
    ts: Date;
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

        return result;
    }

    function generateStatusLine() {
        return <div className="pcs-status"> {status} </div>;
    }

    function generateSystemStatus() {
        if (loading) {
            return <span className="pcs-loading">...</span>;
        }

        return (
            <Table striped bordered hover>
                <thead>
                <tr>
                    <th>System Status</th>
                    <th>Last Updated</th>
                </tr>
                </thead>
                <tbody>
                { wazzData.status.map((entry, index) => (
                    <tr key={index}>
                        <td> { entry.label } </td>
                        <td> { entry.sublabel } </td>
                        <td> { entry.state } </td>
                        <td> { entry.ts.toLocaleString() } </td>
                    </tr>
                )) }
                </tbody>
            </Table>
        )
    }

    function generateRouteStatus() {
        if (loading) {
            return <span className="pcs-loading">...</span>;
        }

        return (
            <Table striped bordered hover>
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

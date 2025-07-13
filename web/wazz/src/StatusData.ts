import {DateTime} from "luxon";
import {fetchJsonFromSimpleCache} from "./SimpleCache.ts";

const WAZZ_JSON_URL = "https://www.alfray.com/cgi/wazz_status.py";
const FAKE_JSON_URL = "mock_data.json";
const JSON_URL = import.meta.env.DEV ? FAKE_JSON_URL : WAZZ_JSON_URL;

// -- Interface from the JSON payload

export interface Timestamp {
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

export interface RouteJsonData {
    ts: DateTime;
    name: string;
    th: string;
    act: number;
    err: boolean;
    nodes: {
        n: string;
        ms: number;
    }[];
}

export interface RouteStatsDict {
    [key: string]: TimestampValue;
}

export interface RtacJsonData {
    [key: string]: TimestampValue|DepartDict|ToggleDict|RouteStatsDict;
}

export async function fetchStatusData() : Promise<RtacJsonData> {
    const data = await fetchJsonFromSimpleCache(JSON_URL, JSON_URL) as RtacJsonData;

    if (import.meta.env.DEV) {
        // Mock data for dev purposes
        const minutes = DateTime.now().minute;
        const cv = data["computer-vision"] as TimestampValue;
        if (cv !== undefined) {
            cv.value = (minutes % 2 === 0 ? "on" : "off");
        }
    }

    return data;
}


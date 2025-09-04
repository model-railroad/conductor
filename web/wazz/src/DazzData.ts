const PROD_URL_BASE = "https://r.alfray.com/cgi/dazz/";
const DEV_URL_BASE = "mock_";
export const LIVE_JSON_URL = `${import.meta.env.DEV ? DEV_URL_BASE : PROD_URL_BASE}live.json`;
export const PERF_JSON_URL = `${import.meta.env.DEV ? DEV_URL_BASE : PROD_URL_BASE}perf.json`;
export const STATZ_TXT_URL = `${import.meta.env.DEV ? DEV_URL_BASE : PROD_URL_BASE}statz`;

// -- Interface from the JSON payload



export interface DazzEntry {
    key: string;
    ts: string;
    st?: boolean;
    d?: string;
}

export interface DazzEntryDict {
    [timestamp: string]: DazzEntry;
}

export interface DazzJsonData {
    [key: string]: {
        entries: DazzEntryDict
    };
}

export interface DazzRouteNode {
    n:  string;
    ms: number;
    mis?: number;
    mas?: number;
}

export interface DazzRoutePayload {
    name: string;
    th: string;
    act: number;
    err: boolean;
    run: string;
    sts: string;
    ets?: string;
    nodes?: DazzRouteNode[];
}

export async function fetchDazzData(url: string) : Promise<DazzJsonData> {
    const jsonData = await fetch(url);
    if (!jsonData.ok) {
        throw new Error(`Error reading data: ${jsonData.status}`);
    }
    return await jsonData.json();
}

export async function fetchDazzText(url: string) : Promise<string> {
    const txtData = await fetch(url);
    if (!txtData.ok) {
        throw new Error(`Error reading data: ${txtData.status}`);
    }
    return await txtData.text();
}

const PROD_URL_BASE = "https://www.alfray.com/cgi/dazz/";
const DEV_URL_BASE = "mock_";
export const LIVE_JSON_URL = `${import.meta.env.DEV ? DEV_URL_BASE : PROD_URL_BASE}live.json`;
export const HISTORY_JSON_URL = `${import.meta.env.DEV ? DEV_URL_BASE : PROD_URL_BASE}history.json`;

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

export interface DazzRoutePayload {
    name: string;
    th: string;
    act: number;
    err: boolean;
    run: string;
    sts: string;
    ets?: string;
    nodes?: {
        n:  string;
        ms: number;
        mis?: number;
        mas?: number;
    }[];
}

export async function fetchDazzData(url: string) : Promise<DazzJsonData> {
    const jsonData = await fetch(url);
    if (!jsonData.ok) {
        throw new Error(`Error reading data: ${jsonData.status}`);
    }
    return await jsonData.json();
}

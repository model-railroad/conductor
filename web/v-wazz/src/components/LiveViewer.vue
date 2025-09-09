<script setup lang="ts">
import {BButton, BTableSimple} from "bootstrap-vue-next";
import {DateTime} from "luxon";
import {onMounted, ref} from "vue";
import {
  type DazzEntryDict,
  type DazzJsonData,
  type DazzRoutePayload,
  fetchDazzData,
  LIVE_JSON_URL, DAZZ_SERVER_TZ
} from "../DazzData.ts";
import {storeInSimpleCache} from "../SimpleCache.ts";
import WFormattedDate from "./WFormattedDate.vue";
import WStateButton from "./WStateButton.vue";
import WFormattedTime from "./WFormattedTime.vue";
import WFormattedDay from "./WFormattedDay.vue";

const REFRESH_KEY = "refresh-live"
const REFRESH_DATA_MINUTES = import.meta.env.DEV ? 1 : 10;
const WARN_RECENT_MINUTES = 10;
const WARN_OLD_MINUTES = 30;
const WARN_SKIP_DAYS = 2;
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
  // The refresh DateTime in the local TimeZone.
  refreshLC?: DateTime;
  // The refresh DateTime in the fixed Server TimeZone.
  refreshTZ?: DateTime;
  toggles: WazzLiveToggle[];
  routes: WazzLiveRoute[];
}

// --

const loading = ref(true);
const status = ref("Loading...");
const liveData = ref<WazzLiveData>({ toggles:[], routes: [] });
const isTabVisible = ref<boolean>(document.visibilityState === "visible");
const intervalRef = ref<number|null>(null);
const displayAll = ref(true);

async function doOnMounted() {
  await fetchData();
}

async function fetchData() {
  try {
    const refresh = DateTime.now();
    console.log(`@@ fetchData ${refresh}`);

    const jsonData = await fetchDazzData(LIVE_JSON_URL);
    const wazz = transformData(jsonData);

    wazz.refreshLC = refresh;
    wazz.refreshTZ = refresh.setZone(DAZZ_SERVER_TZ);
    storeInSimpleCache(REFRESH_KEY, wazz.refreshTZ);
    // GTagRefreshEvent(REFRESH_KEY);

    liveData.value = wazz;
    status.value = "";
    loading.value = false;

  } catch (err) {
    console.error(err);
    status.value = stringifyError(err);
    loading.value = false;
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
        warn: undefined,
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
      const minutes = -1 * sdt.diffNow("minutes").minutes;
      const days = -1 * sdt.diffNow("days").days;
      if (isFirst && isEnabled && days <= WARN_SKIP_DAYS) {
        if (minutes <= WARN_RECENT_MINUTES) {
          color = "green";
        } else if (minutes >= WARN_OLD_MINUTES) {
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
        old: days >= ROUTE_OLD_DAYS,
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
      if (displayAll || key === "computer/consist") {
        _addToggles(key, entries.entries);
      }
    } else if (key.startsWith("conductor/")) {
      if (displayAll) {
        _addToggles(key, entries.entries);
      }
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

function onButtonForceRefresh(evt: MouseEvent) {
  evt.preventDefault();
  // stopRefreshTimer()
  fetchData()
  // startRefreshTimer()
}

function onButtonAll(evt: MouseEvent) {
  evt.preventDefault();
  // if (displayAll) {
  //   navigate(`?`);
  // } else {
  //   const searchParams = new URLSearchParams({
  //     all: "true"
  //   }).toString();
  //   navigate(`?${searchParams}`);
  // }
  displayAll.value = !displayAll.value;
}

onMounted(doOnMounted);

</script>

<template>
  <!-- status -->
  <div class="wazz-status-text"> {{status}} </div>

  <!-- refresh -->
  <div v-if="liveData.refreshTZ === undefined" class="wazz-last-update-text"> -- </div>
  <div v-else class="d-flex">
    <div class="wazz-last-update-text flex-grow-1 align-content-center">
      Data Updated
      {{ liveData.refreshTZ.toLocaleString(DateTime.DATETIME_FULL_WITH_SECONDS) }}
      {{
        liveData.refreshLC?.zoneName === DAZZ_SERVER_TZ ? " " : ` // ${liveData.refreshLC?.toLocaleString(DateTime.DATETIME_FULL_WITH_SECONDS)}`
      }}
    </div>
    <div class="justify-content-end wazz-refresh">
      <BButton variant="link" v-on:click="onButtonForceRefresh">Refresh</BButton>
      <BButton variant="link" v-on:click="onButtonAll">
        {{ displayAll ? "Less" : "All" }}
      </BButton>
    </div>
  </div>

  <!-- system status -->
  <span v-if="loading" class="wazz-loading">...</span>
  <BTableSimple v-else striped bordered variant="light" class="wazz-table wazz-system-table">
    <thead>
    <tr>
      <th colSpan="2">System Status</th>
      <th>Last Updated</th>
    </tr>
    </thead>
    <tbody>
      <tr v-for="(entry, index) in liveData.toggles"
          :key="`${entry.ts}-${entry.label}`"
          :class="`wazz-status-warning-${entry.warn ?? 'undef'}`"
          >
        <td class="wazz-route-name"> {{
            index < 1 || entry.label !== liveData.toggles[index-1].label ? entry.label : ""
          }} </td>
        <td> <WStateButton :state="entry.st" on-label="ON" off-label="OFF" /> </td>
        <td> <WFormattedDate :date-time="entry.ts"/> </td>
      </tr>
    </tbody>
  </BTableSimple>

  <!-- route status -->
  <span v-if="loading" class="wazz-loading">...</span>
  <BTableSimple v-else striped bordered variant="light" class="wazz-table wazz-routes-table">
    <thead>
    <tr>
      <th>Route</th>
      <th colSpan="2">Start</th>
      <th>End</th>
      <th>#</th>
      <th>Status</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="(entry, index) in liveData.routes"
        :key="`${entry.sts}-${entry.label}`"
        :class="`wazz-route-old-${entry.old} wazz-route-recovery-${entry.recovery} wazz-status-highlight-${entry.color ?? 'undef'}`">
      <td class="wazz-route-name"> {{
          index < 1 || entry.label !== liveData.toggles[index-1].label ? entry.label : ""
        }} </td>
      <td> <WFormattedDay  :date-time="entry.sts" /> </td>
      <td> <WFormattedTime :date-time="entry.sts" /> </td>
      <td> <WFormattedTime :date-time="entry.ets" :time-start="entry.sts" /> </td>
      <td> {{ entry.act ?? "-" }} </td>
      <td> <WStateButton :state="!entry.err" on-label="OK" off-label="ERR" /> </td>
    </tr>
    </tbody>
  </BTableSimple>

</template>

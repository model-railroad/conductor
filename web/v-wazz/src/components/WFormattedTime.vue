<script setup lang="ts">

import {DateTime} from "luxon";
import {DAZZ_SERVER_TZ} from "../DazzData.ts";

const props = defineProps<{
  dateTime?: DateTime,
  timeStart?: DateTime,
}>();

const serverDt = props.dateTime?.setZone(DAZZ_SERVER_TZ);
const dateString2 = serverDt?.toLocaleString(DateTime.TIME_WITH_SHORT_OFFSET);
const relativeToNow = props.timeStart == null
    ? serverDt?.toRelative()
    : `run for ${Math.round(props.dateTime?.diff(props.timeStart, "minutes")?.minutes ?? 0)} minutes` ;
const dateTitle = props.dateTime?.toISO( {
  format: "extended",
  suppressMilliseconds: true
}) ?? "";

</script>

<template>
  <span v-if="dateTime == undefined" class="wazz-date">-</span>
  <span v-if="dateTime" class="wazz-date" :title="dateTitle">{{dateString2}}</span>
  {{}}
  <span v-if="dateTime" class="wazz-rel-date">({{relativeToNow}})</span>
</template>

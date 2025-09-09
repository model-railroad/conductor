<script setup lang="ts">

import {DateTime} from "luxon";
import {DAZZ_SERVER_TZ} from "../DazzData.ts";

const props = defineProps<{
  dateTime: DateTime
}>();

const serverDt = props.dateTime.setZone(DAZZ_SERVER_TZ);
const dateString2 = serverDt.toLocaleString(DateTime.DATETIME_FULL_WITH_SECONDS);
const relativeToNow = serverDt.toRelative();
const dateTitle = props.dateTime.toISO( {
  format: "extended",
  suppressMilliseconds: true
}) ?? "";

</script>

<template>
  <span class="wazz-date" :title="dateTitle">{{dateString2}}</span>
  <span class="wazz-rel-date">({{relativeToNow}})</span>
</template>

<script setup lang="ts">

import {DateTime} from "luxon";
import {DAZZ_SERVER_TZ} from "../DazzData.ts";

const props = defineProps<{
  dateTime: DateTime
}>();

const serverDt = props.dateTime.setZone(DAZZ_SERVER_TZ);
const dateString2 = serverDt.toLocaleString(DateTime.DATE_SHORT);
const dateTitle = props.dateTime.toISO( {
  format: "extended",
  suppressMilliseconds: true
}) ?? "";

</script>

<template>
  <span class="wazz-date" :title="dateTitle">{{dateString2}}</span>
</template>

<script setup lang="ts">

import {BButton} from "bootstrap-vue-next";

defineProps<{
  state: boolean|undefined,
  onLabel: string,
  offLabel: string,
}>();

</script>

<template>
  <BButton v-if="state === true"  class="wazz-btn" variant="success" size="sm">{{onLabel}}</BButton>
  <BButton v-if="state === false" class="wazz-btn" variant="danger"  size="sm">{{offLabel}}</BButton>
</template>

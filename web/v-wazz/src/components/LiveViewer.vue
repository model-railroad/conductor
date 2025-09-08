<script setup lang="ts">
import {BTableSimple} from "bootstrap-vue-next";
</script>

<template>
  <div class="wazz-status-text"> {status} </div>

  <div class="d-flex">
    {Refresh{
  </div>

  <BTableSimple striped bordered variant="light" class="wazz-table wazz-system-table">
    <thead>
    <tr>
      <th colSpan="2">System Status</th>
      <th>Last Updated</th>
    </tr>
    </thead>
    <tbody>
    <tr>
      <td>Value 1</td>
      <td>Value 2</td>
      <td>Value 3</td>
    </tr>
    </tbody>
  </BTableSimple>
</template>

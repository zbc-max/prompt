<template>
  <div>
    <el-button v-permission="'${ctx.table.businessName}:add'" type="primary">新增</el-button>
    <el-table :data="rows">
      <#list ctx.table.columns as c>
      <el-table-column prop="${c.javaField}" label="${c.comment}" />
      </#list>
    </el-table>
  </div>
</template>
<script setup lang="ts">
import { ref } from 'vue';
const rows = ref([]);
</script>

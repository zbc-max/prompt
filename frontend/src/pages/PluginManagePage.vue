<template><div><h3>插件管理</h3><el-button @click="load">刷新</el-button><el-table :data="rows"><el-table-column prop="pluginId" label="ID"/><el-table-column prop="name" label="名称"/><el-table-column prop="version" label="版本"/></el-table></div></template>
<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { apiGet } from '@/platform/api/api-client';
const rows = ref<any[]>([]);
async function load(){ rows.value = await apiGet<any[]>('/api/plugins'); }
onMounted(load);
</script>

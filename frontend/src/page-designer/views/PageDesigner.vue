<template>
  <div class="designer-layout">
    <aside class="left"><ComponentPalette /></aside>
    <main class="center">
      <div class="toolbar">
        <el-button type="success" @click="preview = !preview">{{ preview ? '返回设计' : '页面预览' }}</el-button>
        <el-button type="primary" @click="publish">页面发布</el-button>
        <el-tag v-if="schema.published" type="success">已发布 {{ schema.publishTime }}</el-tag>
      </div>
      <DesignerCanvas v-if="!preview" @select-node="onSelect" />
      <PageRenderer v-else />
    </main>
    <aside class="right">
      <PropertyPanel :selected-id="selectedId" />
      <EventPanel :selected-id="selectedId" style="margin-top:12px;" />
    </aside>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import DesignerCanvas from '@/page-designer/components/DesignerCanvas.vue';
import ComponentPalette from '@/page-designer/panels/ComponentPalette.vue';
import PropertyPanel from '@/page-designer/panels/PropertyPanel.vue';
import EventPanel from '@/page-designer/panels/EventPanel.vue';
import PageRenderer from '@/page-designer/components/PageRenderer.vue';
import { pageSchemaState, publishPage } from '@/page-designer/core/schema-store';

const preview = ref(false);
const selectedId = ref<string | null>(null);
const schema = pageSchemaState;

function onSelect(id: string): void { selectedId.value = id; }
function publish(): void { publishPage(); }
</script>

<style scoped>
.designer-layout { display: grid; grid-template-columns: 240px 1fr 320px; gap: 12px; height: 100vh; background: #f0f2f5; }
.left,.right { background: #fff; padding: 12px; overflow: auto; }
.center { background: #fff; padding: 12px; overflow: auto; }
.toolbar { margin-bottom: 12px; display: flex; gap: 8px; }
</style>

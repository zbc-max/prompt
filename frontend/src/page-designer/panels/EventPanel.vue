<template>
  <div>
    <h3>事件面板</h3>
    <template v-if="node">
      <el-button size="small" type="primary" @click="addEvent">新增事件</el-button>
      <div v-for="(evt, idx) in node.events" :key="idx" style="margin-top:8px;border:1px solid #ebeef5;padding:8px;">
        <el-input v-model="evt.name" placeholder="事件名称" />
        <el-select v-model="evt.trigger" style="width:100%;margin-top:8px;">
          <el-option label="click" value="click" />
          <el-option label="change" value="change" />
          <el-option label="mounted" value="mounted" />
        </el-select>
        <el-input v-model="evt.script" type="textarea" :rows="3" placeholder="事件脚本" style="margin-top:8px;" />
      </div>
    </template>
    <div v-else>请选择组件</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { pageSchemaState } from '@/page-designer/core/schema-store';

const props = defineProps<{ selectedId: string | null }>();
const node = computed(() => pageSchemaState.root.children?.find((n) => n.id === props.selectedId) || null);

function addEvent(): void {
  if (!node.value) return;
  node.value.events.push({ name: '新事件', trigger: 'click', script: 'console.log("event")' });
}
</script>

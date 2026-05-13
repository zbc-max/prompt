<template>
  <div>
    <h3>属性面板</h3>
    <template v-if="node">
      <el-form label-width="90px">
        <el-form-item label="组件名称"><el-input v-model="node.name" /></el-form-item>
        <el-form-item label="X"><el-input-number v-model="node.layout.x" :min="1" :max="24" /></el-form-item>
        <el-form-item label="Y"><el-input-number v-model="node.layout.y" :min="1" :max="99" /></el-form-item>
        <el-form-item label="宽度"><el-input-number v-model="node.layout.w" :min="1" :max="24" /></el-form-item>
        <el-form-item label="高度"><el-input-number v-model="node.layout.h" :min="1" :max="20" /></el-form-item>
      </el-form>
    </template>
    <div v-else>请选择组件</div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { pageSchemaState } from '@/page-designer/core/schema-store';

const props = defineProps<{ selectedId: string | null }>();

const node = computed(() => pageSchemaState.root.children?.find((n) => n.id === props.selectedId) || null);
</script>

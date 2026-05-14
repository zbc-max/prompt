<template>
  <div>
    <h3>组件库</h3>
    <div v-for="item in items" :key="item.type" class="palette-item" draggable="true" @dragstart="onDrag($event, item.type)">
      {{ item.label }}
    </div>
  </div>
</template>

<script setup lang="ts">
const items = [
  { type: 'text', label: '文本' },
  { type: 'button', label: '按钮' },
  { type: 'input', label: '输入框' },
  { type: 'table', label: '表格' }
];

function onDrag(e: DragEvent, type: string): void {
  e.dataTransfer?.setData('component-type', type);
}
</script>

<style scoped>
.palette-item { padding: 8px; margin: 8px 0; border: 1px solid #dcdfe6; cursor: grab; background: #fff; }
</style>

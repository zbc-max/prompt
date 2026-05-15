<template>
  <div class="canvas" @dragover.prevent @drop="onDrop">
    <div class="grid" :style="gridStyle">
      <div
        v-for="item in schema.root.children || []"
        :key="item.id"
        class="node"
        :style="nodeStyle(item)"
        @click.stop="select(item.id)"
      >
        <component :is="resolveDesignerComponent(item.type)" v-bind="item.props" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { addNode, pageSchemaState } from '@/page-designer/core/schema-store';
import { resolveDesignerComponent } from '@/page-designer/registry/component-registry';
import type { DesignerNode, DesignerComponentType } from '@/page-designer/types/schema';

const schema = pageSchemaState;
const emit = defineEmits<{ (e: 'select-node', id: string): void }>();

const gridStyle = computed(() => ({
  display: 'grid',
  gridTemplateColumns: `repeat(${schema.grid.cols}, 1fr)`,
  gridAutoRows: `${schema.grid.rowHeight}px`,
  gap: `${schema.grid.gap}px`
}));

function onDrop(e: DragEvent): void {
  const type = (e.dataTransfer?.getData('component-type') || 'text') as DesignerComponentType;
  const node: DesignerNode = {
    id: `node_${Date.now()}`,
    type,
    name: type,
    props: defaultProps(type),
    layout: { x: 1, y: 1, w: 6, h: 2 },
    events: []
  };
  addNode(node);
}

function defaultProps(type: DesignerComponentType): Record<string, unknown> {
  if (type === 'text') return { text: '文本内容' };
  if (type === 'button') return { label: '操作按钮' };
  if (type === 'input') return { placeholder: '请输入' };
  return {};
}

function nodeStyle(item: DesignerNode): Record<string, string> {
  return {
    gridColumn: `${item.layout.x} / span ${item.layout.w}`,
    gridRow: `${item.layout.y} / span ${item.layout.h}`,
    border: '1px dashed #409eff',
    padding: '8px',
    background: '#fff'
  };
}

function select(id: string): void {
  emit('select-node', id);
}
</script>

<style scoped>
.canvas { background: #f5f7fa; padding: 12px; min-height: 600px; }
</style>

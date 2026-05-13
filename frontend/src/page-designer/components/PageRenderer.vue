<template>
  <div>
    <h3>页面预览</h3>
    <div :style="gridStyle">
      <div v-for="node in schema.root.children || []" :key="node.id" :style="nodeStyle(node)">
        <component :is="resolveDesignerComponent(node.type)" v-bind="node.props" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { pageSchemaState } from '@/page-designer/core/schema-store';
import { resolveDesignerComponent } from '@/page-designer/registry/component-registry';
import type { DesignerNode } from '@/page-designer/types/schema';

const schema = pageSchemaState;
const gridStyle = computed(() => ({ display: 'grid', gridTemplateColumns: `repeat(${schema.grid.cols}, 1fr)`, gridAutoRows: `${schema.grid.rowHeight}px`, gap: `${schema.grid.gap}px` }));

function nodeStyle(item: DesignerNode): Record<string, string> {
  return { gridColumn: `${item.layout.x} / span ${item.layout.w}`, gridRow: `${item.layout.y} / span ${item.layout.h}`, padding: '8px' };
}
</script>

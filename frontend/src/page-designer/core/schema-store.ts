import { reactive } from 'vue';
import type { DesignerNode, PageSchema } from '@/page-designer/types/schema';

function createRoot(): DesignerNode {
  return {
    id: 'root',
    type: 'container',
    name: '页面根容器',
    props: {},
    layout: { x: 0, y: 0, w: 24, h: 20 },
    events: [],
    children: []
  };
}

export const pageSchemaState = reactive<PageSchema>({
  pageId: 'page_demo_001',
  pageName: '演示页面',
  version: '1.0.0',
  grid: { cols: 24, rowHeight: 40, gap: 8 },
  root: createRoot(),
  published: false
});

export function addNode(node: DesignerNode): void {
  pageSchemaState.root.children = pageSchemaState.root.children || [];
  pageSchemaState.root.children.push(node);
}

export function updateNode(nodeId: string, updater: (node: DesignerNode) => void): void {
  const node = pageSchemaState.root.children?.find((n) => n.id === nodeId);
  if (node) updater(node);
}

export function publishPage(): void {
  pageSchemaState.published = true;
  pageSchemaState.publishTime = new Date().toISOString();
}

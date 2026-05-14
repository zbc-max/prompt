export type DesignerComponentType = 'container' | 'text' | 'button' | 'input' | 'table';

export interface DesignerEventConfig {
  name: string;
  trigger: 'click' | 'change' | 'mounted';
  script: string;
}

export interface DesignerNode {
  id: string;
  type: DesignerComponentType;
  name: string;
  props: Record<string, unknown>;
  layout: { x: number; y: number; w: number; h: number };
  events: DesignerEventConfig[];
  children?: DesignerNode[];
}

export interface PageSchema {
  pageId: string;
  pageName: string;
  version: string;
  grid: { cols: number; rowHeight: number; gap: number };
  root: DesignerNode;
  published: boolean;
  publishTime?: string;
}

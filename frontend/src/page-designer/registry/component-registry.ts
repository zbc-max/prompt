import { h, type Component } from 'vue';

const registry = new Map<string, Component>();

const TextComp: Component = { props: ['text'], setup: (p) => () => h('div', p.text || '文本') };
const ButtonComp: Component = { props: ['label'], setup: (p) => () => h('button', { class: 'el-button el-button--primary' }, p.label || '按钮') };
const InputComp: Component = { props: ['placeholder'], setup: (p) => () => h('input', { class: 'el-input__inner', placeholder: p.placeholder || '' }) };
const TableComp: Component = { setup: () => () => h('div', { style: 'border:1px solid #dcdfe6;padding:12px' }, '表格组件占位') };

export function registerDesignerDefaults(): void {
  registry.set('text', TextComp);
  registry.set('button', ButtonComp);
  registry.set('input', InputComp);
  registry.set('table', TableComp);
}

export function registerDesignerComponent(type: string, comp: Component): void {
  registry.set(type, comp);
}

export function resolveDesignerComponent(type: string): Component {
  return registry.get(type) ?? TextComp;
}

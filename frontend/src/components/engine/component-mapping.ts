import BaseField from '@/components/fields/BaseField.vue';

const registry = new Map<string, unknown>();

export function registerDefaultComponents(): void {
  ['input','select','radio','checkbox','switch','date','upload','richtext'].forEach((type) => registry.set(type, BaseField));
  registry.set('table', BaseField);
  registry.set('subform', BaseField);
}

export function registerComponent(type: string, component: unknown): void {
  registry.set(type, component);
}

export function resolveComponent(type: string): unknown {
  return registry.get(type) ?? BaseField;
}

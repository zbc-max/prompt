import type { FieldSchema, FormSchema } from '@/types/schema';
import { safeEval } from '@/utils/expression';

export function buildSchemaMap(schema: FormSchema): Record<string, FieldSchema> {
  return schema.fields.reduce<Record<string, FieldSchema>>((acc, f) => {
    acc[f.fieldKey] = f;
    return acc;
  }, {});
}

export function initFormData(schema: FormSchema): Record<string, unknown> {
  return schema.fields.reduce<Record<string, unknown>>((acc, f) => {
    acc[f.fieldKey] = f.defaultValue ?? null;
    return acc;
  }, {});
}

export function isFieldVisible(field: FieldSchema, formData: Record<string, unknown>): boolean {
  if (field.permission && !field.permission.visible) return false;
  if (!field.visibleWhen) return true;
  return Boolean(safeEval(field.visibleWhen, formData));
}

export function isFieldDisabled(field: FieldSchema, formData: Record<string, unknown>): boolean {
  if (field.permission && !field.permission.editable) return true;
  if (!field.disabledWhen) return false;
  return Boolean(safeEval(field.disabledWhen, formData));
}

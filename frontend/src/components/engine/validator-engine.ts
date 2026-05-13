import type { FieldSchema } from '@/types/schema';
import { safeEval } from '@/utils/expression';

export function validateField(field: FieldSchema, value: unknown, formData: Record<string, unknown>): string[] {
  const errors: string[] = [];
  const rules = field.validationRules ?? [];

  if (field.required && (value === undefined || value === null || value === '')) {
    errors.push(`${field.label}不能为空`);
  }

  for (const rule of rules) {
    if (rule.type === 'required' && (value === undefined || value === null || value === '')) {
      errors.push(rule.message);
    }
    if (rule.type === 'regex' && rule.pattern && value != null) {
      if (!new RegExp(rule.pattern).test(String(value))) errors.push(rule.message);
    }
    if (rule.type === 'min' && typeof value === 'number' && rule.min !== undefined && value < rule.min) {
      errors.push(rule.message);
    }
    if (rule.type === 'max' && typeof value === 'number' && rule.max !== undefined && value > rule.max) {
      errors.push(rule.message);
    }
    if (rule.type === 'custom' && rule.expr) {
      const pass = Boolean(safeEval(rule.expr, { ...formData, value }));
      if (!pass) errors.push(rule.message);
    }
  }
  return errors;
}

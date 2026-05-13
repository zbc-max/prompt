import type { FieldSchema } from '@/types/schema';
import { safeEval } from '@/utils/expression';

export function applyFormula(field: FieldSchema, formData: Record<string, unknown>): unknown {
  if (!field.formula?.expr) return formData[field.fieldKey];
  return safeEval(field.formula.expr, formData);
}

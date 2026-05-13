import type { EventRule, FieldSchema } from '@/types/schema';
import { safeEval } from '@/utils/expression';
import { fetchOptions } from '@/services/data-source';

export async function runEvents(
  field: FieldSchema,
  trigger: EventRule['trigger'],
  formData: Record<string, unknown>,
  schemaMap: Record<string, FieldSchema>
): Promise<void> {
  const rules = (field.eventRules ?? []).filter((r) => r.trigger === trigger);
  for (const rule of rules) {
    const pass = rule.when ? Boolean(safeEval(rule.when, formData)) : true;
    if (!pass) continue;

    for (const action of rule.actions) {
      const target = schemaMap[action.target];
      if (!target) continue;
      switch (action.type) {
        case 'setValue':
          formData[target.fieldKey] = action.expr ? safeEval(action.expr, formData) : action.value;
          break;
        case 'setVisible':
          target.permission = { ...(target.permission ?? { visible: true, editable: true }), visible: Boolean(action.value) };
          break;
        case 'setDisabled':
          target.permission = { ...(target.permission ?? { visible: true, editable: true }), editable: !Boolean(action.value) };
          break;
        case 'fetchDataSource':
          if (target.dataSource) {
            target.options = await fetchOptions(target.dataSource, formData);
          }
          break;
      }
    }
  }
}

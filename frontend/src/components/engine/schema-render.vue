<template>
  <el-form label-width="120px">
    <template v-for="field in schema.fields" :key="field.fieldKey">
      <component
        v-if="isFieldVisible(field, formData)"
        :is="resolveComponent(field.componentType)"
        :field="field"
        :model-value="formData[field.fieldKey]"
        :error="errors[field.fieldKey]"
        :disabled="isFieldDisabled(field, formData)"
        @update:model-value="(v: unknown) => updateValue(field.fieldKey, v)"
        @change="() => handleEvent(field, 'change')"
        @blur="() => handleEvent(field, 'blur')"
      />
    </template>
  </el-form>
</template>

<script setup lang="ts">
import { reactive, watchEffect } from 'vue';
import type { FieldSchema, FormSchema } from '@/types/schema';
import { buildSchemaMap, initFormData, isFieldDisabled, isFieldVisible } from './form-parser';
import { resolveComponent } from './component-mapping';
import { runEvents } from './event-engine';
import { validateField } from './validator-engine';
import { applyFormula } from './formula-engine';

const props = defineProps<{ schema: FormSchema }>();

const formData = reactive<Record<string, unknown>>(initFormData(props.schema));
const errors = reactive<Record<string, string>>({});
const schemaMap = buildSchemaMap(props.schema);

function updateValue(key: string, value: unknown): void {
  formData[key] = value;
}

async function handleEvent(field: FieldSchema, trigger: 'change' | 'blur' | 'init'): Promise<void> {
  await runEvents(field, trigger, formData, schemaMap);
  const err = validateField(field, formData[field.fieldKey], formData);
  errors[field.fieldKey] = err[0] || '';
}

watchEffect(() => {
  props.schema.fields.forEach((field) => {
    if (field.formula?.expr) {
      formData[field.fieldKey] = applyFormula(field, formData);
    }
  });
});
</script>

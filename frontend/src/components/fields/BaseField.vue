<template>
  <el-form-item :label="field.label" :prop="field.fieldKey" :error="error">
    <component
      :is="componentName"
      v-model="innerValue"
      v-bind="componentProps"
      :placeholder="field.placeholder"
      :disabled="disabled"
      @change="onChange"
      @blur="onBlur"
    >
      <template v-if="field.componentType === 'select'">
        <el-option v-for="op in field.options || []" :key="String(op.value)" :label="op.label" :value="op.value" />
      </template>
      <template v-if="field.componentType === 'radio'">
        <el-radio v-for="op in field.options || []" :key="String(op.value)" :label="op.value">{{ op.label }}</el-radio>
      </template>
      <template v-if="field.componentType === 'checkbox'">
        <el-checkbox v-for="op in field.options || []" :key="String(op.value)" :label="op.value">{{ op.label }}</el-checkbox>
      </template>
    </component>
  </el-form-item>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import type { FieldSchema } from '@/types/schema';

const props = defineProps<{
  field: FieldSchema;
  modelValue: unknown;
  error?: string;
  disabled?: boolean;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: unknown): void;
  (e: 'change'): void;
  (e: 'blur'): void;
}>();

const innerValue = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v)
});

const componentName = computed(() => {
  switch (props.field.componentType) {
    case 'input': return 'el-input';
    case 'select': return 'el-select';
    case 'radio': return 'el-radio-group';
    case 'checkbox': return 'el-checkbox-group';
    case 'switch': return 'el-switch';
    case 'date': return 'el-date-picker';
    case 'upload': return 'el-upload';
    case 'richtext': return 'el-input';
    default: return 'el-input';
  }
});

const componentProps = computed(() => {
  if (props.field.componentType === 'richtext') {
    return { type: 'textarea', rows: 6, ...(props.field.props || {}) };
  }
  return props.field.props || {};
});

const onChange = () => emit('change');
const onBlur = () => emit('blur');
</script>

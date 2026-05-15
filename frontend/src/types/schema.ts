export type ComponentType =
  | 'input'
  | 'select'
  | 'radio'
  | 'checkbox'
  | 'switch'
  | 'date'
  | 'upload'
  | 'table'
  | 'subform'
  | 'richtext';

export interface OptionItem {
  label: string;
  value: string | number | boolean;
}

export interface DataSourceConfig {
  sourceCode: string;
  url: string;
  method?: 'GET' | 'POST';
  params?: Record<string, unknown>;
  valueField?: string;
  labelField?: string;
}

export interface ValidationRule {
  type: 'required' | 'regex' | 'min' | 'max' | 'custom';
  message: string;
  pattern?: string;
  min?: number;
  max?: number;
  expr?: string;
}

export interface EventAction {
  type: 'setValue' | 'setVisible' | 'setDisabled' | 'fetchDataSource';
  target: string;
  value?: unknown;
  expr?: string;
}

export interface EventRule {
  trigger: 'change' | 'blur' | 'init';
  when?: string;
  actions: EventAction[];
}

export interface FormulaConfig {
  expr: string;
  dependencies: string[];
}

export interface FieldPermission {
  visible: boolean;
  editable: boolean;
}

export interface FieldSchema {
  fieldKey: string;
  label: string;
  componentType: ComponentType;
  required?: boolean;
  defaultValue?: unknown;
  placeholder?: string;
  props?: Record<string, unknown>;
  options?: OptionItem[];
  dataSource?: DataSourceConfig;
  validationRules?: ValidationRule[];
  eventRules?: EventRule[];
  formula?: FormulaConfig;
  visibleWhen?: string;
  disabledWhen?: string;
  permission?: FieldPermission;
  columns?: Array<{ field: string; label: string; width?: number }>;
  children?: FieldSchema[];
}

export interface FormSchema {
  schemaCode: string;
  schemaName: string;
  fields: FieldSchema[];
}

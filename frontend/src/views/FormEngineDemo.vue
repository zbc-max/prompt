<template>
  <div style="padding: 20px">
    <h2>动态表单引擎 Demo</h2>
    <SchemaRender :schema="schema" />
  </div>
</template>

<script setup lang="ts">
import SchemaRender from '@/components/engine/schema-render.vue';
import type { FormSchema } from '@/types/schema';

const schema: FormSchema = {
  schemaCode: 'demo_order_form',
  schemaName: '订单表单',
  fields: [
    { fieldKey: 'customerName', label: '客户姓名', componentType: 'input', required: true, placeholder: '请输入客户姓名' },
    {
      fieldKey: 'region',
      label: '区域',
      componentType: 'select',
      dataSource: { sourceCode: 'region_api', url: '/mock/regions' },
      options: [
        { label: '华东', value: 'east' },
        { label: '华南', value: 'south' }
      ]
    },
    {
      fieldKey: 'amount',
      label: '金额',
      componentType: 'input',
      validationRules: [{ type: 'regex', pattern: '^\\d+(\\.\\d{1,2})?$', message: '金额格式不正确' }]
    },
    {
      fieldKey: 'tax',
      label: '税费',
      componentType: 'input',
      formula: { expr: 'Number(amount || 0) * 0.06', dependencies: ['amount'] }
    },
    {
      fieldKey: 'needInvoice',
      label: '需要发票',
      componentType: 'switch',
      defaultValue: false,
      eventRules: [{ trigger: 'change', actions: [{ type: 'setVisible', target: 'invoiceTitle', value: true }] }]
    },
    { fieldKey: 'invoiceTitle', label: '发票抬头', componentType: 'input', visibleWhen: 'needInvoice === true' },
    { fieldKey: 'detailTable', label: '明细表', componentType: 'table', columns: [{ field: 'sku', label: 'SKU' }, { field: 'qty', label: '数量' }] },
    { fieldKey: 'subItems', label: '子表单', componentType: 'subform', children: [{ fieldKey: 'subName', label: '子项名称', componentType: 'input' }] },
    { fieldKey: 'remark', label: '备注', componentType: 'richtext', props: { rows: 4 } }
  ]
};
</script>

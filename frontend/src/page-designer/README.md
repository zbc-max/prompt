# 可视化页面设计器

## 1. 页面设计架构
- 左侧组件库 + 中间画布 + 右侧属性/事件面板 + 预览/发布工具栏

## 2. 拖拽实现
- HTML5 Drag & Drop：组件库 `dragstart` 写入 `component-type`，画布 `drop` 创建节点

## 3. Schema 设计
- `types/schema.ts` 定义 PageSchema、DesignerNode、事件、栅格布局

## 4. 页面渲染器
- `components/PageRenderer.vue` 按页面 Schema 栅格渲染

## 5. 属性编辑器
- `panels/PropertyPanel.vue` 编辑节点名称和布局参数

## 6. 组件注册系统
- `registry/component-registry.ts` 支持默认组件与扩展注册

## 7. Vue3 完整实现
- `views/PageDesigner.vue` 汇总设计态、预览态、发布态

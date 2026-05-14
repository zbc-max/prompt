# 动态表单引擎（Vue3 + TS）

## 1. 前端目录结构

```text
frontend/src
├── components
│   ├── engine
│   │   ├── component-mapping.ts
│   │   ├── event-engine.ts
│   │   ├── form-parser.ts
│   │   ├── formula-engine.ts
│   │   ├── schema-render.vue
│   │   └── validator-engine.ts
│   └── fields
│       └── BaseField.vue
├── services
│   └── data-source.ts
├── types
│   └── schema.ts
├── utils
│   └── expression.ts
├── views
│   └── FormEngineDemo.vue
└── main.ts
```

## 2-10 实现说明

- schema 定义：`types/schema.ts`
- 渲染器：`components/engine/schema-render.vue`
- 动态组件注册：`components/engine/component-mapping.ts`
- 校验引擎：`components/engine/validator-engine.ts`
- 事件引擎：`components/engine/event-engine.ts`
- API 数据源：`services/data-source.ts`
- 动态联动：`event-engine.ts + form-parser.ts`
- 公式引擎：`components/engine/formula-engine.ts`
- 完整代码：见本目录全部文件

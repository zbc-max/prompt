#!/usr/bin/env bash
set -e

BASE=${1:-http://localhost:8080}

echo "[1] 插件列表"
curl -s "$BASE/api/plugins" | jq .

echo "[2] 代码生成预览"
curl -s -X POST "$BASE/api/gen/preview" -H 'Content-Type: application/json' -d '{
  "project":{"basePackage":"com.demo","moduleName":"demo","author":"tester"},
  "table":{"tableName":"t_demo","className":"Demo","businessName":"demo","comment":"demo","sceneType":"SINGLE","columns":[{"columnName":"id","javaField":"id","dbType":"bigint","javaType":"Long","comment":"ID","pk":true,"required":true}]}
}' | jq .

echo "[3] 工作流部署"
curl -s -X POST "$BASE/api/workflow/deploy" -H 'Content-Type: application/json' -d '{
  "processCode":"demo_flow","processName":"测试流程","version":1,
  "nodes":[
    {"nodeId":"start","nodeName":"开始","nodeType":"START"},
    {"nodeId":"approve1","nodeName":"审批","nodeType":"APPROVAL","approverType":"USER","approverValue":"U2","signType":"SINGLE","rejectToNodeId":"start"},
    {"nodeId":"end","nodeName":"结束","nodeType":"END"}
  ],
  "transitions":[
    {"fromNodeId":"start","toNodeId":"approve1","conditionExpr":"","priority":1},
    {"fromNodeId":"approve1","toNodeId":"end","conditionExpr":"","priority":1}
  ]
}' | jq .

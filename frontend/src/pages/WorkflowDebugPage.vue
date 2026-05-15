<template><div><h3>工作流调试</h3><el-input v-model="processCode" placeholder="processCode"/><el-button @click="start">启动实例</el-button><pre>{{result}}</pre></div></template>
<script setup lang="ts">
import { ref } from 'vue';
const processCode=ref('demo'); const result=ref('');
async function start(){
  const res=await fetch(`/api/workflow/start?processCode=${processCode.value}&version=1&businessKey=B1&userId=U1`,{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({amount:100})});
  result.value=JSON.stringify(await res.json(),null,2);
}
</script>

<template><div><h3>代码生成预览/下载</h3><el-button @click="preview">预览</el-button><el-button @click="download">下载ZIP</el-button><el-input type="textarea" :rows="18" v-model="content"/></div></template>
<script setup lang="ts">
import { ref } from 'vue';
const content=ref('');
const ctx={project:{basePackage:'com.demo',moduleName:'demo',author:'u'},table:{tableName:'t_demo',className:'Demo',businessName:'demo',comment:'demo',sceneType:'SINGLE',columns:[{columnName:'id',dbType:'bigint',javaField:'id',comment:'ID'}]}};
async function preview(){const res=await fetch('/api/gen/preview',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(ctx)});content.value=JSON.stringify(await res.json(),null,2);}
async function download(){const res=await fetch('/api/gen/zip',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(ctx)});const blob=await res.blob();const a=document.createElement('a');a.href=URL.createObjectURL(blob);a.download='codegen.zip';a.click();}
</script>

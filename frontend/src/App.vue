<template>
  <el-container>
    <el-aside width="220px"><el-menu router><el-menu-item index="/plugins">插件管理</el-menu-item><el-menu-item index="/workflow">工作流调试</el-menu-item><el-menu-item index="/codegen">代码生成</el-menu-item></el-menu></el-aside>
    <el-main><router-view /></el-main>
  </el-container>
</template>

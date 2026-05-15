import { createRouter, createWebHistory } from 'vue-router';
import PluginManagePage from '@/pages/PluginManagePage.vue';
import WorkflowDebugPage from '@/pages/WorkflowDebugPage.vue';
import CodegenPage from '@/pages/CodegenPage.vue';

export default createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/plugins' },
    { path: '/plugins', component: PluginManagePage },
    { path: '/workflow', component: WorkflowDebugPage },
    { path: '/codegen', component: CodegenPage }
  ]
});

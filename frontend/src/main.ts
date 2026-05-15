import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import { createPinia } from 'pinia';
import router from '@/router';
import App from '@/App.vue';
import PageDesigner from '@/page-designer/views/PageDesigner.vue';
import { registerDesignerDefaults } from '@/page-designer/registry/component-registry';

registerDesignerDefaults();
createApp(PageDesigner).use(ElementPlus).use(createPinia()).use(router).mount('#app');

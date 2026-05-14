import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import PageDesigner from '@/page-designer/views/PageDesigner.vue';
import { registerDesignerDefaults } from '@/page-designer/registry/component-registry';

registerDesignerDefaults();
createApp(PageDesigner).use(ElementPlus).mount('#app');

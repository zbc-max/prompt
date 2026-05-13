import { createApp } from 'vue';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import FormEngineDemo from '@/views/FormEngineDemo.vue';
import { registerDefaultComponents } from '@/components/engine/component-mapping';

registerDefaultComponents();

createApp(FormEngineDemo).use(ElementPlus).mount('#app');

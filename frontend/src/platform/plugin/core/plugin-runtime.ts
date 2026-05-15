import type { FrontPlugin } from '@/platform/plugin/sdk/types';
import { loadPlugin } from '@/platform/plugin/loader/plugin-loader';

export function bootPlugins(plugins: FrontPlugin[]): void {
  plugins.forEach(loadPlugin);
}

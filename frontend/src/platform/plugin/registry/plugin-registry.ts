import type { FrontPlugin } from '@/platform/plugin/sdk/types';

const plugins = new Map<string, FrontPlugin>();
const states = new Map<string, 'installed' | 'started' | 'stopped' | 'uninstalled'>();

export function registerPlugin(plugin: FrontPlugin): void {
  plugins.set(plugin.descriptor.pluginId, plugin);
  states.set(plugin.descriptor.pluginId, 'installed');
}

export function getPlugins(): FrontPlugin[] { return Array.from(plugins.values()); }
export function getPlugin(id: string): FrontPlugin | undefined { return plugins.get(id); }
export function setPluginState(id: string, state: 'installed' | 'started' | 'stopped' | 'uninstalled'): void { states.set(id, state); }
export function getPluginState(id: string): string | undefined { return states.get(id); }

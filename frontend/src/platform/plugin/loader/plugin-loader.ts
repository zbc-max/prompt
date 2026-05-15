import { getPlugins, registerPlugin, setPluginState } from '@/platform/plugin/registry/plugin-registry';
import { allPluginConfigs } from '@/platform/plugin/core/config-center';
import type { FrontPlugin, PluginContext } from '@/platform/plugin/sdk/types';

const components = new Map<string, unknown>();

const ctx: PluginContext = {
  configCenter: allPluginConfigs(),
  registerComponent: (name, comp) => components.set(name, comp)
};

export function loadPlugin(plugin: FrontPlugin): void {
  registerPlugin(plugin);
  plugin.install(ctx);
  plugin.start?.(ctx);
  setPluginState(plugin.descriptor.pluginId, 'started');
}

export function reloadPlugin(pluginId: string): void {
  const plugin = getPlugins().find((p) => p.descriptor.pluginId === pluginId);
  if (!plugin) return;
  plugin.reload?.(ctx);
  setPluginState(pluginId, 'started');
}

export function stopPlugin(pluginId: string): void {
  const plugin = getPlugins().find((p) => p.descriptor.pluginId === pluginId);
  if (!plugin) return;
  plugin.stop?.(ctx);
  setPluginState(pluginId, 'stopped');
}

export function resolvePluginComponent(name: string): unknown {
  return components.get(name);
}

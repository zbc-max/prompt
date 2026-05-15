export interface FrontPluginDescriptor {
  pluginId: string;
  pluginName: string;
  version: string;
  dependencies?: string[];
  permissions?: string[];
  menus?: Array<Record<string, unknown>>;
  routes?: Array<Record<string, unknown>>;
  config?: Record<string, unknown>;
}

export interface FrontPlugin {
  descriptor: FrontPluginDescriptor;
  install: (ctx: PluginContext) => void;
  start?: (ctx: PluginContext) => void;
  stop?: (ctx: PluginContext) => void;
  reload?: (ctx: PluginContext) => void;
  uninstall?: (ctx: PluginContext) => void;
}

export interface PluginContext {
  configCenter: Map<string, unknown>;
  registerComponent: (name: string, comp: unknown) => void;
}

const configCenter = new Map<string, unknown>();

export function setPluginConfig(key: string, value: unknown): void { configCenter.set(key, value); }
export function getPluginConfig<T>(key: string): T | undefined { return configCenter.get(key) as T | undefined; }
export function allPluginConfigs(): Map<string, unknown> { return configCenter; }

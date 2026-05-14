import type { DataSourceConfig, OptionItem } from '@/types/schema';

export async function fetchOptions(config: DataSourceConfig, formData: Record<string, unknown>): Promise<OptionItem[]> {
  const method = config.method ?? 'GET';
  const params = { ...(config.params ?? {}), ...formData };

  const query = new URLSearchParams(
    Object.entries(params).reduce<Record<string, string>>((acc, [k, v]) => {
      if (v !== undefined && v !== null) acc[k] = String(v);
      return acc;
    }, {})
  ).toString();

  const endpoint = method === 'GET' && query ? `${config.url}?${query}` : config.url;
  const response = await fetch(endpoint, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: method === 'POST' ? JSON.stringify(params) : undefined
  });

  const data = await response.json();
  const list = Array.isArray(data) ? data : (data?.data ?? []);
  const valueField = config.valueField ?? 'value';
  const labelField = config.labelField ?? 'label';
  return list.map((item: Record<string, unknown>) => ({
    value: item[valueField] as string | number | boolean,
    label: String(item[labelField])
  }));
}

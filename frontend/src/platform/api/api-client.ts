export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

export async function apiPost<T>(url: string, body: unknown): Promise<T> {
  const res = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });
  const json = (await res.json()) as ApiResponse<T>;
  if (json.code !== 0) throw new Error(json.message || '请求失败');
  return json.data;
}

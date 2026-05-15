export interface ApiResponse<T> { code: number; message: string; data: T; }

async function parse<T>(res: Response): Promise<T> {
  const json = (await res.json()) as ApiResponse<T>;
  if (json.code !== 0) throw new Error(json.message || '请求失败');
  return json.data;
}

export async function apiGet<T>(url: string): Promise<T> { return parse<T>(await fetch(url)); }

export async function apiPost<T>(url: string, body: unknown): Promise<T> {
  return parse<T>(await fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }));
}

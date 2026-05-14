export function safeEval(expr: string, context: Record<string, unknown>): unknown {
  const keys = Object.keys(context);
  const values = Object.values(context);
  // eslint-disable-next-line no-new-func
  const fn = new Function(...keys, `return (${expr});`);
  return fn(...values);
}

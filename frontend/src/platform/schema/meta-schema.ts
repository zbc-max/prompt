export type SchemaType = 'FORM' | 'PAGE' | 'WORKFLOW';

export interface MetaSchemaEnvelope {
  schemaType: SchemaType;
  schemaCode: string;
  version: string;
  meta: {
    tenantId: string;
    status: 'draft' | 'published';
    [k: string]: unknown;
  };
  payload: Record<string, unknown>;
}

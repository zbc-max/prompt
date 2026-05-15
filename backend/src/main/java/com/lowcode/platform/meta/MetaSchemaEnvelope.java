package com.lowcode.platform.meta;

import lombok.Data;

import java.util.Map;

@Data
public class MetaSchemaEnvelope {
    private String schemaType; // FORM/PAGE/WORKFLOW
    private String schemaCode;
    private String version;
    private Map<String, Object> meta;
    private Map<String, Object> payload;
}

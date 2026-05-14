package com.lowcode.workflow.engine;

import java.util.Map;

public class ConditionEvaluator {
    public boolean evaluate(String expr, Map<String, Object> vars) {
        if (expr == null || expr.isBlank()) return true;
        String[] parts = expr.split("==");
        if (parts.length == 2) {
            String key = parts[0].trim();
            String val = parts[1].trim().replace("'", "");
            return val.equals(String.valueOf(vars.get(key)));
        }
        return false;
    }
}

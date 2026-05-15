package com.lowcode.plugin.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PluginContext {
    private final Map<String, Object> shared = new ConcurrentHashMap<>();

    public void put(String key, Object value) { shared.put(key, value); }
    public Object get(String key) { return shared.get(key); }
    public Map<String, Object> all() { return shared; }
}

package com.lowcode.plugin.sdk;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PluginDescriptor {
    private String pluginId;
    private String pluginName;
    private String version;
    private String entryClass;
    private List<String> dependencies;
    private List<String> permissions;
    private List<Map<String, Object>> menus;
    private List<Map<String, Object>> routes;
    private Map<String, Object> config;
}

package com.lowcode.plugin.controller;

import com.lowcode.platform.api.ApiResponse;
import com.lowcode.plugin.core.PluginRegistry;
import com.lowcode.plugin.loader.PluginLoader;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plugins")
public class PluginController {
    private final PluginRegistry registry;
    private final PluginLoader loader;

    public PluginController(PluginRegistry registry, PluginLoader loader) {
        this.registry = registry;
        this.loader = loader;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        List<Map<String, Object>> data = registry.descriptors().stream().map(d -> Map.of(
                "pluginId", d.getPluginId(),
                "name", d.getPluginName(),
                "version", d.getVersion(),
                "permissions", d.getPermissions(),
                "menus", d.getMenus(),
                "routes", d.getRoutes()
        )).toList();
        return ApiResponse.ok(data);
    }

    @PostMapping("/{pluginId}/stop")
    public ApiResponse<String> stop(@PathVariable String pluginId) { loader.stop(pluginId); return ApiResponse.ok("OK"); }

    @PostMapping("/{pluginId}/reload")
    public ApiResponse<String> reload(@PathVariable String pluginId) { loader.reload(pluginId); return ApiResponse.ok("OK"); }

    @PostMapping("/{pluginId}/unload")
    public ApiResponse<String> unload(@PathVariable String pluginId) { loader.unload(pluginId); return ApiResponse.ok("OK"); }
}

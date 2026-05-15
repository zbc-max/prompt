package com.lowcode.plugin.core;

import com.lowcode.plugin.sdk.MicroKernelPlugin;
import com.lowcode.plugin.sdk.PluginDescriptor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PluginRegistry {
    private final Map<String, MicroKernelPlugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, PluginState> states = new ConcurrentHashMap<>();

    public void register(MicroKernelPlugin plugin) {
        String id = plugin.descriptor().getPluginId();
        plugins.put(id, plugin);
        states.put(id, PluginState.INSTALLED);
    }

    public MicroKernelPlugin get(String pluginId) { return plugins.get(pluginId); }

    public PluginState state(String pluginId) { return states.get(pluginId); }

    public void setState(String pluginId, PluginState state) { states.put(pluginId, state); }

    public List<PluginDescriptor> descriptors() {
        return plugins.values().stream().map(MicroKernelPlugin::descriptor).toList();
    }

    public void checkDependencies(PluginDescriptor descriptor) {
        if (descriptor.getDependencies() == null) return;
        for (String dep : descriptor.getDependencies()) {
            if (!plugins.containsKey(dep)) throw new IllegalStateException("缺少依赖插件: " + dep);
        }
    }
}

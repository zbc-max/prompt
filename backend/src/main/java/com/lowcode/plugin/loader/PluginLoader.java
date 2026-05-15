package com.lowcode.plugin.loader;

import com.lowcode.plugin.api.PluginContext;
import com.lowcode.plugin.core.PluginRegistry;
import com.lowcode.plugin.core.PluginState;
import com.lowcode.plugin.sdk.MicroKernelPlugin;
import org.springframework.stereotype.Component;

@Component
public class PluginLoader {
    private final PluginRegistry registry;
    private final PluginContext context = new PluginContext();

    public PluginLoader(PluginRegistry registry) { this.registry = registry; }

    public void load(MicroKernelPlugin plugin) {
        registry.checkDependencies(plugin.descriptor());
        registry.register(plugin);
        plugin.install(context);
        plugin.start(context);
        registry.setState(plugin.descriptor().getPluginId(), PluginState.STARTED);
    }

    public void stop(String pluginId) {
        MicroKernelPlugin plugin = registry.get(pluginId);
        if (plugin == null) return;
        plugin.stop(context);
        registry.setState(pluginId, PluginState.STOPPED);
    }

    public void reload(String pluginId) {
        MicroKernelPlugin plugin = registry.get(pluginId);
        if (plugin == null) return;
        plugin.reload(context);
        registry.setState(pluginId, PluginState.STARTED);
    }

    public void unload(String pluginId) {
        MicroKernelPlugin plugin = registry.get(pluginId);
        if (plugin == null) return;
        plugin.uninstall(context);
        registry.setState(pluginId, PluginState.UNINSTALLED);
    }
}

package com.lowcode.plugin.sdk;

import com.lowcode.plugin.api.PluginContext;

public interface MicroKernelPlugin {
    PluginDescriptor descriptor();
    default void install(PluginContext context) {}
    default void start(PluginContext context) {}
    default void stop(PluginContext context) {}
    default void uninstall(PluginContext context) {}
    default void reload(PluginContext context) { stop(context); start(context); }
}

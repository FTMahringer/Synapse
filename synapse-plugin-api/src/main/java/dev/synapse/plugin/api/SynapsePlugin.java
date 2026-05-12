package dev.synapse.plugin.api;

public interface SynapsePlugin {
    String getId();
    PluginManifest getManifest();
    void onLoad(PluginContext context);
    void onUnload();
}

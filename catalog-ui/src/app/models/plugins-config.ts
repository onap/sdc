
export class Plugin {
    pluginId: string;
    pluginDiscoveryUrl: string;
    pluginSourceUrl: string;
    pluginStateUrl: string;
    pluginDisplayOptions: Map<string, PluginDisplayOptions>;
}

export class PluginDisplayOptions {
    displayName: string;
    displayContext: Array<string>;
    displayRoles: Array<string>;
}

export type Plugins = Array<Plugin>;

export class PluginsConfiguration {
    static plugins: Plugins;
}

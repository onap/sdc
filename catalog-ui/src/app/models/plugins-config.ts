
export class Plugin {
    pluginId: string;
    pluginDiscoveryUrl: string;
    pluginSourceUrl: string;
    pluginStateUrl: string;
    pluginDisplayOptions: Map<string, PluginDisplayOptions>;
    isOnline: boolean;
}

export class PluginDisplayOptions {
    displayName: string;
    displayContext: string[];
    displayRoles: string[];
}

export type Plugins = Plugin[];

export class PluginsConfiguration {
    static plugins: Plugins;
    static connectionTimeout: boolean;
}

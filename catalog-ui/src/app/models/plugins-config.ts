
export class Plugin {
    pluginId: string;
    pluginHost: string;
    pluginPort: string;
    pluginPath: string;
    pluginStateUrl: string;
    pluginProtocol: string;
    pluginDisplayOptions: Map<string, PluginDisplayOptions>;
}

export class PluginDisplayOptions {
    displayName: string;
    displayContext: Array<string>;
}

export type Plugins = Array<Plugin>;

export class PluginsConfiguration {
    static plugins: Plugins;
}

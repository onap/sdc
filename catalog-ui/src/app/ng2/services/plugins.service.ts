import { Injectable } from '@angular/core';
import {Plugin, PluginsConfiguration} from "app/models";

@Injectable()
export class PluginsService {

    constructor() {
    }

    public getPluginByStateUrl = (stateUrl: string) => {
        let pluginKey: any = _.findKey(PluginsConfiguration.plugins, (pluginConfig: Plugin) =>{
            return pluginConfig.pluginStateUrl ===  stateUrl;
        });

        return PluginsConfiguration.plugins[pluginKey];
    };

    public isPluginDisplayedInContext = (plugin: Plugin ,userRole: string, contextType: string) => {
        return plugin.pluginDisplayOptions["context"] &&
               plugin.pluginDisplayOptions["context"].displayRoles.includes(userRole) &&
               plugin.pluginDisplayOptions["context"].displayContext.indexOf(contextType) !== -1
    }
}

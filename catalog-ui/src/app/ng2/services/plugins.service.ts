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
    }
}

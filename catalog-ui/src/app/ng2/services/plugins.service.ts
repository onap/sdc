import {Inject, Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {Http, Response} from '@angular/http';
import {IApi, IAppConfigurtaion, Plugin, PluginsConfiguration} from "app/models";
import {ISdcConfig, SdcConfigToken} from "../config/sdc-config.config";

@Injectable()
export class PluginsService {

    public configuration: IAppConfigurtaion;
    public api: IApi;
    private baseUrl;

    constructor(private http: Http, @Inject(SdcConfigToken) private sdcConfig: ISdcConfig) {
        this.api = this.sdcConfig.api;
        this.baseUrl = this.api.root + this.sdcConfig.api.component_api_root;
    }

    public getPluginByStateUrl = (stateUrl: string) => {
        let pluginKey: any = _.findKey(PluginsConfiguration.plugins, (pluginConfig: Plugin) => {
            return pluginConfig.pluginStateUrl === stateUrl;
        });

        return PluginsConfiguration.plugins[pluginKey];
    };

    public isPluginDisplayedInContext = (plugin: Plugin, userRole: string, contextType: string) => {
        return plugin.pluginDisplayOptions["context"] &&
            plugin.pluginDisplayOptions["context"].displayRoles.includes(userRole) &&
            plugin.pluginDisplayOptions["context"].displayContext.indexOf(contextType) !== -1
    };

    public isPluginOnline = (pluginId: string): Observable<boolean> => {
        let url: string = this.api.no_proxy_root + this.api.GET_plugin_online_state.replace(':pluginId', pluginId);
        return this.http.get(url).map((res: Response) => {
            return res.json()
        })
            .catch(error => Observable.of(false));
    }
}

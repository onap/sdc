import {Inject, Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {HttpBackend, HttpClient} from '@angular/common/http';
import {IApi, IAppConfigurtaion} from 'app/models/app-config';
import {Plugin, PluginsConfiguration} from 'app/models/plugins-config';
import {ISdcConfig, SdcConfigToken} from "../config/sdc-config.config";

@Injectable()
export class PluginsService {

    public configuration: IAppConfigurtaion;
    public api: IApi;
    private baseUrl;
    private http: HttpClient;

    constructor(httpBackend: HttpBackend, @Inject(SdcConfigToken) private sdcConfig: ISdcConfig) {
        this.http = new HttpClient(httpBackend);
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
        return this.http.get<boolean>(url).pipe(catchError(() => of(false)));
    }
}

import {Component, Inject} from "@angular/core";
import {IUserProperties, Plugin} from "app/models";
import {CacheService, PluginsService} from "app/services-ng2";

@Component({
    selector: 'plugin-tab-view',
    templateUrl: './plugin-tab-view.page.component.html',
    styleUrls: ['./plugin-tab-view.page.component.less']
})

export class PluginTabViewPageComponent {
    plugin: Plugin;
    user: IUserProperties;
    version: string;
    queryParams: Object;
    isLoading: boolean;

    constructor(@Inject("$stateParams") private _stateParams,
                private cacheService: CacheService,
                private pluginsService: PluginsService) {

        this.plugin = this.pluginsService.getPluginByStateUrl(_stateParams.path);
        this.version = this.cacheService.get('version');
        this.user = this.cacheService.get('user');
    }

    ngOnInit() {
        this.isLoading = true;

        this.queryParams = {
            userId: this.user.userId,
            userRole: this.user.role,
            displayType: "tab",
            parentUrl: window.location.origin,
            eventsClientId: this.plugin.pluginId
        };

    }

    onLoadingDone(plugin: Plugin) {
        if (plugin.pluginId == this.plugin.pluginId) {
            this.isLoading = false;
        }
    }
}
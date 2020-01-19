import {Component, Inject} from "@angular/core";
import {Component as ComponentData, IUserProperties, Plugin} from "app/models";
import {CacheService, PluginsService} from "app/services-ng2";


@Component({
    selector: 'plugin-context-view',
    templateUrl: './plugin-context-view.page.component.html',
    styleUrls: ['./plugin-context-view.page.component.less']
})

export class PluginContextViewPageComponent {
    plugin: Plugin;
    user: IUserProperties;
    queryParams: Object;
    isLoading: boolean;
    show: boolean;
    component: ComponentData;

    constructor(@Inject("$stateParams") private _stateParams,
                private cacheService: CacheService,
                private pluginsService: PluginsService) {

        this.show = false;
        this.component = this._stateParams.component;
        this.plugin = this.pluginsService.getPluginByStateUrl(_stateParams.path);
        this.user = this.cacheService.get('user');
    }

    ngOnInit() {
        this.isLoading = true;

        this.queryParams = {
            userId: this.user.userId,
            userRole: this.user.role,
            displayType: "context",
            contextType: this.component.getComponentSubType(),
            uuid: this.component.uuid,
            lifecycleState: this.component.lifecycleState,
            isOwner: this.component.lastUpdaterUserId === this.user.userId,
            version: this.component.version,
            parentUrl: window.location.origin,
            eventsClientId: this.plugin.pluginId
        };

        if (this._stateParams.queryParams) {
            _.assign(this.queryParams, this._stateParams.queryParams);
        }
    }

    onLoadingDone(plugin: Plugin) {
        if (plugin.pluginId == this.plugin.pluginId) {
            this.isLoading = false;
        }
    }


}

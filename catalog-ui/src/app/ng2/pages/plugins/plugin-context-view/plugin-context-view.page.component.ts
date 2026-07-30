import {Component} from "@angular/core";
import {Component as ComponentData, IUserProperties, Plugin} from "app/models";
import {CacheService, PluginsService} from "app/services-ng2";
import {NavigationService} from "../../../services/navigation.service";


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

    constructor(private navigationService: NavigationService,
                private cacheService: CacheService,
                private pluginsService: PluginsService) {

        this.show = false;
        this.component = this.navigationService.getParam('component');
        this.plugin = this.pluginsService.getPluginByStateUrl(this.navigationService.getParam('path'));
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
            componentId: this.component.uniqueId,
            lifecycleState: this.component.lifecycleState,
            isOwner: this.component.lastUpdaterUserId === this.user.userId,
            version: this.component.version,
            parentUrl: window.location.origin,
            eventsClientId: this.plugin.pluginId
        };

        const stateQueryParams = this.navigationService.getParam('queryParams');
        if (stateQueryParams) {
            _.assign(this.queryParams, stateQueryParams);
        }
    }

    onLoadingDone(plugin: Plugin) {
        if (plugin.pluginId == this.plugin.pluginId) {
            this.isLoading = false;
        }
    }


}

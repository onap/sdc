import {Plugin, IUserProperties} from "app/models";
import {CacheService} from "app/services";
import {PluginsService} from "../../../../ng2/services/plugins.service";
import {IWorkspaceViewModelScope} from "../../workspace-view-model";


interface IPluginsContextViewModelScope extends IWorkspaceViewModelScope {
    plugin: Plugin;
    user:IUserProperties;
    queryParams: Object;
    isLoading: boolean;

    onLoadingDone(plugin: Plugin): void;
}

export class PluginsContextViewModel {
    static '$inject' = [
        '$scope',
        '$stateParams',
        'Sdc.Services.CacheService',
        'PluginsService'
    ];

    constructor(private $scope:IPluginsContextViewModelScope,
                private $stateParams:any,
                private cacheService:CacheService,
                private pluginsService:PluginsService) {

        this.initScope();
    }

    private initScope = ():void => {
        this.$scope.isLoading = true;
        this.$scope.plugin = this.pluginsService.getPluginByStateUrl(this.$stateParams.path);
        this.$scope.user = this.cacheService.get('user');

        this.$scope.queryParams = {
            userId: this.$scope.user.userId,
            userRole: this.$scope.user.role,
            displayType: "context",
            contextType: this.$scope.component.getComponentSubType(),
            uuid: this.$scope.component.uuid,
            lifecycleState: this.$scope.component.lifecycleState,
            isOwner: this.$scope.component.lastUpdaterUserId === this.$scope.user.userId,
            version: this.$scope.component.version ,
            parentUrl: window.location.origin,
            eventsClientId: this.$scope.plugin.pluginId
        };

        this.$scope.onLoadingDone = (plugin: Plugin) => {
            if (plugin.pluginId == this.$scope.plugin.pluginId) {
                this.$scope.isLoading = false;
            }
        };

    }
}

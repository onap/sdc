import {Plugin, IUserProperties} from "app/models";
import {CacheService} from "app/services";
import {PluginsService} from "../../ng2/services/plugins.service";


interface IPluginsTabViewModelScope extends ng.IScope {
    plugin: Plugin
    user: IUserProperties;
    version: string;
    queryParams: Object;
}

export class PluginsTabViewModel {
    static '$inject' = [
        '$scope',
        '$stateParams',
        'Sdc.Services.CacheService',
        'PluginsService'
    ];

    constructor(private $scope:IPluginsTabViewModelScope,
                private $stateParams:any,
                private cacheService:CacheService,
                private pluginsService:PluginsService) {

        this.initScope();
    }

    private initScope = ():void => {
        this.$scope.plugin = this.pluginsService.getPluginByStateUrl(this.$stateParams.path);

        this.$scope.version = this.cacheService.get('version');

        this.$scope.user = this.cacheService.get('user');

        this.$scope.queryParams = {
            userId: this.$scope.user.userId
        };
    }
}

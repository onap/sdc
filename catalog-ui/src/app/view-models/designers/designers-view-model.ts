import {Designer, IUserProperties, DesignersConfiguration} from "app/models";
import {CacheService} from "app/services";
import {MenuItemGroup} from "app/utils";


interface IDesignerViewModelScope extends ng.IScope {
    designer: Designer
    topNavMenuModel:Array<MenuItemGroup>;
    user:IUserProperties;
    version:string;
}

export class DesignersViewModel {
    static '$inject' = [
        '$scope',
        '$stateParams',
        '$sce',
        'Sdc.Services.CacheService'
    ];

    constructor(private $scope:IDesignerViewModelScope,
                private $stateParams:any,
                private $sce:any,
                private cacheService:CacheService) {

        this.initScope();
    }

    private initScope = ():void => {
        // get the designer object by using the path parameter
        let designerKey: any = _.findKey(DesignersConfiguration.designers, (designerConfig: Designer) =>{
            return designerConfig.designerStateUrl ===  this.$stateParams.path;
        });

        this.$scope.designer = DesignersConfiguration.designers[designerKey];

        this.$scope.version = this.cacheService.get('version');
        this.$scope.topNavMenuModel = [];

        this.$scope.user = this.cacheService.get('user');
    }
}

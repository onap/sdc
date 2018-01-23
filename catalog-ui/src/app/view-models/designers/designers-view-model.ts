import {Designer, IUserProperties} from "app/models";
import {CacheService} from "app/services";
import {MenuItemGroup} from "app/utils";
import {DesignersService} from "../../ng2/services/designers.service";


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
        'Sdc.Services.CacheService',
        'DesignersService'
    ];

    constructor(private $scope:IDesignerViewModelScope,
                private $stateParams:any,
                private cacheService:CacheService,
                private designersService:DesignersService) {

        this.initScope();
    }

    private initScope = ():void => {
        this.$scope.designer = this.designersService.getDesignerByStateUrl(this.$stateParams.path);

        this.$scope.version = this.cacheService.get('version');
        this.$scope.topNavMenuModel = [];

        this.$scope.user = this.cacheService.get('user');
    }
}

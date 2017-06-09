'use strict';
import {CacheService} from "app/services";
import {IAppConfigurtaion} from "app/models";

interface IAdminDashboardViewModelScope extends ng.IScope {
    version:string;
    sdcConfig:IAppConfigurtaion;
    isLoading:boolean;
    currentTab:string;
    templateUrl:string;
    monitorUrl:string;
    moveToTab(tab:string):void;
    isSelected(tab:string):boolean;
}


export class AdminDashboardViewModel {
    static '$inject' = [
        '$scope',
        '$templateCache',
        'Sdc.Services.CacheService',
        'sdcConfig'
    ];

    constructor(private $scope:IAdminDashboardViewModelScope,
                private $templateCache:ng.ITemplateCacheService,
                private cacheService:CacheService,
                private sdcConfig:IAppConfigurtaion) {

        this.initScope();
    }

    private initScope = ():void => {

        this.$scope.version = this.cacheService.get('version');
        this.$scope.sdcConfig = this.sdcConfig;
        this.$scope.monitorUrl = this.$scope.sdcConfig.api.kibana;
        this.$scope.isSelected = (tab:string):boolean => {
            return tab === this.$scope.currentTab;
        }

        this.$scope.moveToTab = (tab:string):void => {
            if (tab === this.$scope.currentTab) {
                return;
            }
            else if (tab === 'USER_MANAGEMENT') {
                this.$scope.templateUrl="user-management-view.html";
                this.$templateCache.put("user-management-view.html", require('app/view-models/admin-dashboard/user-management/user-management-view.html'));
            }
            else if (tab === 'CATEGORY_MANAGEMENT') {
                this.$scope.templateUrl="category-management-view.html";
                this.$templateCache.put("category-management-view.html", require('app/view-models/admin-dashboard/category-management/category-management-view.html'));
            }
            this.$scope.currentTab = tab;
        };

        this.$scope.moveToTab('USER_MANAGEMENT');


    }
}

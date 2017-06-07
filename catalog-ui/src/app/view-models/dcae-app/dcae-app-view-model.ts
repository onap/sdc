'use strict';
import {MenuItemGroup, MenuItem} from "app/utils";
import {BreadcrumbsPath, BreadcrumbsMenu} from "../onboard-vendor/onboard-vendor-view-model";
import {CacheService} from "app/services";
import {IUserProperties} from "app/models";

export class TestData {
    breadcrumbs:BreadcrumbsPath;
}

export interface IDcaeAppViewModelScope extends ng.IScope {
    testData:TestData;
    onTestEvent:Function;
    topNavMenuModel:Array<MenuItemGroup>;
    topNavRootMenu:MenuItemGroup;
    user:IUserProperties;
    version:string;
}

export class DcaeAppViewModel {
    static '$inject' = [
        '$scope',
        '$q',
        'Sdc.Services.CacheService'
    ];

    private firstControlledTopNavMenu:MenuItemGroup;

    constructor(private $scope:IDcaeAppViewModelScope,
                private $q:ng.IQService,
                private cacheService:CacheService) {

        this.$scope.testData = {
            breadcrumbs: {
                selectedKeys: []
            }
        };

        this.$scope.version = this.cacheService.get('version');

        this.$scope.onTestEvent = (eventName:string, data:any):void => {
            switch (eventName) {
                case 'breadcrumbsupdated':
                    this.handleBreadcrumbsUpdate(data);
                    break;
            }
        };

        this.$scope.topNavMenuModel = [];

        this.$scope.user = this.cacheService.get('user');
    }

    updateBreadcrumbsPath = (selectedKeys:Array<string>):ng.IPromise<boolean> => {
        let topNavMenuModel = this.$scope.topNavMenuModel;
        let startIndex = topNavMenuModel.indexOf(this.firstControlledTopNavMenu);
        if (startIndex === -1) {
            startIndex = topNavMenuModel.length;
        }
        topNavMenuModel.splice(startIndex + selectedKeys.length);
        this.$scope.testData = {
            breadcrumbs: {selectedKeys: selectedKeys}
        };

        return this.$q.when(true);
    };

    handleBreadcrumbsUpdate(breadcrumbsMenus:Array<BreadcrumbsMenu>):void {
        let selectedKeys = [];
        let topNavMenus = breadcrumbsMenus.map((breadcrumbMenu, breadcrumbIndex) => {
            let topNavMenu = new MenuItemGroup();
            topNavMenu.menuItems = breadcrumbMenu.menuItems.map(menuItem =>
                new MenuItem(
                    menuItem.displayText,
                    this.updateBreadcrumbsPath,
                    null,
                    null,
                    [selectedKeys.concat([menuItem.key])]
                )
            );
            topNavMenu.selectedIndex = _.findIndex(
                breadcrumbMenu.menuItems,
                menuItem => menuItem.key === breadcrumbMenu.selectedKey
            );
            selectedKeys.push(breadcrumbMenu.selectedKey);
            return topNavMenu;
        });

        let topNavMenuModel = this.$scope.topNavMenuModel;
        let len = topNavMenuModel.length;
        let startIndex = topNavMenuModel.indexOf(this.firstControlledTopNavMenu);
        if (startIndex === -1) {
            startIndex = len;
        }
        topNavMenuModel.splice(startIndex, len - startIndex);
        topNavMenuModel.push.apply(topNavMenuModel, topNavMenus);
        this.firstControlledTopNavMenu = topNavMenus[0];

        if (startIndex === 1 && this.$scope.topNavRootMenu == null) {
            let topNavRootMenu = topNavMenuModel[0];
            let onboardItem = topNavRootMenu.menuItems[topNavRootMenu.selectedIndex];
            let originalCallback = onboardItem.callback;
            onboardItem.callback = (...args) => {
                let ret = this.updateBreadcrumbsPath([]);
                return originalCallback && originalCallback.apply(undefined, args) || ret;
            };
            this.$scope.topNavRootMenu = topNavRootMenu;
        }

        this.updateBreadcrumbsPath(selectedKeys);
    }
}

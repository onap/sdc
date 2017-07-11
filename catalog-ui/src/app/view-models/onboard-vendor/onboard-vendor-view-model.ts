/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

'use strict';
import {IUserProperties} from "app/models";
import {MenuItemGroup, MenuItem} from "app/utils";
import {CacheService} from "app/services";

export class BreadcrumbsMenuItem {
    key:string;
    displayText:string;
}

export class BreadcrumbsMenu {
    selectedKey:string;
    menuItems:Array<BreadcrumbsMenuItem>;
}

export class BreadcrumbsPath {
    selectedKeys:Array<string>;
}

export class VendorData {
    breadcrumbs:BreadcrumbsPath;
}

export interface IOnboardVendorViewModelScope extends ng.IScope {
    vendorData:VendorData;
    onVendorEvent:Function;
    topNavMenuModel:Array<MenuItemGroup>;
    topNavRootMenu:MenuItemGroup;
    user:IUserProperties;
    version:string;
}

export class OnboardVendorViewModel {
    static '$inject' = [
        '$scope',
        '$q',
        'Sdc.Services.CacheService'
    ];

    private firstControlledTopNavMenu:MenuItemGroup;

    constructor(private $scope:IOnboardVendorViewModelScope,
                private $q:ng.IQService,
                private cacheService:CacheService) {

        this.$scope.vendorData = {
            breadcrumbs: {
                selectedKeys: []
            }
        };

        this.$scope.version = this.cacheService.get('version');

        this.$scope.onVendorEvent = (eventName:string, data:any):void => {
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
        this.$scope.vendorData = {
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

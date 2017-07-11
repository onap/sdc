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

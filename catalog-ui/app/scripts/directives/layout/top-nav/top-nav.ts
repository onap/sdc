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
/// <reference path="../../../references"/>
module Sdc.Directives {
    'use strict';

    export interface ITopNavScope extends ng.IScope {
        topLvlSelectedIndex: number;
        hideSearch: boolean;
        searchBind: any;
        menuModel: Array<Utils.MenuItemGroup>;

        topLvlMenu: Utils.MenuItemGroup;
        goToState(state:string, params:Array<any>):ng.IPromise<boolean>;
        menuItemClick: Function;
        user: Models.IUserProperties;
        version:string;
    }


    export class TopNavDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService,
                    private $filter:ng.IFilterService,
                    private $state:ng.ui.IStateService,
                    private $q: ng.IQService,
                    private userResourceService: Sdc.Services.IUserResourceClass
            ) {
        }

        public replace = true;
        public restrict = 'E';
        public transclude = false;


        scope = {
            topLvlSelectedIndex: '@?',
            hideSearch: '=',
            searchBind: '=',
            version: '@',
            notificationIconCallback: '=',
            menuModel: '=?',
        };

        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/layout/top-nav/top-nav.html');
        };

        public link = (scope:ITopNavScope, $elem:ng.IAugmentedJQuery, $attrs:angular.IAttributes) => {

            let getTopLvlSelectedIndexByState = ():number => {
                if (!scope.topLvlMenu.menuItems) {
                    return 0;
                }

                let result = -1;

                //set result to current state
                scope.topLvlMenu.menuItems.forEach((item:Utils.MenuItem, index:number)=> {
                    if (item.state === this.$state.current.name) {
                        result = index;
                    }
                });

                //if it's a different state , checking previous state param
                if (result === -1) {
                    scope.topLvlMenu.menuItems.forEach((item:Utils.MenuItem, index:number)=> {
                        if (item.state === this.$state.params['previousState']) {
                            result = index;
                        }
                    });
                }

                if (result === -1) {
                    result = 0;
                }

                return result;
            };

            scope.user = this.userResourceService.getLoggedinUser();

            let tmpArray:Array<Utils.MenuItem> = [
                new Utils.MenuItem(this.$filter('translate')("TOP_MENU_HOME_BUTTON"), null, "dashboard", "goToState", null, null),
                new Utils.MenuItem(this.$filter('translate')("TOP_MENU_CATALOG_BUTTON"), null, "catalog", "goToState", null, null)
            ];

            // Only designer can perform onboarding
            if (scope.user && scope.user.role === 'DESIGNER'){
                tmpArray.push(new Utils.MenuItem(this.$filter('translate')("TOP_MENU_ON_BOARD_BUTTON"), null, "onboardVendor", "goToState", null, null));
            }

            scope.topLvlMenu = new Utils.MenuItemGroup(0, tmpArray , true );
            scope.topLvlMenu.selectedIndex = isNaN(scope.topLvlSelectedIndex) ? getTopLvlSelectedIndexByState() : scope.topLvlSelectedIndex;

            let generateMenu = () => {
                if (scope.menuModel && scope.menuModel[0] !== scope.topLvlMenu) {
                    scope.menuModel.unshift(scope.topLvlMenu);
                }
            };
            scope.$watch('menuModel', generateMenu);

            generateMenu();

            /////scope functions////

            scope.goToState = (state:string, params:Array<any>):ng.IPromise<boolean> => {
                let deferred = this.$q.defer();
                this.$state.go(state, params && params.length > 0 ? [0] : undefined);
                deferred.resolve(true);
                return deferred.promise;
            };

            scope.menuItemClick = (itemGroup:Utils.MenuItemGroup, item:Utils.MenuItem) => {

                itemGroup.itemClick = false;

                let onSuccess = ():void => {
                    itemGroup.selectedIndex = itemGroup.menuItems.indexOf(item);
                };
                let onFailed = ():void => {};

                if (item.callback) {
                    (item.callback.apply(undefined, item.params)).then(onSuccess, onFailed);
                } else {
                    scope[item.action](item.state, item.params).then(onSuccess, onFailed);
                }
            };
        };

        public static factory = ($templateCache:ng.ITemplateCacheService, $filter:ng.IFilterService, $state:ng.ui.IStateService, $q: ng.IQService, userResourceService: Sdc.Services.IUserResourceClass)=> {
            return new TopNavDirective($templateCache, $filter, $state,$q, userResourceService);
        };

    }

    TopNavDirective.factory.$inject = ['$templateCache', '$filter', '$state','$q', 'Sdc.Services.UserResourceService'];
}

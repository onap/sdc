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
    export interface IExpandCollapseMenuBoxDirectiveScope extends ng.IScope {
        menuItemsGroup: Utils.MenuItemGroup;
        menuTitle: string;
        parentScope: ng.IScope;
        onMenuItemClick(menuItem: Utils.MenuItem):void;
    }

    export class ExpandCollapseMenuBoxDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService) {
        }

        scope = {
            menuTitle: '@',
            menuItemsGroup: '=',
            parentScope: '='
        };

        public replace = false;
        public restrict = 'AE';
        public transclude = true;

        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/utils/expand-collapse-menu-box/expand-collapse-menu-box.html');
        };

        link = (scope:IExpandCollapseMenuBoxDirectiveScope, $elem:any) => {
            scope.onMenuItemClick = (menuItem: Utils.MenuItem):void => {
                let onSuccess = ():void => {
                    scope.menuItemsGroup.selectedIndex = scope.menuItemsGroup.menuItems.indexOf(menuItem);
                };
                let onFailed = ():void => {};
                scope.parentScope[menuItem.action](menuItem.state).then(onSuccess, onFailed);
            }
        };

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new ExpandCollapseMenuBoxDirective($templateCache);
        };

    }

    ExpandCollapseMenuBoxDirective.factory.$inject = ['$templateCache'];
}

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
/**
 * Created by obarda on 7/28/2016.
 */
/// <reference path="../../references"/>
module Sdc.Directives {
    'use strict';

    export interface ISdcTabsDirectiveScope extends ng.IScope {
        tabs:Array<Models.Tab>;
        selectedTab: Models.Tab;
        isActive: boolean;
        onTabSelected(selectedTab: Models.Tab);
    }

    export class SdcTabsDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService) {
        }

        scope = {
            tabs: "=",
            selectedTab: "=?",
            isViewOnly: "="
        };

        replace = true;
        restrict = 'E';
        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/sdc-tabs/sdc-tabs-directive-view.html');
        };

        link = (scope:ISdcTabsDirectiveScope) => {
            scope.isActive = true;

            if(!scope.selectedTab){
                scope.selectedTab = scope.tabs[0];
            }

            scope.onTabSelected = (selectedTab: Models.Tab) => {
                scope.selectedTab = selectedTab;
            }
        };

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new SdcTabsDirective($templateCache);
        };
    }

    SdcTabsDirective.factory.$inject = ['$templateCache'];
}

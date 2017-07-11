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
'use strict';
import {Tab} from "app/models";

export interface ISdcTabsDirectiveScope extends ng.IScope {
    tabs:Array<Tab>;
    selectedTab:Tab;
    isActive:boolean;
    onTabSelected(selectedTab:Tab);
}

export class SdcTabsDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        tabs: "=",
        selectedTab: "=?",
        isViewOnly: "="
    };

    replace = true;
    restrict = 'E';
    template = ():string => {
        return require('./sdc-tabs-directive-view.html');
    };

    link = (scope:ISdcTabsDirectiveScope) => {
        scope.isActive = true;

        if (!scope.selectedTab) {
            scope.selectedTab = scope.tabs[0];
        }

        scope.onTabSelected = (selectedTab:Tab) => {
            scope.selectedTab = selectedTab;
        }
    };

    public static factory = ()=> {
        return new SdcTabsDirective();
    };
}

SdcTabsDirective.factory.$inject = [];

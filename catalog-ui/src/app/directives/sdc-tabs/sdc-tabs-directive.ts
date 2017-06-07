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

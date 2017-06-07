'use strict';
import {MenuItem, MenuItemGroup} from "app/utils";

export interface IExpandCollapseMenuBoxDirectiveScope extends ng.IScope {
    menuItemsGroup:MenuItemGroup;
    menuTitle:string;
    parentScope:ng.IScope;
    onMenuItemClick(menuItem:MenuItem):void;
}

export class ExpandCollapseMenuBoxDirective implements ng.IDirective {

    constructor() {
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
        return require('./expand-collapse-menu-box.html');
    };

    link = (scope:IExpandCollapseMenuBoxDirectiveScope) => {
        scope.onMenuItemClick = (menuItem:MenuItem):void => {
            let onSuccess = ():void => {
                scope.menuItemsGroup.selectedIndex = scope.menuItemsGroup.menuItems.indexOf(menuItem);
            };
            let onFailed = ():void => {
            };
            scope.parentScope[menuItem.action](menuItem.state).then(onSuccess, onFailed);
        }
    };

    public static factory = ()=> {
        return new ExpandCollapseMenuBoxDirective();
    };

}

ExpandCollapseMenuBoxDirective.factory.$inject = [];

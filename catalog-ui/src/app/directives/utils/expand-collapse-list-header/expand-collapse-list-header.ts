/**
 * Created by rcohen on 12/5/2016.
 */
'use strict';

export class ExpandCollapseListData {
    filter:string;//variable for filter text
    orderByField:string;//order by field name
    expandCollapse:boolean;//boolean param for expand collapse the list
}

export interface IExpandCollapseListHeaderScope extends ng.IScope {
    title:string;//the title on the header
    expandCollapseListData:ExpandCollapseListData;
    showSearchBox:boolean;
    desc:boolean;//order by desc or asc

    swapOrderBy():void;
    showHideSearchBox():void;
}

export class ExpandCollapseListHeaderDirective implements ng.IDirective {

    constructor(private $timeout:ng.ITimeoutService) {
    }

    scope = {
        title: '@',
        expandCollapseListData: '='
    };

    public replace = false;
    public restrict = 'AE';
    public transclude = true;

    template = ():string => {
        return require('./expand-collapse-list-header.html');
    };

    link = (scope:IExpandCollapseListHeaderScope, $elem:any) => {
        scope.swapOrderBy = ():void => {
            if (scope.expandCollapseListData.orderByField.charAt(0) === '-') {
                scope.expandCollapseListData.orderByField = scope.expandCollapseListData.orderByField.substr(1);
            } else {
                scope.expandCollapseListData.orderByField = '-' + scope.expandCollapseListData.orderByField;
            }
            scope.desc = !scope.desc;
        };

        scope.showHideSearchBox = ():void => {
            scope.showSearchBox = !scope.showSearchBox;
            if (scope.showSearchBox) {
                this.$timeout(function () {
                    angular.element("#list-search-box").focus();
                }, 0);
            }
        };
    };

    public static factory = ($timeout:ng.ITimeoutService)=> {
        return new ExpandCollapseListHeaderDirective($timeout);
    };

}

ExpandCollapseListHeaderDirective.factory.$inject = ['$timeout'];

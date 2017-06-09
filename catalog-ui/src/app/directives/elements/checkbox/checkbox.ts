'use strict';

export interface ICheckboxElementScope extends ng.IScope {
    elemId:string;
    text:string;
    sdcChecklistModel:any;
    sdcChecklistValue:string;
    disabled:boolean;
}

export class CheckboxElementDirective implements ng.IDirective {

    constructor(private $filter:ng.IFilterService) {
    }

    public replace = true;
    public restrict = 'E';
    public transclude = false;

    scope = {
        elemId: '@',
        text: '@',
        disabled: '=',
        sdcChecklistModel: '=',
        sdcChecklistValue: '=',
        sdcChecklistChange: '&'
    };

    template = ():string => {
        return require('./checkbox.html');
    };

    public link = (scope:ICheckboxElementScope, $elem:ng.IAugmentedJQuery, $attrs:angular.IAttributes) => {

    };

    public static factory = ($filter:ng.IFilterService)=> {
        return new CheckboxElementDirective($filter);
    };

}

CheckboxElementDirective.factory.$inject = ['$filter'];

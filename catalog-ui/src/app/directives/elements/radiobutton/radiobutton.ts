import INgModelController = angular.INgModelController;
'use strict';

export interface IRadiobuttonElementScope extends ng.IScope {
    elemId:string;
    elemName:string;
    text:string;
    sdcModel:any;
    value:any;
    disabled:boolean;
    onValueChange:Function;
}

export class RadiobuttonElementDirective implements ng.IDirective {

    constructor(private $filter:ng.IFilterService) {
    }

    public replace = true;
    public restrict = 'E';
    public transclude = false;

    scope = {
        elemId: '@',
        elemName: '@',
        text: '@',
        sdcModel: '=',
        value: '@',
        disabled: '=',
        onValueChange: '&'
    };

    template = ():string => {
        return require('./radiobutton.html');
    };

    public link = (scope:IRadiobuttonElementScope, $elem:ng.IAugmentedJQuery, $attrs:angular.IAttributes) => {
        //$elem.removeAttr("id")
        //console.log(scope.sdcChecklistValue);
    };

    public static factory = ($filter:ng.IFilterService)=> {
        return new RadiobuttonElementDirective($filter);
    };

}

RadiobuttonElementDirective.factory.$inject = ['$filter'];

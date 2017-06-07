'use strict';

export interface ITopProgressScope extends ng.IScope {
    progressValue:number;
    progressMessage:string;
}

export class TopProgressDirective implements ng.IDirective {

    constructor() {
    }

    public replace = true;
    public restrict = 'E';
    public transclude = false;

    scope = {
        progressValue: '=',
        progressMessage: '='
    };

    template = ():string => {
        return require('./top-progress.html');
    };

    public link = (scope:ITopProgressScope, $elem:ng.IAugmentedJQuery, $attrs:angular.IAttributes) => {

    };

    public static factory = ()=> {
        return new TopProgressDirective();
    };

}

TopProgressDirective.factory.$inject = [];

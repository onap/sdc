'use strict';
export interface IEllipsisScope extends ng.IScope {
    ellipsis:string;
    maxChars:number;
    toggleText():void;
    collapsed:boolean;
    actualText:string;

}

export class EllipsisDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        ellipsis: '=',
        moreClass: '@',
        maxChars: '='
    };

    replace = false;
    restrict = 'A';
    template = ():string => {
        return require('./ellipsis-directive.html');
    };

    link = (scope:IEllipsisScope, $elem:any) => {


        scope.collapsed = true;

        scope.toggleText = ():void => {
            if (scope.ellipsis && scope.collapsed) {
                scope.actualText = scope.ellipsis.substr(0, scope.maxChars);
                scope.actualText += scope.ellipsis.length > scope.maxChars ? '...' : '';
            }
            else {
                scope.actualText = scope.ellipsis;
            }
        };

        scope.$watch("ellipsis", function () {
            scope.collapsed = true;
            scope.toggleText();
        });


    };

    public static factory = ()=> {
        return new EllipsisDirective();
    };

}

EllipsisDirective.factory.$inject = [];

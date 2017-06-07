'use strict';

/**
 * Usage:
 * In data-ng-repeat html: <ol ng-repeat="record in records" on-last-repeat>
 * In the controller, catch the last repeat:
 * $scope.$on('onRepeatLast', function(scope, element, attrs){
     * //work your magic
     * });
 */
export interface IOnLastRepeatDirectiveScope extends ng.IScope {
    $last:any;
}

export class OnLastRepeatDirective implements ng.IDirective {

    constructor() {
    }

    scope = {};

    restrict = 'AE';
    replace = true;

    link = (scope:IOnLastRepeatDirectiveScope, element:any, attrs:any) => {
        let s:any = scope.$parent; // repeat scope
        if (s.$last) {
            setTimeout(function () {
                s.$emit('onRepeatLast', element, attrs);
            }, 1);
        }
    };

    public static factory = ()=> {
        return new OnLastRepeatDirective();
    };

}

OnLastRepeatDirective.factory.$inject = [];

/**
 * Created by ob0695 on 5/15/2018.
 */
'use strict';

export class PreventDoubleClickDirective implements ng.IDirective {

    constructor(private $timeout:ng.ITimeoutService) {
    }

    restrict:'A';

    link = (scope, elem) => {

        let delay = 600;
        let disabled = false;

        scope.onClick = (evt) => {
            if (disabled) {
                evt.preventDefault();
                evt.stopImmediatePropagation();
            } else {
                disabled = true;
                this.$timeout(function () {
                    disabled = false;
                }, delay, false);
            }
        }

        scope.$on('$destroy', function () {
            elem.off('click', scope.onClick);
        });
        elem.on('click', scope.onClick);
    };

    public static factory = ($timeout:ng.ITimeoutService) => {
        return new PreventDoubleClickDirective($timeout);
    }
}

PreventDoubleClickDirective.factory.$inject = ['$timeout'];
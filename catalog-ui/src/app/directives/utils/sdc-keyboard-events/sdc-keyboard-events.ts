'use strict';

export interface ISdcKeyboardEventsScope extends ng.IScope {
    keyEnter:Function;
    keyShift:Function;
    keyCtrl:Function;
    keyEscape:Function;
    keySpace:Function;
}

export class SdcKeyboardEventsDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        keyEnter: '=',
        keyShift: '=',
        keyCtrl: '=',
        keyEscape: '=',
        keySpace: '='
    };

    public replace = false;
    public restrict = 'A';
    public transclude = false;

    link = (scope:ISdcKeyboardEventsScope, element:ng.IAugmentedJQuery, attrs:angular.IAttributes) => {

        element.bind("keydown keypress", function (event) {
            //console.log(event.which);
            switch (event.which) {
                case 13: // enter key
                    scope.$apply(function () {
                        if (scope.keyEnter) {
                            scope.keyEnter();
                            event.preventDefault();
                        }
                    });
                    break;
                case 16: // shift key
                    scope.$apply(function () {
                        if (scope.keyShift) {
                            scope.keyShift();
                            event.preventDefault();
                        }
                    });
                    break;
                case 17: // ctrl key
                    scope.$apply(function () {
                        if (scope.keyCtrl) {
                            scope.keyCtrl();
                            event.preventDefault();
                        }
                    });
                    break;
                case 27: // escape key
                    scope.$apply(function () {
                        if (scope.keyEscape) {
                            scope.keyEscape();
                            event.preventDefault();
                        }
                    });
                    break;
                case 32: // space key
                    scope.$apply(function () {
                        if (scope.keySpace) {
                            scope.keySpace();
                            event.preventDefault();
                        }
                    });
                    break;
            }
        });

    };

    public static factory = ()=> {
        return new SdcKeyboardEventsDirective();
    };

}

SdcKeyboardEventsDirective.factory.$inject = [];

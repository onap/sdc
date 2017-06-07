'use strict';
export interface ICustomValidationScope extends ng.IScope {
    validationFunc:Function;
}

export class CustomValidationDirective implements ng.IDirective {

    constructor() {
    }

    require = 'ngModel';
    restrict = 'A';

    scope = {
        validationFunc: '='
    };

    link = (scope:ICustomValidationScope, elem, attrs, ngModel) => {

        ngModel.$validators.customValidation = (modelValue, viewValue):boolean => {
            return scope.validationFunc(viewValue);
        };

    };

    public static factory = ()=> {
        return new CustomValidationDirective();
    };

}

CustomValidationDirective.factory.$inject = [];

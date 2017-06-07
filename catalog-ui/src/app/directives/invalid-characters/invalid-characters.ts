'use strict';

export class InvalidCharactersDirective implements ng.IDirective {

    constructor() {
    }

    require = 'ngModel';

    link = (scope, elem, attrs, ngModel) => {

        let invalidCharacters = [];

        attrs.$observe('invalidCharacters', (val:string) => {
            invalidCharacters = val.split('');
            validate(ngModel.$viewValue);
        });

        let validate:Function = function (value) {

            let valid:boolean = true;

            if (value) {
                for (let i = 0; i < invalidCharacters.length; i++) {
                    if (value.indexOf(invalidCharacters[i]) != -1) {
                        valid = false;
                    }
                }
            }

            ngModel.$setValidity('invalidCharacters', valid);
            if (!value) {
                ngModel.$setPristine();
            }
            return value;
        };

        //For DOM -> model validation
        ngModel.$parsers.unshift(validate);
        //For model -> DOM validation
        ngModel.$formatters.unshift(validate);

    };

    public static factory = ()=> {
        return new InvalidCharactersDirective();
    };

}

InvalidCharactersDirective.factory.$inject = [];

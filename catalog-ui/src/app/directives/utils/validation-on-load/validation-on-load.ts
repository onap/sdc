'use strict';

export interface IValidationOnLoadScope extends ng.IScope {
    formToValidate:ng.IFormController;
}

export class ValidationOnLoadDirective implements ng.IDirective {

    constructor(private $timeout:ng.ITimeoutService) {
    }

    scope = {
        formToValidate: '='
    };

    public replace = false;
    public restrict = 'A';


    public link = (scope:IValidationOnLoadScope, $elem:ng.IAugmentedJQuery, $attrs:angular.IAttributes) => {

        let init = ()=> {
            //validate errors
            if (scope.formToValidate.$error) {
                angular.forEach(scope.formToValidate.$error, (value, key)=> {
                    //skip on the required error if its a new form
                    if (key != 'required') {
                        angular.forEach(value, function (field) {
                            field.$setDirty();//trigger to show the error label
                        });
                    }
                })
            }
        };

        this.$timeout(()=> {
            init();
        }, 0);

    };

    public static factory = ($timeout:ng.ITimeoutService)=> {
        return new ValidationOnLoadDirective($timeout);
    };

}

ValidationOnLoadDirective.factory.$inject = ['$timeout'];

'use strict';

export class FileTypeDirective implements ng.IDirective {

    constructor() {
    }

    require = 'ngModel';

    link = (scope, elem, attrs, ngModel) => {

        let typesToApprove = "";

        attrs.$observe('fileType', (val:string) => {
            typesToApprove = val;
            validate(ngModel.$viewValue);
        });

        let validate:Function = function (value) {
            let fileName:string = elem.val(), valid:boolean = true;

            if (fileName && value && typesToApprove) {
                let extension:string = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
                valid = typesToApprove.split(',').indexOf(extension) > -1;
            }

            ngModel.$setValidity('filetype', valid);
            if (!value) {
                ngModel.$setPristine();
            }
            return value;
        };

        //For DOM -> model validation
        ngModel.$parsers.unshift(validate);

    };

    public static factory = ()=> {
        return new FileTypeDirective();
    };

}

FileTypeDirective.factory.$inject = [];

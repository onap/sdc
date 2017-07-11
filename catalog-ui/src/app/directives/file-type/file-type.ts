/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

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

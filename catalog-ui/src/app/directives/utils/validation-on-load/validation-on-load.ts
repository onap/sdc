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

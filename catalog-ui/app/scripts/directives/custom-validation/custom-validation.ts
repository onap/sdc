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
/// <reference path="../../references"/>
module Sdc.Directives {
    'use strict';


    export interface ICustomValidationScope extends ng.IScope {
        validationFunc: Function;
    }

    export class CustomValidationDirective implements ng.IDirective {

        constructor() {}

        require = 'ngModel';
        restrict = 'A';

        scope = {
            validationFunc: '='
        };

        link = (scope:ICustomValidationScope, elem, attrs, ngModel) => {

            ngModel.$validators.customValidation = (modelValue, viewValue) :boolean => {
                return scope.validationFunc(viewValue);
            };

        };

        public static factory = ()=> {
            return new CustomValidationDirective();
        };

    }

    CustomValidationDirective.factory.$inject = [];
}

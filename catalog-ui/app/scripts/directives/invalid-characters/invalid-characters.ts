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

    export class InvalidCharactersDirective implements ng.IDirective {

        constructor() {}

        require = 'ngModel';

        link = (scope, elem, attrs, ngModel) => {

            let invalidCharacters = [];

            attrs.$observe('invalidCharacters', (val:string) => {
                invalidCharacters = val.split('');
                validate(ngModel.$viewValue);
            });

            let validate: Function = function (value) {

                let valid:boolean = true;

                if(value) {
                    for (let i = 0; i < invalidCharacters.length; i++) {
                        if (value.indexOf(invalidCharacters[i]) != - 1) {
                            valid = false;
                        }
                    }
                }

                ngModel.$setValidity('invalidCharacters', valid);
                if(!value) {
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
}

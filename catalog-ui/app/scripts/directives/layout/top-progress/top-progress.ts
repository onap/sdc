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
/// <reference path="../../../references"/>
module Sdc.Directives {
    'use strict';

    export interface ITopProgressScope extends ng.IScope {
        progressValue:number;
        progressMessage:string;
    }

    export class TopProgressDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService) {}

        public replace = true;
        public restrict = 'E';
        public transclude = false;

        scope = {
            progressValue: '=',
            progressMessage: '='
        };

        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/layout/top-progress/top-progress.html');
        };

        public link = (scope:ITopProgressScope, $elem:ng.IAugmentedJQuery, $attrs:angular.IAttributes) => {

        };

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new TopProgressDirective($templateCache);
        };

    }

    TopProgressDirective.factory.$inject = ['$templateCache'];
}

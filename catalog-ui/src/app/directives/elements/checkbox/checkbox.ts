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

export interface ICheckboxElementScope extends ng.IScope {
    elemId:string;
    text:string;
    sdcChecklistModel:any;
    sdcChecklistValue:string;
    disabled:boolean;
}

export class CheckboxElementDirective implements ng.IDirective {

    constructor(private $filter:ng.IFilterService) {
    }

    public replace = true;
    public restrict = 'E';
    public transclude = false;

    scope = {
        elemId: '@',
        text: '@',
        disabled: '=',
        sdcChecklistModel: '=',
        sdcChecklistValue: '=',
        sdcChecklistChange: '&'
    };

    template = ():string => {
        return require('./checkbox.html');
    };

    public link = (scope:ICheckboxElementScope, $elem:ng.IAugmentedJQuery, $attrs:angular.IAttributes) => {

    };

    public static factory = ($filter:ng.IFilterService)=> {
        return new CheckboxElementDirective($filter);
    };

}

CheckboxElementDirective.factory.$inject = ['$filter'];

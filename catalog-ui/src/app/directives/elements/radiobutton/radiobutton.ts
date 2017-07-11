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

import INgModelController = angular.INgModelController;
'use strict';

export interface IRadiobuttonElementScope extends ng.IScope {
    elemId:string;
    elemName:string;
    text:string;
    sdcModel:any;
    value:any;
    disabled:boolean;
    onValueChange:Function;
}

export class RadiobuttonElementDirective implements ng.IDirective {

    constructor(private $filter:ng.IFilterService) {
    }

    public replace = true;
    public restrict = 'E';
    public transclude = false;

    scope = {
        elemId: '@',
        elemName: '@',
        text: '@',
        sdcModel: '=',
        value: '@',
        disabled: '=',
        onValueChange: '&'
    };

    template = ():string => {
        return require('./radiobutton.html');
    };

    public link = (scope:IRadiobuttonElementScope, $elem:ng.IAugmentedJQuery, $attrs:angular.IAttributes) => {
        //$elem.removeAttr("id")
        //console.log(scope.sdcChecklistValue);
    };

    public static factory = ($filter:ng.IFilterService)=> {
        return new RadiobuttonElementDirective($filter);
    };

}

RadiobuttonElementDirective.factory.$inject = ['$filter'];

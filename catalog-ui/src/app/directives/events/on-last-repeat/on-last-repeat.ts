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

/**
 * Usage:
 * In data-ng-repeat html: <ol ng-repeat="record in records" on-last-repeat>
 * In the controller, catch the last repeat:
 * $scope.$on('onRepeatLast', function(scope, element, attrs){
     * //work your magic
     * });
 */
export interface IOnLastRepeatDirectiveScope extends ng.IScope {
    $last:any;
}

export class OnLastRepeatDirective implements ng.IDirective {

    constructor() {
    }

    scope = {};

    restrict = 'AE';
    replace = true;

    link = (scope:IOnLastRepeatDirectiveScope, element:any, attrs:any) => {
        let s:any = scope.$parent; // repeat scope
        if (s.$last) {
            setTimeout(function () {
                s.$emit('onRepeatLast', element, attrs);
            }, 1);
        }
    };

    public static factory = ()=> {
        return new OnLastRepeatDirective();
    };

}

OnLastRepeatDirective.factory.$inject = [];

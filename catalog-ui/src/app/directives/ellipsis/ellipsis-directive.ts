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
export interface IEllipsisScope extends ng.IScope {
    ellipsis:string;
    maxChars:number;
    toggleText():void;
    onMoreLessClick(event): void;
    collapsed:boolean;
    actualText:string;

}

export class EllipsisDirective implements ng.IDirective {

    constructor() {
    }

    scope = {
        ellipsis: '=',
        moreClass: '@',
        maxChars: '='
    };

    replace = false;
    restrict = 'A';
    template = ():string => {
        return require('./ellipsis-directive.html');
    };

    link = (scope:IEllipsisScope, $elem:any) => {


        scope.collapsed = true;

        scope.onMoreLessClick = (event): void => {
            event.stopPropagation();
            scope.collapsed = !scope.collapsed;
            scope.toggleText();
        };

        scope.toggleText = ():void => {
            if (scope.ellipsis && scope.collapsed) {
                scope.actualText = scope.ellipsis.substr(0, scope.maxChars);
                scope.actualText += scope.ellipsis.length > scope.maxChars ? '...' : '';
            }
            else {
                scope.actualText = scope.ellipsis;
            }
        };

        scope.$watch("ellipsis", function () {
            scope.collapsed = true;
            scope.toggleText();
        });


    };

    public static factory = ()=> {
        return new EllipsisDirective();
    };

}

EllipsisDirective.factory.$inject = [];

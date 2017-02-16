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

    class ListItem {
        name;
        url;
    }

    export interface IPageSelectorScope extends ng.IScope {
        selected:string;
        expanded: boolean;
        list:Array<ListItem>;
        exceptSelectedComparator(actual, expected):boolean;
        openCollapse();
    }

    export class PageSelectorDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService) {
        }

        scope = {
            list: '=',
            selected: '@',
        };

        public replace = true;
        public restrict = 'E';
        public transclude = false;

        private ulElement:HTMLElement;
        private itemHeight:number = 64;

        private getUlHeight = ():number => {
            let tmp:string = getComputedStyle(this.ulElement).height;
            //console.log("tmp: " + tmp);
            let ulHeight:number = parseInt(tmp.substr(0,tmp.length-2));
            //console.log("ulHeight: " + ulHeight);
            return ulHeight;
        };

        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/utils/page-selector/page-selector.html');
        };

        link = (scope:IPageSelectorScope, $elem:any) => {
            scope.expanded=false;

            window.setTimeout(() => {
                this.ulElement = angular.element(".i-sdc-left-sidebar-page-nav ul")[0];
                console.log("this.ulElement: " + this.ulElement);
                console.log("this.itemHeight: " + this.itemHeight);
                this.ulElement.style.top = (this.itemHeight - this.getUlHeight() - 5) + 'px';
                this.ulElement.style.visibility = 'visible';
            },10);

            this.ulElement = angular.element(".i-sdc-left-sidebar-page-nav ul")[0];

            scope.exceptSelectedComparator = (actual) => {
                if (actual.name===scope.selected) {
                    return false;
                }
                return true;
            };

            scope.openCollapse = ():void => {
                scope.expanded=!scope.expanded;
                if (scope.expanded===true) {
                    this.ulElement.style.transition = 'top 0.4s ease-out';
                    this.ulElement.style.top = this.itemHeight + 'px';
                } else {
                    this.ulElement.style.transition = 'top 0.4s ease-in';
                    this.ulElement.style.top =  (this.itemHeight - this.getUlHeight() - 5) + 'px';
                }
            };

        };

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new PageSelectorDirective($templateCache);
        };

    }

    PageSelectorDirective.factory.$inject = ['$templateCache'];
}

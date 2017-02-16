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
    export interface IExpandCollapseScope extends ng.IScope {
        toggle(): void;
        collapsed: boolean;
        expandedSelector: string;
        content:string;
        isCloseOnInit:boolean;
        loadDataFunction: Function;
        isLoadingData: boolean;
    }

    export class ExpandCollapseDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService) {
        }

        scope = {
            expandedSelector: '@',
            loadDataFunction: '&?',
            isCloseOnInit: '=?'
        };

        public replace = false;
        public restrict = 'AE';
        public transclude = true;

        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/utils/expand-collapse/expand-collapse.html');
        };

        link = (scope:IExpandCollapseScope, $elem:any) => {
            scope.collapsed = false;
            scope.isLoadingData = false;
            $elem.addClass('expanded');


            if(scope.isCloseOnInit) {
                  window.setTimeout(function () {
                    toggle();
                },0);
            }

            $elem.click(function(){
                toggle();
            });

            let expand = ():void => {
                $elem.addClass('expanded');
                scope.collapsed = false;

                let element = $(scope.expandedSelector)[0];
                let prevWidth = element.style.height;
                element.style.height = 'auto';
                let endWidth = getComputedStyle(element).height;
                element.style.height = prevWidth;
                element.offsetHeight; // force repaint
                element.style.transition = 'height .3s ease-in-out';
                element.style.height = endWidth;
                element.hidden = false;
                element.addEventListener('transitionend', function transitionEnd(event) {
                    if (event['propertyName'] == 'height') {
                        element.style.transition = '';
                        element.style.height = 'auto';
                        element.removeEventListener('transitionend', transitionEnd, false);
                    }
                }, false)
            };

            let collapse = ():void => {
                $elem.removeClass('expanded');
                scope.collapsed = true;

                let element = $(scope.expandedSelector)[0];
                 element.style.height = getComputedStyle(element).height;
                 element.style.transition = 'height .5s ease-in-out';
                 element.offsetHeight; // force repaint
                 element.style.height = '0px';
                element.hidden = true;
            };

            let toggle = ():void => {
                if (scope.collapsed === true){
                    if(scope.loadDataFunction) {
                        scope.isLoadingData = true;
                        let onSuccess = () => {
                            window.setTimeout(function () {
                                expand();
                                scope.isLoadingData = false;
                            },0);
                        };
                        scope.loadDataFunction().then(onSuccess);
                    }
                    else  {
                        if(scope.isLoadingData === false) {
                            expand();
                        }
                    }

                } else {
                    if(scope.isLoadingData === false) {
                        collapse();
                    }
                }
            }

        };

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new ExpandCollapseDirective($templateCache);
        };

    }

    ExpandCollapseDirective.factory.$inject = ['$templateCache'];
}

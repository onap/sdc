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

    export interface ISmartTooltipScope extends ng.IScope {
        sdcSmartToolip;
    }

    export class SmartTooltipDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService,
                    private $compile:ng.ICompileService) {
        }

        public replace = false;
        public restrict = 'A';
        public transclude = false;

        public link = (scope:ISmartTooltipScope, $elem:ng.IAugmentedJQuery, $attrs:angular.IAttributes) => {

            if ($elem[0].hasAttribute('style')===false){
                $elem[0].setAttribute("style", "overflow: hidden; white-space: nowrap; text-overflow: ellipsis;");
            } else {
                let styles = $elem.attr('style');
                $elem[0].setAttribute("style", styles + ";overflow: hidden; white-space: nowrap; text-overflow: ellipsis;");
            }

            $elem.bind('mouseenter', () => {
                if($elem[0].offsetWidth < $elem[0].scrollWidth && !$elem.attr('tooltips')){
                    $attrs.$set('tooltips', 'tooltips');
                    if ($attrs['sdcSmartTooltip'] && $attrs['sdcSmartTooltip'].length>0){
                        $elem.attr('tooltip-content', $attrs['sdcSmartTooltip']);
                    } else {
                        $attrs.$set('tooltip-content', $elem.text());
                    }

                    //One possible problem arises when the ngIf is placed on the root element of the template.
                    //ngIf removes the node and places a comment in it's place. Then it watches over the expression and adds/removes the actual HTML element as necessary.
                    //The problem seems to be that if it is placed on the root element of the template, then a single comment is what is left from the
                    //whole template (even if only temporarily), which gets ignored (I am not sure if this is browser-specific behaviour), resulting in an empty template.

                    // Remove ng-if attribute and its value (if we reach here, we pass ng-if (ng-if===true), so we can remove it).
                    $elem.removeAttr('ng-if');
                    $elem.removeAttr('data-ng-if');

                    // Remove me (the directive from the element)
                    let template = $elem[0].outerHTML;
                    template = template.replace('sdc-smart-tooltip=""','');
                    template = template.replace('sdc-smart-tooltip="' + $elem.text() + '"','');
                    //console.log(template);

                    let el = this.$compile(template)(scope);
                    console.log(el);
                    $elem.replaceWith(el);
                }
            });
        };

        public static factory = ($templateCache:ng.ITemplateCacheService, $compile:ng.ICompileService)=> {
            return new SmartTooltipDirective($templateCache, $compile);
        };

    }

    SmartTooltipDirective.factory.$inject = ['$templateCache', '$compile'];
}

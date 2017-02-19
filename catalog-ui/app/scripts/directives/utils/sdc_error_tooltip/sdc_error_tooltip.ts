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

    export interface ISdcErrorTooltipScope extends ng.IScope {
        alignToSelector: string;
        topMargin: string;
    }

    export class SdcErrorTooltipDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService) {
        }

        scope = {
            alignToSelector: '@', // Jquery selector to align to
            topMargin: '@'    // The margin from the top, in case there is label or not the top margin is different.
        };

        public replace = false;
        public restrict = 'E';
        public transclude = true;

        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/utils/sdc_error_tooltip/sdc_error_tooltip.html');
        };

        link = (scope:ISdcErrorTooltipScope, $elem:any) => {
            let _self = this;

            $elem.addClass("i-sdc-form-item-error-icon");

            // Calculate the position of the elements after they loaded to the dom.
            window.setTimeout(function(){
                _self.calculatePosition(scope, $elem);
            },100);

            $elem.bind('mouseover', function(){
                $(".i-sdc-form-item-error-message",$elem).css("display", "block");
            });

            $elem.bind('mouseleave', function(){
                $(".i-sdc-form-item-error-message",$elem).css("display", "none");
            });

        }

        private calculatePosition(scope:ISdcErrorTooltipScope, $elem:any):void {
            let leftMargin = 13;
            let topMargin = scope.topMargin? parseInt(scope.topMargin) : 10;

            if (scope.alignToSelector) {
                // Set the position of the error, in case user add align-to-selector attribute
                let jObj = $(scope.alignToSelector);
                if (jObj.length > 0) {
                    let height1 = jObj.outerHeight();
                    $elem.css('left', jObj.position().left + jObj.outerWidth() + leftMargin);
                    //$elem.css('top', jObj.position().top + topMargin + (height1 / 2));
                    $elem.css('top', jObj.position().top + (height1 / 2) - 5); // Label margin is: 2
                }
            } else {
                // Set the position of the error, according to the input element.
                let inputElm = $elem.siblings('input');
                let textareaElm = $elem.siblings('textarea');
                let selectElm = $elem.siblings('select');
                if (inputElm.length > 0) {
                    $elem.css('left', inputElm.outerWidth() + leftMargin);
                    $elem.css('top', inputElm.position().top + topMargin);
                } else if (textareaElm.length > 0) {
                    $elem.css('left', textareaElm.outerWidth() + leftMargin);
                    let height2 = textareaElm.outerHeight();
                    let elmHeight2 = $elem.outerHeight();
                    //let top = textareaElm.position().top;
                    $elem.css('bottom', (height2 - (elmHeight2 / 2)) / 2);
                } else if (selectElm.length > 0) {
                    $elem.css('left', selectElm.outerWidth() + leftMargin);
                    $elem.css('top', selectElm.position().top + topMargin);
                }
            }
        }

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new SdcErrorTooltipDirective($templateCache);
        };

    }

    SdcErrorTooltipDirective.factory.$inject = ['$templateCache'];

}

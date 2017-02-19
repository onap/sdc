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
    export interface ISdcMessageScope extends ng.IScope {
        sdcTranslate: string;
        sdcTranslateValues:string;
        sdcAlign:string;
    }

    export class SdcMessageDirective implements ng.IDirective {

        constructor(private $animate:any, private $filter:any, private $parse:any) {
        }

        scope = {
            field: '=',
            required: '@',
            pattern: '@',
            sdcTranslate: '@',
            sdcTranslateValues: '@',
            sdcAlign: '@'
        };

        public terminal = true;
        public restrict = 'A';
        public transclude = 'element';
        public require = '^^sdcMessages';

        link = (scope:ISdcMessageScope, $element:any, $attrs:any,sdcMessagesCtrl:any, $transclude:any) => {
            let self = this;

            let commentNode = $element[0];

            let records;
            let staticExp = $attrs.sdcMessage || $attrs.when;
            let dynamicExp = $attrs.sdcMessageExp || $attrs.whenExp;
            let assignRecords = function(items) {
                records = items
                    ? (angular.isArray(items)
                    ? items
                    : items.split(/[\s,]+/))
                    : null;
                sdcMessagesCtrl.reRender();
            };

            if (dynamicExp) {
                assignRecords(scope.$eval(dynamicExp));
                scope.$watchCollection(dynamicExp, assignRecords);
            } else {
                assignRecords(staticExp);
            }

            let currentElement, messageCtrl;
            sdcMessagesCtrl.register(commentNode, messageCtrl = {
                test: function (name) {
                    return self.contains(records, name);
                },
                attach: function () {
                    if (!currentElement) {
                        $transclude(scope, function (elm) {

                            self.$animate.enter(elm, null, $element);
                            currentElement = elm;

                            elm.addClass("i-sdc-form-item-error-message");

                            //$compile
                            let text;
                            if (scope.sdcTranslate) {
                                text = self.$filter('translate')(scope.sdcTranslate, scope.sdcTranslateValues);
                            } else {
                                //TODO: Need to handle this
                                //let t = elm.html();
                                //let t = angular.element("<span>" + elm.html() + "</span>");
                                //text = self.$parse(t);
                            }

                            //scope.sdcTranslateValues
                            elm.html(text);

                            elm.prepend("<span class='error'></span>");

                            // Adding OK to close the message
                            //let okElm = $('<span />').attr('class', 'ok').html('OK');
                            //okElm.click(function(e){
                            //    messageCtrl.detach();
                            //});
                            //elm.append(okElm);

                            // Handle the position
                            if (scope.sdcAlign){
                                let choosenElm = $(scope.sdcAlign);
                                if (choosenElm.length > 0) {
                                    let height1 = choosenElm.outerHeight();
                                    let elmHeight1 = elm.outerHeight();
                                    elm.css('left', choosenElm.outerWidth());
                                    elm.css('bottom', (height1 - (elmHeight1 / 2)) / 2);
                                }
                            } else {
                                // Set the position of the error, according to the input element.
                                let inputElm = elm.parent().siblings('input');
                                let textareaElm = elm.parent().siblings('textarea');
                                let selectElm = elm.parent().siblings('select');
                                if (inputElm.length > 0) {
                                    elm.css('left', inputElm.outerWidth());
                                    elm.css('top', inputElm.position().top);
                                } else if (textareaElm.length > 0) {
                                    elm.css('left', textareaElm.outerWidth());
                                    let height = textareaElm.outerHeight();
                                    let elmHeight = elm.outerHeight();
                                    //let top = textareaElm.position().top;
                                    elm.css('bottom', (height - (elmHeight / 2)) / 2);
                                } else if (selectElm.length > 0) {
                                    elm.css('left', selectElm.outerWidth());
                                    elm.css('top', selectElm.position().top);
                                }
                            }

                            // Each time we attach this node to a message we get a new id that we can match
                            // when we are destroying the node later.
                            let $$attachId = currentElement.$$attachId = sdcMessagesCtrl.getAttachId();

                            // in the event that the parent element is destroyed
                            // by any other structural directive then it's time
                            // to deregister the message from the controller
                            currentElement.on('$destroy', function () {
                                if (currentElement && currentElement.$$attachId === $$attachId) {
                                    sdcMessagesCtrl.deregister(commentNode);
                                    messageCtrl.detach();
                                }
                            });
                        });
                    }
                },
                detach: function () {
                    if (currentElement) {
                        let elm = currentElement;
                        currentElement = null;
                        self.$animate.leave(elm);
                    }
                }
            });
        }

        contains = (collection, key):any => {
            if (collection) {
                return angular.isArray(collection)
                    ? collection.indexOf(key) >= 0
                    : collection.hasOwnProperty(key);
            }
        }

        public static factory = ($animate:any, $filter:any, $parse:any)=> {
            return new SdcMessageDirective($animate, $filter, $parse);
        };

    }

    SdcMessageDirective.factory.$inject = ['$animate', '$filter', '$parse'];
}

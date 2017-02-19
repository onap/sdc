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
    export interface ISdcMessagesScope extends ng.IScope {
        sdcMessages: any;
        editForm:ng.IFormController;
    }

    export class SdcMessagesDirective implements ng.IDirective {

        constructor() {}

        scope = {
            sdcMessages: '='
        };

        public restrict = 'AE';
        public require = 'sdcMessages';
        public controller = SdcMessagesController;

        /*template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/utils/sdc-messages/sdc-messages.html');
        };

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new SdcMessagesDirective($templateCache);
        };*/

        public static factory = ()=> {
            return new SdcMessagesDirective();
        }

    }

    export class SdcMessagesController {

        messages:any;
        getAttachId:Function;
        render:any;
        reRender:Function;
        register:Function;
        deregister:Function;
        head:any;

        static '$inject' = [
            '$element',
            '$scope',
            '$attrs',
            '$animate'
        ];

        constructor(private $element:JQuery,
                    private $scope:ISdcMessagesScope,
                    private $attrs:ng.IAttributes,
                    private $animate:any
            ) {

            this.init();

        }

        init=():void => {
            let self = this;

            let ACTIVE_CLASS:string = 'ng-active';
            let INACTIVE_CLASS:string = 'ng-inactive';

            let ctrl = this;
            let latestKey = 0;
            let nextAttachId = 0;

            this.getAttachId = function getAttachId() { return nextAttachId++; };

            let messages = this.messages = {};
            let renderLater, cachedCollection;

            this.render = function(collection) {
                collection = collection || {};

                renderLater = false;
                cachedCollection = collection;

                // this is true if the attribute is empty or if the attribute value is truthy
                let multiple =  self.isAttrTruthy(self.$scope, self.$attrs['sdcMessagesMultiple']) || self.isAttrTruthy(self.$scope, self.$attrs['multiple']);

                let unmatchedMessages = [];
                let matchedKeys = {};
                let messageItem = ctrl.head;
                let messageFound = false;
                let totalMessages = 0;

                // we use != instead of !== to allow for both undefined and null values
                while (messageItem != null) {
                    totalMessages++;
                    let messageCtrl = messageItem.message;

                    let messageUsed = false;
                    if (!messageFound) {
                        _.each(collection, function(value, key) {
                            if (!messageUsed && self.truthy(value) && messageCtrl.test(key)) {
                                // this is to prevent the same error name from showing up twice
                                if (matchedKeys[key]) return;
                                matchedKeys[key] = true;

                                messageUsed = true;
                                messageCtrl.attach();
                            }
                        });
                    }

                    if (messageUsed) {
                        // unless we want to display multiple messages then we should
                        // set a flag here to avoid displaying the next message in the list
                        messageFound = !multiple;
                    } else {
                        unmatchedMessages.push(messageCtrl);
                    }

                    messageItem = messageItem.next;
                }

                _.each(unmatchedMessages, function(messageCtrl) {
                    messageCtrl.detach();
                });

                unmatchedMessages.length !== totalMessages
                    ? ctrl.$animate.setClass(self.$element, ACTIVE_CLASS, INACTIVE_CLASS)
                    : ctrl.$animate.setClass(self.$element, INACTIVE_CLASS, ACTIVE_CLASS);
            };

            self.$scope.$watchCollection('sdcMessages' || self.$attrs['for'], function(newVal:any, oldVal:any){
                ctrl.render(newVal);
            });

            this.reRender = function() {
                if (!renderLater) {
                    renderLater = true;
                    self.$scope.$evalAsync(function() {
                        if (renderLater) {
                            cachedCollection && ctrl.render(cachedCollection);
                        }
                    });
                }
            };

            this.register = function(comment, messageCtrl) {
                let nextKey = latestKey.toString();
                messages[nextKey] = {
                    message: messageCtrl
                };
                insertMessageNode(self.$element[0], comment, nextKey);
                comment.$$sdcMessageNode = nextKey;
                latestKey++;

                ctrl.reRender();
            };

            this.deregister = function(comment) {
                let key = comment.$$sdcMessageNode;
                delete comment.$$sdcMessageNode;
                removeMessageNode(self.$element[0], comment, key);
                delete messages[key];
                ctrl.reRender();
            };

            function findPreviousMessage(parent, comment) {
                let prevNode = comment;
                let parentLookup = [];
                while (prevNode && prevNode !== parent) {
                    let prevKey = prevNode.$$sdcMessageNode;
                    if (prevKey && prevKey.length) {
                        return messages[prevKey];
                    }

                    // dive deeper into the DOM and examine its children for any sdcMessage
                    // comments that may be in an element that appears deeper in the list
                    if (prevNode.childNodes.length && parentLookup.indexOf(prevNode) == -1) {
                        parentLookup.push(prevNode);
                        prevNode = prevNode.childNodes[prevNode.childNodes.length - 1];
                    } else {
                        prevNode = prevNode.previousSibling || prevNode.parentNode;
                    }
                }
            }

            function insertMessageNode(parent, comment, key) {
                let messageNode = messages[key];
                if (!ctrl.head) {
                    ctrl.head = messageNode;
                } else {
                    let match = findPreviousMessage(parent, comment);
                    if (match) {
                        messageNode.next = match.next;
                        match.next = messageNode;
                    } else {
                        messageNode.next = ctrl.head;
                        ctrl.head = messageNode;
                    }
                }
            }

            function removeMessageNode(parent, comment, key) {
                let messageNode = messages[key];

                let match = findPreviousMessage(parent, comment);
                if (match) {
                    match.next = messageNode.next;
                } else {
                    ctrl.head = messageNode.next;
                }
            }
        }

        isAttrTruthy = (scope, attr):any => {
            return (angular.isString(attr) && attr.length === 0) || //empty attribute
                this.truthy(scope.$eval(attr));
        }

        truthy = (val):any => {
            return angular.isString(val) ? val.length : !!val;
        }

    }

    SdcMessagesDirective.factory.$inject = ['$templateCache','$animate'];
}

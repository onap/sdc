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

    export interface ISdcTagsScope extends ng.IScope {
        tags:Array<string>;
        specialTag:string;
        newTag:string;
        formElement:ng.IFormController;
        elementName:string;
        pattern:any;
        sdcDisabled:boolean;
        maxTags:number;
        deleteTag(tag:string):void;
        addTag(tag:string):void;
        validateName():void;
    }

    export class SdcTagsDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService) {
        }

        scope = {
            tags: '=',
            specialTag: '=',
            pattern: '=',
            sdcDisabled: '=',
            formElement: '=',
            elementName: '@',
            maxTags: '@'
        };

        public replace = false;
        public restrict = 'E';
        public transclude = false;

        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/utils/sdc-tags/sdc-tags.html');
        };

        link = (scope:ISdcTagsScope, element:ng.INgModelController) => {

               scope.deleteTag = (tag:string):void => {
                scope.tags.splice(scope.tags.indexOf(tag),1);
            };

            scope.addTag = ():void => {
                let valid = scope.formElement[scope.elementName].$valid;
                if (valid &&
                    scope.tags.length<scope.maxTags &&
                    scope.newTag &&
                    scope.newTag!=='' &&
                    scope.tags.indexOf(scope.newTag)===-1 &&
                    scope.newTag!==scope.specialTag) {
                        scope.tags.push(scope.newTag);
                        scope.newTag='';
                }
            };

            scope.validateName = ():void => {
                if (scope.tags.indexOf(scope.newTag)>-1) {
                    scope.formElement[scope.elementName].$setValidity('nameExist', false);
                }else{
                    scope.formElement[scope.elementName].$setValidity('nameExist', true);
                }
            }

        };

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new SdcTagsDirective($templateCache);
        };

    }

    SdcTagsDirective.factory.$inject = ['$templateCache'];
}

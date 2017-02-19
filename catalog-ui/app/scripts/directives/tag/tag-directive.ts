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
/// <reference path="../../references"/>
module Sdc.Directives {
    'use strict';

    export class TagData {
        tag:string;
        tooltip:string;
        id: string;
    }

    export interface ITagScope extends ng.IScope {
        tagData: TagData;
        onDelete: Function;
        delete:Function;
        hideTooltip:boolean;
        hideDelete:boolean;
        sdcDisable: boolean;
    }

    export class TagDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService) {
        }

        scope = {
            tagData: '=',
            onDelete: '&',
            hideTooltip: '=',
            hideDelete: '=',
            sdcDisable: '='
        };

        replace = true;
        restrict = 'EA';
        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/tag/tag-directive.html');
        };

        link = (scope:ITagScope) => {
            scope.delete = ()=>{
                scope.onDelete({'uniqueId':scope.tagData.id});
            }
        };

        public static factory = ($templateCache:ng.ITemplateCacheService)=> {
            return new TagDirective($templateCache);
        };

    }

    TagDirective.factory.$inject = ['$templateCache'];
}

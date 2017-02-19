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

    export interface ISdcModalScope extends ng.IScope {
        modal:ng.ui.bootstrap.IModalServiceInstance;
        hideBackground:string;
        ok():void;
        close(result:any):void;
        cancel(reason:any):void;
    }

    export interface ISdcModalButton {
        name:string;
        css:string;
        disabled?:boolean;
        callback:Function;
    }

    export class SdcModalDirective implements ng.IDirective {

        constructor(
            private $templateCache: ng.ITemplateCacheService
        ) {}

        scope = {
            modal: '=',
            type: '@',
            header: '@',
            headerTranslate: '@',
            headerTranslateValues: '@',
            showCloseButton: '@',
            hideBackground: '@',
            buttons: '=',
            getCloseModalResponse: '='
        };

        public replace = true;
        public restrict = 'E';
        public transclude = true;

        template = (): string => {
            return this.$templateCache.get('/app/scripts/directives/modal/sdc-modal.html');
        };

        link = (scope:ISdcModalScope, $elem:any) => {

            if (scope.hideBackground==="true"){
                $(".modal-backdrop").css('opacity','0');
            }

            scope.close = function (result:any) {
                scope.modal.close(result);
            };

            scope.ok = function () {
                scope.modal.close();
            };

            scope.cancel = function (reason:any) {
                if(this.getCloseModalResponse)
                     scope.modal.dismiss(this.getCloseModalResponse());
                else {
                    scope.modal.dismiss();
                }
            };

            if (scope.modal) {
                scope.modal.result.then(function (selectedItem) {
                    //$scope.selected = selectedItem;
                }, function () {
                    //console.info('Modal dismissed at: ' + new Date());
                });
            }
        }

        public static factory = ($templateCache: ng.ITemplateCacheService)=> {
            return new SdcModalDirective($templateCache);
        };

    }

    SdcModalDirective.factory.$inject = ['$templateCache'];
}

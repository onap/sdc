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
/**
 * Created by obarda on 1/27/2016.
 */
/// <reference path="../../references"/>
module Sdc.Directives {
    'use strict';

    export class FileUploadModel {
        filetype: string;
        filename: string;
        filesize: number;
        base64: string;
    }

    export interface IFileUploadScope extends ng.IScope {
        fileModel: FileUploadModel;
        formElement:ng.IFormController;
        extensions: string;
        elementDisabled: string;
        elementName: string;
        elementRequired: string;
        myFileModel: any; // From the ng bind to <input type=file
        defaultText: string;
        onFileChangedInDirective:Function;

        getExtensionsWithDot():string;
        onFileChange():void
        onFileClick(element:any):void;
        setEmptyError(element):void;
        validateField(field:any):boolean;
        cancel():void;
    }


    export class FileUploadDirective implements ng.IDirective {

        constructor(private $templateCache:ng.ITemplateCacheService, private sdcConfig:Models.IAppConfigurtaion) {
        }

        scope = {
            fileModel: '=',
            formElement: '=',
            extensions: '@',
            elementDisabled: '@',
            elementName: '@',
            elementRequired: '@',
            onFileChangedInDirective: '=?',
            defaultText: '=',
        };

        restrict = 'E';
        replace = true;
        template = ():string => {
            return this.$templateCache.get('/app/scripts/directives/file-upload/file-upload.html');
        };

        link = (scope:IFileUploadScope, element:any, $attr:any) => {

            // In case the browse has filename, set it valid.
            // When editing artifact the file is not sent again, so if we have filename I do not want to show error.
            if (scope.fileModel && scope.fileModel.filename && scope.fileModel.filename!==''){
                scope.formElement[scope.elementName].$setValidity('required', true);
            }

            scope.getExtensionsWithDot = ():string => {
                let ret = [];
                if(scope.extensions) {
                    _.each(scope.extensions.split(','), function (item) {
                        ret.push("." + item.toString());
                    });
                }
                return ret.join(",");
            };

            scope.onFileChange = ():void => {
                if (scope.onFileChangedInDirective) {
                    scope.onFileChangedInDirective();
                }
                if (scope.myFileModel) {
                    scope.fileModel = scope.myFileModel;
                    scope.formElement[scope.elementName].$setValidity('required', true);
                }
            };

            scope.setEmptyError = (element):void => {
                if(element.files[0].size){
                    scope.formElement[scope.elementName].$setValidity('emptyFile', true);
                }else{
                    scope.formElement[scope.elementName].$setValidity('emptyFile', false);
                    scope.fileModel = undefined;
                }

            };

            // Workaround, in case user select a file then cancel (X) then select the file again, the event onChange is not fired.
            // This is a workaround to fix this issue.
            scope.onFileClick = (element:any):void => {
                element.value = null;
            };

            scope.cancel = ():void => {
                scope.fileModel.filename = '';
                scope.formElement[scope.elementName].$pristine;
                scope.formElement[scope.elementName].$setValidity('required', false);
            }
        };

        public static factory = ($templateCache:ng.ITemplateCacheService, sdcConfig:Models.IAppConfigurtaion)=> {
            return new FileUploadDirective($templateCache, sdcConfig);
        };

    }

    FileUploadDirective.factory.$inject = ['$templateCache', 'sdcConfig'];
}

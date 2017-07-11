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

'use strict';

export interface IFileOpenerScope extends ng.IScope {
    importFile:any;
    testsId:any;
    extensions:string;

    onFileSelect():void;
    onFileUpload(file:any):void;
    getExtensionsWithDot():string;
}

export class FileOpenerDirective implements ng.IDirective {

    constructor(private $compile:ng.ICompileService) {
    }

    scope = {
        onFileUpload: '&',
        testsId: '@',
        extensions: '@'
    };

    restrict = 'AE';
    replace = true;
    template = ():string => {
        return require('./file-opener.html');
    };

    link = (scope:IFileOpenerScope, element:any) => {

        scope.onFileSelect = () => {
            scope.onFileUpload({file: scope.importFile});
            element.html('app/directives/file-opener/file-opener.html');
            this.$compile(element.contents())(scope);
        };

        scope.getExtensionsWithDot = ():string => {
            let ret = [];
            _.each(scope.extensions.split(','), function (item) {
                ret.push("." + item.toString());
            });
            return ret.join(",");
        };

    };

    public static factory = ($compile:ng.ICompileService)=> {
        return new FileOpenerDirective($compile);
    };

}

FileOpenerDirective.factory.$inject = ['$compile'];

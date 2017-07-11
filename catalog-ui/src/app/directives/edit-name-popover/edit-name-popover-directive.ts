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

export interface IEditNamePopoverDirectiveScope extends ng.IScope {
    isOpen:boolean;
    templateUrl:string;
    module:any;
    direction:string;
    header:string;
    heatNameValidationPattern:RegExp;
    originalName:string;
    onSave:any;

    closePopover(isCancel:boolean):void;
    validateField(field:any, originalName:string):boolean;
    updateHeatName(heatName:string):void;
    onInit():void;
}

export class EditNamePopoverDirective implements ng.IDirective {

    constructor(private ValidationPattern:RegExp,private $templateCache:ng.ITemplateCacheService) {
    }

    scope = {
        direction: "@?",
        module: "=",
        header: "@?",
        onSave: "&"
    };

    link = (scope:IEditNamePopoverDirectiveScope) => {
        if (!scope.direction) {
            scope.direction = 'top';
        }

        scope.originalName = '';
        this.$templateCache.put("edit-module-name-popover.html", require('./edit-module-name-popover.html'));
        scope.templateUrl = "edit-module-name-popover.html";
        scope.isOpen = false;

        scope.closePopover = (isCancel:boolean = true) => {
            scope.isOpen = !scope.isOpen;

            if (isCancel) {
                scope.module.heatName = scope.originalName;
            }
        };

        scope.onInit = () => {
            scope.originalName = scope.module.heatName;
        };

        scope.validateField = (field:any):boolean => {
            return !!(field && field.$dirty && field.$invalid);
        };

        scope.heatNameValidationPattern = this.ValidationPattern;

        scope.updateHeatName = () => {
            scope.closePopover(false);
            scope.onSave();
        }

    };

    replace = true;
    restrict = 'E';
    template = ():string => {
        return require('./edit-name-popover-view.html');
    };

    public static factory = (ValidationPattern:RegExp,$templateCache:ng.ITemplateCacheService)=> {
        return new EditNamePopoverDirective(ValidationPattern,$templateCache);
    }
}

EditNamePopoverDirective.factory.$inject = ['ValidationPattern','$templateCache'];

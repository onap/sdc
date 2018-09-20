/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 Huawei Intellectual Property. All rights reserved.
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
import {
    ComponentType, DEFAULT_ICON
} from "app/utils";
import { CacheService } from "app/services";
import { WorkspaceMode, ResourceType } from "app/utils";
import { IValidate, Resource, Component, IMainCategory, ISubCategory } from "app/models";
import { ComponentServiceNg2 } from "../../../ng2/services/component-services/component.service"
import { LeftPaletteLoaderService } from "../../../services/components/utils/composition-left-palette-service";




export class Validation {
    componentNameValidationPattern: RegExp;
    commentValidationPattern: RegExp;
}


export interface IResourceViewModelScope extends ng.IScope {
    validation: Validation;
    validationPattern: RegExp;
    isAlreadyPressed: boolean;
    footerButtons: Array<any>;
    forms: any;
    modalInstanceName: ng.ui.bootstrap.IModalServiceInstance;
    name: string;
    component: Component;
    parentComponent: Component;
    componentId: string;
    save(): void;
    close(): void;
    validateField(field: any): boolean;
}

export class ResourceSaveViewFormModel {

    static '$inject' = [
        '$scope',
        '$state',
        'ValidationPattern',
        'ComponentNameValidationPattern',
        'CommentValidationPattern', 
        '$uibModalInstance',
        'Sdc.Services.CacheService',
        'ComponentServiceNg2',
        'serviceId',
        'component',
        'parentComponent',
        'LeftPaletteLoaderService'
    ];

    constructor(private $scope: IResourceViewModelScope,
        private $state: ng.ui.IStateService,
        private ValidationPattern: RegExp,
        private ComponentNameValidationPattern: RegExp,
        private CommentValidationPattern: RegExp,
        private $uibModalInstance: ng.ui.bootstrap.IModalServiceInstance,
        private cacheService: CacheService,
        private componentServiceNg2: ComponentServiceNg2,
        private serviceId: string,
        private component: Component,
        private parentComponent: Component,
        private LeftPaletteLoaderService: LeftPaletteLoaderService
    ) {
        this.initScopeValidation();
        this.initScope();
    }

    private initScopeValidation = (): void => {
        this.$scope.validation = new Validation();
        this.$scope.validation.componentNameValidationPattern = this.ComponentNameValidationPattern;
        this.$scope.validation.commentValidationPattern = this.CommentValidationPattern;
    };

    private initScope = (): void => {
        this.$scope.validationPattern = this.ValidationPattern;
        this.$scope.component = this.component; 
        this.$scope.parentComponent = this.parentComponent;       
        this.$scope.isAlreadyPressed = false;
        this.$scope.component.contactId = this.cacheService.get("user").userId;
        this.$scope.forms = {};
        this.$scope.modalInstanceName = this.$uibModalInstance;
        this.$scope.validationPattern = this.ValidationPattern;
        this.$scope.componentType = "combination"; 


        // Init UIModel
        this.$scope.component.tags = _.without(this.$scope.component.tags, this.$scope.component.name);

        this.$scope.close = (): void => {
            this.$uibModalInstance.close();
        }
        this.$scope.save = (): void => {
            let onSaveSuccess = (response: any): void => {
                this.$uibModalInstance.close();
                this.$scope.isAlreadyPressed = false;
                this.$state.go('catalog');
            }
            let onSaveFailed = (response: any): void => {
                this.$uibModalInstance.close();
                this.$scope.isAlreadyPressed = true;
            }
            this.$scope.component.tags = _.without(this.$scope.component.tags, this.$scope.component.name);
            this.$scope.component.tags[0] = this.$scope.component.name;
            this.$scope.isAlreadyPressed = true;
            this.$scope.component.icon = "combination";

            this.componentServiceNg2.createServiceInput(this.$scope.component, this.serviceId).subscribe(onSaveSuccess, onSaveSuccess);
        };

        this.$scope.footerButtons = [
            { 'name': 'Done', 'css': 'blue', 'callback': this.$scope.save },
            { 'name': 'Cancel', 'css': 'grey', 'callback': this.$scope.close }
        ];



        this.$scope.validateField = (field: any): boolean => {
            if (field && field.$dirty && field.$invalid) {
                return true;
            }
            return false;
        }

        this.$scope.validateName = (isInit: boolean): void => {
            let name = this.$scope.component.name;
            if (!name || name === "") {
                if (this.$scope.forms.editForm
                    && this.$scope.forms.editForm["componentName"]
                    && this.$scope.forms.editForm["componentName"].$error) {

                    // Clear the error name already exists
                    this.$scope.forms.editForm["componentName"].$setValidity('nameExist', true);
                }

                return;
            }
            let subtype: string = 'Combination';

            let onFailed = (response) => {
            };

            let onSuccess = (validation: IValidate) => {
                this.$scope.forms.editForm["componentName"].$setValidity('nameExist', validation.isValid);
                if (validation.isValid) {
                    //update breadcrumb after changing name
                    this.updateComponentNameInBreadcrumbs();
                }
            };

            // Validating on change (has debounce)
            if (this.$scope.forms.editForm
                && this.$scope.forms.editForm["componentName"]
                && this.$scope.forms.editForm["componentName"].$error
                && !this.$scope.forms.editForm["componentName"].$error.pattern
            ) {
                if (!(this.$scope.componentType === ComponentType.RESOURCE && (<Resource>this.$scope.component).csarUUID !== undefined)
                ) {
                    this.$scope.component.validateName(name, subtype).then(onSuccess, onFailed);
                }
            }

        };



        /*this.$scope.$watchCollection('component.name', (newData:any):void => {
            this.$scope.validateName(false);
        });*/

        this.$scope.$watch("[forms.editForm.$invalid,component.name,isAlreadyPressed]", (newVal, oldVal) => {
            //if the name is invalid or if user pressed ok and didn't try to change name again or the new name = source name
            this.$scope.footerButtons[0].disabled = this.$scope.forms.editForm.$invalid || (this.$scope.isAlreadyPressed && newVal[1] === oldVal[1]); // || this.$scope.component.name === this.$scope.oldName;
        });

    };
    

    private updateComponentNameInBreadcrumbs = (): void => {
        //update breadcrum after changing name
        this.$scope.breadcrumbsModel[1].updateSelectedMenuItemText(this.$scope.component.getComponentSubType() + ': ' + this.$scope.component.name);
        this.$scope.updateMenuComponentName(this.$scope.component.name);
    };

}

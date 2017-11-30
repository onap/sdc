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
import {ValidationUtils} from "app/utils";
import {ArtifactModel, HeatParameterModel, Component} from "app/models";

export interface IEnvParametersFormViewModelScope extends ng.IScope {
    isLoading:boolean;
    type:string;
    heatParameters:Array<HeatParameterModel>;
    forms:any;
    artifactResource:ArtifactModel;
    buttons:Array<any>;
    envParametersModal:ng.ui.bootstrap.IModalServiceInstance;
    tableHeadersList:Array<any>;
    selectedParameter:HeatParameterModel;
    templatePopover:string;

    getValidationPattern(type:string):RegExp;
    isInstance():boolean;
    validateJson(json:string):boolean;
    onValueChanged(parameter: HeatParameterModel):void;
    close():void;
    save():void;
    openDescPopover(selectedParam:HeatParameterModel):void;
    closeDescriptionPopover():void;
}

export class EnvParametersFormViewModel {

    static '$inject' = [
        '$scope',
        '$templateCache',
        '$state',
        '$uibModalInstance',
        'artifact',
        'ValidationUtils',
        'component'
    ];

    constructor(private $scope:IEnvParametersFormViewModelScope,
                private $templateCache:ng.ITemplateCacheService,
                private $state:any,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private artifact:ArtifactModel,
                private ValidationUtils:ValidationUtils,
                private component:Component) {


        this.initScope();
    }

    private updateInstanceHeat = ():void => {
        let success = (responseArtifact:ArtifactModel):void => {
            this.$scope.isLoading = false;
            this.$uibModalInstance.close();
        };

        let error = ():void => {
            this.$scope.isLoading = false;
            console.info('Failed to load save artifact');
        };

        this.component.addOrUpdateInstanceArtifact(this.$scope.artifactResource).then(success, error);
    };

    private initScope = ():void => {
        this.$scope.forms = {};
        this.$scope.envParametersModal = this.$uibModalInstance;
        this.$scope.artifactResource = this.artifact;
        this.$scope.heatParameters = angular.copy(this.artifact.heatParameters);
        //if param does not have a value - display the default
        this.$scope.heatParameters.forEach((heatParam) => {
            heatParam.currentValue = heatParam.currentValue || heatParam.defaultValue;
        });
        this.$scope.tableHeadersList = [
            {title: "Parameter", property: "name"},
            {title: "Default Value", property: "defaultValue", info: "DEFAULT_VALUE_INFO"},
            {title: "Current Value", property: "currentValue", info: "CURRENT_VALUE_INFO"}
        ];

        this.$templateCache.put("env-parametr-description-popover.html", require('app/view-models/forms/env-parameters-form/env-parametr-description-popover.html'));
        this.$scope.templatePopover = "env-parametr-description-popover.html";

        this.$scope.getValidationPattern = (validationType:string, parameterType?:string):RegExp => {
            return this.ValidationUtils.getValidationPattern(validationType, parameterType);
        };

        this.$scope.validateJson = (json:string):boolean => {
            if (!json) {
                return true;
            }
            return this.ValidationUtils.validateJson(json);
        };

        this.$scope.isInstance = ():boolean => {
            return !!this.component.selectedInstance;
        };

        this.$scope.save = ():void => {
            this.$scope.buttons[0].disabled = true;//prevent double click (DE246266)
            this.$scope.isLoading = true;
            this.artifact.heatParameters = angular.copy(this.$scope.heatParameters);
            this.artifact.heatParameters.forEach((parameter:any):void => {
                if ("" === parameter.currentValue) {
                    //[Bug 154465] - Update and erase current value field in Env parameters form return empty String ("") instead of null.
                    parameter.currentValue = null;
                } else if (parameter.defaultValue && parameter.defaultValue == parameter.currentValue) {
                    parameter.currentValue = undefined;
                }
            });

            if (this.$scope.isInstance()) {
                this.updateInstanceHeat();
                return;
            }

            let success = (responseArtifact:ArtifactModel):void => {
                this.$scope.isLoading = false;
                this.$uibModalInstance.close();

            };

            let error = ():void => {
                this.$scope.isLoading = false;
                console.info('Failed to load save artifact');
            };

            this.component.addOrUpdateArtifact(this.$scope.artifactResource).then(success, error);
        };

        this.$scope.onValueChanged = (parameter: HeatParameterModel):void => {
            parameter.filterTerm = parameter.name + ' ' + parameter.currentValue + ' ' + parameter.defaultValue + ' ' +parameter.description
            if('json'==parameter.type){
                this.$scope.forms.editForm[parameter.name].$setValidity('pattern', this.$scope.validateJson(parameter.currentValue));
            }
        }

        this.$scope.close = ():void => {
            //this.artifact.heatParameters.forEach((parameter:any):void => {
            //    if (!parameter.currentValue && parameter.defaultValue) {
            //        parameter.currentValue = parameter.defaultValue;
            //    }
            //});
            this.$uibModalInstance.dismiss();
        };

        this.$scope.openDescPopover = (selectedParam:HeatParameterModel):void => {
            this.$scope.selectedParameter = selectedParam;
        };

        this.$scope.closeDescriptionPopover = ():void => {
            this.$scope.selectedParameter = null;
        };

        this.$scope.buttons = [
            {'name': 'Save', 'css': 'blue', 'callback': this.$scope.save},
            {'name': 'Cancel', 'css': 'grey', 'callback': this.$scope.close}
        ];

        this.$scope.$watch("forms.editForm.$invalid", (newVal, oldVal) => {
            this.$scope.buttons[0].disabled = this.$scope.forms.editForm.$invalid;
        });

    };
}

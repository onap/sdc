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
import {ComponentInstanceFactory} from "app/utils";
import {ComponentInstance} from "app/models";
import {Requirement, Component, Capability} from "app/models";

interface IResourceInstanceViewModelScope extends ng.IScope {

    componentInstanceModel:ComponentInstance;
    validationPattern:RegExp;
    oldName:string;
    isAlreadyPressed:boolean;
    footerButtons:Array<any>;
    forms:any;
    modalInstanceName:ng.ui.bootstrap.IModalServiceInstance;

    save():void;
    close():void;
}

export class ResourceInstanceNameViewModel {

    static '$inject' = [
        '$scope',
        'ValidationPattern',
        '$uibModalInstance',
        'component'
    ];


    constructor(private $scope:IResourceInstanceViewModelScope,
                private ValidationPattern:RegExp,
                private $uibModalInstance:ng.ui.bootstrap.IModalServiceInstance,
                private component:Component) {

        this.initScope();
    }


    private initScope = ():void => {
        this.$scope.forms = {};
        this.$scope.validationPattern = this.ValidationPattern;
        this.$scope.componentInstanceModel = ComponentInstanceFactory.createComponentInstance(this.component.selectedInstance);
        this.$scope.oldName = this.component.selectedInstance.name;
        this.$scope.modalInstanceName = this.$uibModalInstance;

        this.$scope.isAlreadyPressed = false;


        this.$scope.close = ():void => {
            this.$uibModalInstance.dismiss();
        };

        this.$scope.save = ():void => {

            let onFailed = () => {
                this.$scope.isAlreadyPressed = true;
            };

            let onSuccess = (componentInstance:ComponentInstance) => {
                this.$uibModalInstance.close();
                this.$scope.isAlreadyPressed = false;
                this.$scope.componentInstanceModel = componentInstance;
                //this.component.name = componentInstance.name;//DE219124
                this.component.selectedInstance.name = componentInstance.name;
                //update requirements and capabilities owner name
                _.forEach(this.component.selectedInstance.requirements, (requirementsArray:Array<Requirement>) => {
                    _.forEach(requirementsArray, (requirement:Requirement):void => {
                        requirement.ownerName = componentInstance.name;
                    });
                });

                _.forEach(this.component.selectedInstance.capabilities, (capabilitiesArray:Array<Capability>) => {
                    _.forEach(capabilitiesArray, (capability:Capability):void => {
                        capability.ownerName = componentInstance.name;
                    });
                });

            };

            this.$scope.isAlreadyPressed = true;
            if (this.$scope.oldName != this.$scope.componentInstanceModel.name) {
                this.component.updateComponentInstance(this.$scope.componentInstanceModel).then(onSuccess, onFailed);
            }
        };

        this.$scope.footerButtons = [
            {'name': 'OK', 'css': 'blue', 'callback': this.$scope.save},
            {'name': 'Cancel', 'css': 'grey', 'callback': this.$scope.close}
        ];

        this.$scope.$watch("[forms.editNameForm.$invalid,componentInstanceModel.name,isAlreadyPressed]", (newVal, oldVal) => {
            this.$scope.footerButtons[0].disabled = this.$scope.forms.editNameForm.$invalid || this.$scope.isAlreadyPressed || this.$scope.componentInstanceModel.name === this.$scope.oldName;
        });
    }
}

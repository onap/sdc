/*
* ============LICENSE_START=======================================================
* SDC
* ================================================================================
*  Copyright (C) 2021 Nordix Foundation. All rights reserved.
*  ================================================================================
*  Licensed under the Apache License, Version 2.0 (the "License");
*  you may not use this file except in compliance with the License.
*  You may obtain a copy of the License at
*
*        http://www.apache.org/licenses/LICENSE-2.0
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*
*  SPDX-License-Identifier: Apache-2.0
*  ============LICENSE_END=========================================================
*/

import {Component, EventEmitter, Output} from '@angular/core';
import {UIInterfaceModel} from "../interface-operations.component";
import {
    InputOperationParameter,
    InterfaceOperationModel,
    IOperationParamsList
} from "../../../../../models/interfaceOperation";
import {TranslateService} from "../../../../shared/translator/translate.service";
import {IDropDownOption} from "onap-ui-angular/dist/form-elements/dropdown/dropdown-models";
import {DropdownValue} from "../../../../components/ui/form-components/dropdown/ui-element-dropdown.component";
import {ArtifactModel} from "../../../../../models/artifacts";
import {PropertyAssignment} from "../../../../../models/properties-inputs/property-assignment";
import {PropertyBEModel} from "../../../../../models/properties-inputs/property-be-model";
import {PropertyParamRowComponent} from "./property-param-row/property-param-row.component";
import {PropertyFEModel} from "../../../../../models/properties-inputs/property-fe-model";

@Component({
    selector: 'operation-handler',
    templateUrl: './interface-operation-handler.component.html',
    styleUrls: ['./interface-operation-handler.component.less'],
    providers: [TranslateService]
})

export class InterfaceOperationHandlerComponent {
    @Output('propertyChanged') emitter: EventEmitter<PropertyFEModel> = new EventEmitter<PropertyFEModel>();

    input: {
        deploymentArtifactsFilePath: Array<DropdownValue>;
        toscaArtifactTypes: Array<DropdownValue>;
        selectedInterface: UIInterfaceModel;
        selectedInterfaceOperation: InterfaceOperationModel;
        validityChangedCallback: Function;
    };

    interfaceType: string;
    artifactVersion: string;
    artifactFile: string;
    interfaceOperationName: string;
    operationToUpdate: InterfaceOperationModel;
    inputs: Array<InputOperationParameter> = [];
    properties: Array<PropertyParamRowComponent> = [];
    isLoading: boolean = false;
    readonly: boolean;

    deploymentArtifactSelected: string;
    toscaArtifactTypeSelected: string;
    toscaArtifactTypeProperties: Array<PropertyBEModel> = [];

    deploymentArtifactsFilePath: Array<DropdownValue> = [];
    toscaArtifactTypes: Array<DropdownValue> = [];

    enableAddArtifactImplementation: boolean;

    ngOnInit() {
        this.interfaceType = this.input.selectedInterface.displayType();
        this.operationToUpdate = new InterfaceOperationModel(this.input.selectedInterfaceOperation);
        this.operationToUpdate.interfaceId = this.input.selectedInterface.uniqueId;
        this.operationToUpdate.interfaceType = this.input.selectedInterface.type;
        if (!this.operationToUpdate.inputs) {
            this.operationToUpdate.inputs = new class implements IOperationParamsList {
                listToscaDataDefinition: Array<InputOperationParameter> = [];
            }
        }

        this.inputs = this.operationToUpdate.inputs.listToscaDataDefinition;
        this.removeImplementationQuote();
        this.validityChanged();

        this.deploymentArtifactsFilePath = this.input.deploymentArtifactsFilePath;
        this.toscaArtifactTypes = this.input.toscaArtifactTypes;
        this.artifactVersion = this.operationToUpdate.implementation.artifactVersion;
        this.artifactFile = this.operationToUpdate.implementation.artifactName;
        this.toscaArtifactTypeProperties = this.operationToUpdate.implementation.properties;
        console.info("Implementation on Init ", this.operationToUpdate.implementation);
        this.getArtifactTypesSelected();
    }

    onDescriptionChange= (value: any): void => {
        this.operationToUpdate.description = value;
    }

    onImplementationNameChange(value: any) {
        this.readonly = true;
        if (value) {
            let artifact = new ArtifactModel();
            artifact.artifactName = value;
            this.input.selectedInterfaceOperation.implementation = artifact;
            this.enableAddArtifactImplementation = false;
            this.readonly = false;
        }
    }

    onPropertyValueChange = (propertyValue) => {
        this.emitter.emit(propertyValue);
    }

    onMarkToAddArtifactToImplementation(event: any) {
        if (!event) {
            if (this.operationToUpdate.implementation && !this.operationToUpdate.implementation.artifactType) {
                this.toscaArtifactTypeSelected = undefined;
                this.deploymentArtifactSelected = undefined;
                this.artifactVersion = undefined;
                this.artifactFile = undefined;
            }
        }
        this.getArtifactTypesSelected();

        this.enableAddArtifactImplementation = event;
        this.validateRequiredField();
    }

    onSelectToscaArtifactType(type: IDropDownOption) {
        if (type) {
            let toscaArtifactType = type.value;
            let artifact = new ArtifactModel();
            artifact.artifactName = this.artifactFile;
            artifact.artifactVersion = this.artifactVersion;
            artifact.artifactType = toscaArtifactType.type;
            artifact.properties = toscaArtifactType.properties;
            this.toscaArtifactTypeProperties = artifact.properties;
            this.toscaArtifactTypeSelected = artifact.artifactType;
            this.operationToUpdate.implementation = artifact;
            this.getArtifactTypesSelected();
        }
        this.validateRequiredField();
    }

    onArtifactFileChange(value: any) {
        if (value) {
            this.operationToUpdate.implementation.artifactName = value;
        }
        this.validateRequiredField();
    }

    onArtifactVersionChange(value: any) {
        if (value) {
            this.operationToUpdate.implementation.artifactVersion = value;
        }
    }

    onAddInput(inputOperationParameter?: InputOperationParameter): void {
        let newInput = new InputOperationParameter(inputOperationParameter)
        newInput.type = "string";
        newInput.inputId = this.generateUniqueId();
        this.inputs.push(newInput);
        this.validityChanged();
    }

    propertyValueValidation = (propertyValue): void => {
        this.onPropertyValueChange(propertyValue);
        this.readonly = !propertyValue.isValid;
    }

    onRemoveInput = (inputParam: InputOperationParameter): void => {
        let index = this.inputs.indexOf(inputParam);
        this.inputs.splice(index, 1);
        this.validityChanged();
    }

    private removeImplementationQuote(): void {
        if (this.operationToUpdate.implementation) {
            if (!this.operationToUpdate.implementation
                || !this.operationToUpdate.implementation.artifactName) {
                return;
            }

            let implementation = this.operationToUpdate.implementation.artifactName.trim();

            if (implementation.startsWith("'") && implementation.endsWith("'")) {
                this.operationToUpdate.implementation.artifactName = implementation.slice(1, -1);
            }
        }
    }

    private generateUniqueId = (): string => {
        let result = '';
        const characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        const charactersLength = characters.length;
        for (let i = 0; i < 36; i++ ) {
            result += characters.charAt(Math.floor(Math.random() * charactersLength));
        }
        return result;
    }

    validityChanged = () => {
        let validState = this.checkFormValidForSubmit();
        this.input.validityChangedCallback(validState);
        if (validState) {
            this.readonly = false;
        }
    }

    private getArtifactTypesSelected() {
        if (this.operationToUpdate.implementation && this.operationToUpdate.implementation.artifactType
            && this.operationToUpdate.implementation.artifactName) {
            this.deploymentArtifactSelected = this.operationToUpdate.implementation.artifactName;
            this.artifactFile = this.operationToUpdate.implementation.artifactName;
            this.toscaArtifactTypeSelected = this.operationToUpdate.implementation.artifactType;
            this.enableAddArtifactImplementation = true;
        }
        this.validateRequiredField();
    }

    validateRequiredField = () => {
        this.readonly = true;
        let requiredFieldSelected = this.toscaArtifactTypeSelected && this.artifactFile ? true : false;
        this.input.validityChangedCallback(requiredFieldSelected);
        if (requiredFieldSelected) {
            this.readonly = false;
        }
    }

    private checkFormValidForSubmit = (): boolean => {
        return this.operationToUpdate.name && this.isParamsValid();
    }

    private isParamsValid = (): boolean => {
        const isInputValid = (input) => input.name && input.inputId;
        const isValid = this.inputs.every(isInputValid);
        if (!isValid) {
            this.readonly = true;
        }
        return isValid;
    }

    toDropDownOption(val: string) {
        return { value : val, label: val };
    }

}

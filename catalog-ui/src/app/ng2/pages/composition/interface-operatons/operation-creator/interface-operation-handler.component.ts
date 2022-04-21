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
import {Component, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {UIInterfaceModel} from "../interface-operations.component";
import {InputOperationParameter, InterfaceOperationModel, IOperationParamsList} from "../../../../../models/interfaceOperation";
import {TranslateService} from "../../../../shared/translator/translate.service";
import {DropdownValue} from "../../../../components/ui/form-components/dropdown/ui-element-dropdown.component";
import {ArtifactModel} from "../../../../../models/artifacts";
import {PropertyBEModel} from "../../../../../models/properties-inputs/property-be-model";
import {PropertyParamRowComponent} from "./property-param-row/property-param-row.component";
import {PropertyFEModel} from "../../../../../models/properties-inputs/property-fe-model";
import {IDropDownOption} from 'onap-ui-angular';
import {ComponentServiceNg2} from "../../../../services/component-services/component.service";
import {DropDownComponent} from "onap-ui-angular/dist/form-elements/dropdown/dropdown.component";
import {DataTypeService} from "../../../../services/data-type.service";
import {Observable} from "rxjs/Observable";
import {DataTypeModel} from "../../../../../models/data-types";

@Component({
    selector: 'operation-handler',
    templateUrl: './interface-operation-handler.component.html',
    styleUrls: ['./interface-operation-handler.component.less'],
    providers: [TranslateService]
})
export class InterfaceOperationHandlerComponent {

    @Input() private modelName: string;
    @Output('propertyChanged') emitter: EventEmitter<PropertyFEModel> = new EventEmitter<PropertyFEModel>();
    @ViewChild('interfaceOperationDropDown') interfaceOperationDropDown: DropDownComponent;

    input: {
        toscaArtifactTypes: Array<DropdownValue>;
        selectedInterface: UIInterfaceModel;
        selectedInterfaceOperation: InterfaceOperationModel;
        validityChangedCallback: Function;
        isViewOnly: boolean;
        isEdit: boolean;
    };

    dataTypeMap$: Observable<Map<string, DataTypeModel>>;
    dataTypeMap: Map<string, DataTypeModel>;
    interfaceType: string;
    artifactVersion: string;
    artifactName: string;
    interfaceOperationName: string;
    operationToUpdate: InterfaceOperationModel;
    inputs: Array<InputOperationParameter> = [];
    properties: Array<PropertyParamRowComponent> = [];
    isLoading: boolean = false;
    readonly: boolean;
    isViewOnly: boolean;
    isEdit: boolean;
    interfaceTypes: Array<DropdownValue> = [];
    interfaceTypeOptions: Array<DropDownOption> = [];
    selectedInterfaceType: DropDownOption = undefined;
    interfaceOperationMap: Map<string, Array<string>> = new Map<string, Array<string>>();
    interfaceOperationOptions: Array<DropDownOption> = [];
    selectedInterfaceOperation: DropDownOption = undefined;

    toscaArtifactTypeSelected: string;
    toscaArtifactTypeProperties: Array<PropertyBEModel> = [];
    artifactTypeProperties: Array<InputOperationParameter> = [];

    toscaArtifactTypes: Array<DropdownValue> = [];

    enableAddArtifactImplementation: boolean;

    propertyValueValid: boolean = true;
    inputTypeOptions: any[];

    constructor(private dataTypeService: DataTypeService, private componentServiceNg2: ComponentServiceNg2) {
        this.dataTypeMap$ = new Observable<Map<string, DataTypeModel>>(subscriber => {
            this.dataTypeService.findAllDataTypesByModel(this.modelName)
            .then((dataTypesMap: Map<string, DataTypeModel>) => {
                subscriber.next(dataTypesMap);
            });
        });
        this.dataTypeMap$.subscribe(value => {
            this.dataTypeMap = value;
        });

    }

    ngOnInit() {
        this.isViewOnly = this.input.isViewOnly;
        this.isEdit = this.input.isEdit;
        this.interfaceType = this.input.selectedInterface.type;
        this.operationToUpdate = this.input.selectedInterfaceOperation;
        this.operationToUpdate.interfaceId = this.input.selectedInterface.uniqueId;
        this.operationToUpdate.interfaceType = this.input.selectedInterface.type;
        this.initInputs();
        this.removeImplementationQuote();
        this.validityChanged();
        this.loadInterfaceOperationImplementation();
    }

    private initInputs() {
        if (!this.operationToUpdate.inputs) {
            this.operationToUpdate.inputs = new class implements IOperationParamsList {
                listToscaDataDefinition: Array<InputOperationParameter> = [];
            }
        }

        this.inputs = Array.from(this.operationToUpdate.inputs.listToscaDataDefinition);
        this.removeImplementationQuote();
        this.validityChanged();
        this.loadInterfaceOperationImplementation();
        this.loadInterfaceType();
    }

    private loadInterfaceType() {
        this.componentServiceNg2.getInterfaceTypesByModel(undefined)
        .subscribe(response => {
            if (response) {
                this.interfaceOperationMap = new Map<string, Array<string>>();
                for (const interfaceType of Object.keys(response).sort()) {
                    const operationList = response[interfaceType];
                    operationList.sort();
                    this.interfaceOperationMap.set(interfaceType, operationList);
                    const operationDropDownOption: DropDownOption = new DropDownOption(interfaceType);
                    this.interfaceTypeOptions.push(operationDropDownOption);
                    if (this.interfaceType == interfaceType) {
                        this.selectedInterfaceType = operationDropDownOption;
                    }
                }
                this.loadInterfaceTypeOperations();
            }
        });
    }

    loadInterfaceTypeOperations() {
        this.interfaceOperationOptions = new Array<DropDownOption>();
        const interfaceOperationList = this.interfaceOperationMap.get(this.interfaceType);

        if (interfaceOperationList) {
            interfaceOperationList.forEach(operationName => {
                const operationOption = new DropDownOption(operationName, operationName);
                this.interfaceOperationOptions.push(operationOption);
                if (this.operationToUpdate.name == operationName) {
                    this.selectedInterfaceOperation = operationOption
                }
            });
        }

        this.interfaceOperationDropDown.allOptions = this.interfaceOperationOptions;
    }

    private loadInterfaceOperationImplementation() {
        this.toscaArtifactTypes = this.input.toscaArtifactTypes;
        if (this.operationToUpdate.implementation) {
            this.artifactVersion = this.operationToUpdate.implementation.artifactVersion;
            this.artifactName = this.operationToUpdate.implementation.artifactName;
            this.toscaArtifactTypeProperties = this.operationToUpdate.implementation.properties;
        }
        this.artifactTypeProperties = this.convertArtifactsPropertiesToInput();
        this.getArtifactTypesSelected();
    }

    onDescriptionChange = (value: any): void => {
        this.operationToUpdate.description = value;
    }

    onURIChange(value: string | undefined) {
        if (value || value === '' || value === undefined) {
            this.operationToUpdate.implementation.artifactName = value === undefined ? '' : value;
        }
    }

    onPropertyValueChange = (propertyValue) => {
        this.emitter.emit(propertyValue);
    }

    onMarkToAddArtifactToImplementation(event: boolean) {
        if (!event) {
            this.toscaArtifactTypeSelected = undefined;
            this.artifactVersion = undefined;
            if (this.operationToUpdate.implementation.artifactType) {
                this.operationToUpdate.implementation.artifactName = '';
                this.operationToUpdate.implementation.artifactVersion = '';
            }
            this.toscaArtifactTypeProperties = undefined;
            this.artifactTypeProperties = undefined;
        } else {
            this.getArtifactTypesSelected();
        }
        this.enableAddArtifactImplementation = event;
        this.validateRequiredField();
    }

    onSelectToscaArtifactType(type: IDropDownOption) {
        if (type) {
            let toscaArtifactType = type.value;
            let artifact = new ArtifactModel();
            artifact.artifactName = this.operationToUpdate.implementation.artifactName;
            artifact.artifactVersion = this.operationToUpdate.implementation.artifactVersion;
            artifact.artifactType = toscaArtifactType.type;
            artifact.properties = toscaArtifactType.properties;
            this.toscaArtifactTypeProperties = artifact.properties;
            this.artifactTypeProperties = this.convertArtifactsPropertiesToInput();
            this.toscaArtifactTypeSelected = artifact.artifactType;
            this.operationToUpdate.implementation = artifact;
            this.getArtifactTypesSelected();
        }
        this.validateRequiredField();
    }

    onArtifactVersionChange(value: string | undefined) {
        if (value || value === '' || value === undefined) {
            this.operationToUpdate.implementation.artifactVersion = value === undefined ? '' : value;
        }
    }

    onAddInput(inputOperationParameter: InputOperationParameter) {
        this.addInput(inputOperationParameter);
        this.validityChanged();
    }

    propertyValueValidation = (propertyValue): void => {
        this.onPropertyValueChange(propertyValue);
        this.propertyValueValid = propertyValue.isValid;
        this.readonly = !this.propertyValueValid;
        this.validateRequiredField();
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

    validityChanged = () => {
        let validState = this.checkFormValidForSubmit();
        this.input.validityChangedCallback(validState);
        if (validState) {
            this.readonly = false;
        }
    }

    private getArtifactTypesSelected() {
        if (this.operationToUpdate.implementation && this.operationToUpdate.implementation.artifactType) {
            this.artifactName =
                this.artifactName ? this.artifactName : this.operationToUpdate.implementation.artifactName;
            this.toscaArtifactTypeSelected = this.operationToUpdate.implementation.artifactType;
            this.artifactVersion =
                this.artifactVersion ? this.artifactVersion : this.operationToUpdate.implementation.artifactVersion;
            this.toscaArtifactTypeProperties = this.operationToUpdate.implementation.properties;
            this.artifactTypeProperties = this.convertArtifactsPropertiesToInput();
            this.enableAddArtifactImplementation = true;
        }
        this.validateRequiredField();
    }

    validateRequiredField = () => {
        this.readonly = true;
        const isRequiredFieldSelected = this.isRequiredFieldsSelected();
        this.input.validityChangedCallback(isRequiredFieldSelected);
        if (isRequiredFieldSelected && this.propertyValueValid) {
            this.readonly = false;
        }
    }

    private isRequiredFieldsSelected() {
        return this.toscaArtifactTypeSelected && this.artifactName;
    }

    private checkFormValidForSubmit = (): boolean => {
        return this.operationToUpdate.name && this.artifactName && this.isParamsValid();
    }

    private isParamsValid = (): boolean => {
        const isInputValid = (input) => input.name && input.inputId && input.type;
        const isValid = this.inputs.every(isInputValid);
        if (!isValid) {
            this.readonly = true;
        }
        return isValid;
    }

    toDropDownOption(val: string) {
        return { value : val, label: val };
    }

    /**
     * Handles the input value change event.
     * @param changedInput the changed input
     */
    onInputValueChange(changedInput: InputOperationParameter) {
        if (changedInput.value instanceof Object) {
            changedInput.value = JSON.stringify(changedInput.value);
        }
        const inputOperationParameter = this.inputs.find(value => value.name == changedInput.name);
        inputOperationParameter.value = changedInput.value;
    }

    onArtifactPropertyValueChange(changedProperty: InputOperationParameter) {
        if (changedProperty.value instanceof Object) {
            changedProperty.value = JSON.stringify(changedProperty.value);
        }
        const property = this.toscaArtifactTypeProperties.find(artifactProperty => artifactProperty.name == changedProperty.name);
        property.value = changedProperty.value;
    }

    /**
     * Handles the add input event.
     * @param input the input to add
     * @private
     */
    private addInput(input: InputOperationParameter) {
        this.operationToUpdate.inputs.listToscaDataDefinition.push(input);
        this.inputs = Array.from(this.operationToUpdate.inputs.listToscaDataDefinition);
    }

    /**
     * Return a list with current input names.
     */
    collectInputNames() {
        return this.inputs.map((input) => input.name);
    }

    /**
     * Handles the delete input event.
     * @param inputName the name of the input to be deleted
     */
    onInputDelete(inputName: string) {
        const currentInputs = this.operationToUpdate.inputs.listToscaDataDefinition;
        const input1 = currentInputs.find(value => value.name === inputName);
        const indexOfInput = currentInputs.indexOf(input1);
        if (indexOfInput === -1) {
            console.error(`Could delete input '${inputName}'. Input not found.`);
            return;
        }
        currentInputs.splice(currentInputs.indexOf(input1), 1);
        this.inputs = Array.from(currentInputs);
    }

    private convertArtifactsPropertiesToInput(): Array<InputOperationParameter> {
        if (!this.toscaArtifactTypeProperties) {
            return [];
        }
        const inputList: Array<InputOperationParameter> = [];
        this.toscaArtifactTypeProperties.forEach(property => {
            const input = new InputOperationParameter();
            input.name = property.name;
            input.type = property.type;
            input.schema = property.schema;
            input.toscaDefaultValue = property.defaultValue;
            input.value = property.value;
            inputList.push(input);
        });
        return inputList;
    }

    onSelectInterface(dropDownOption: DropDownOption) {
        if (dropDownOption) {
            this.setInterfaceType(dropDownOption);
        } else {
            this.setInterfaceType(undefined);
        }
        this.setInterfaceOperation(undefined);
        this.interfaceOperationDropDown.selectOption({} as IDropDownOption);
        this.loadInterfaceTypeOperations();
    }

    onSelectOperation(dropDownOption: DropDownOption) {
        if (this.selectedInterfaceType && dropDownOption) {
            this.setInterfaceOperation(dropDownOption);
        }
    }

    private setInterfaceType(dropDownOption: DropDownOption) {
        this.selectedInterfaceType = dropDownOption ? dropDownOption : undefined;
        this.interfaceType = dropDownOption ? dropDownOption.value : undefined;
        this.operationToUpdate.interfaceType = dropDownOption ? dropDownOption.value : undefined;
        this.operationToUpdate.interfaceId = dropDownOption ? dropDownOption.value : undefined;
    }

    private setInterfaceOperation(dropDownOption: DropDownOption) {
        this.operationToUpdate.name = dropDownOption ? dropDownOption.value : undefined;
        this.operationToUpdate.operationType = dropDownOption ? dropDownOption.value : undefined;
        this.selectedInterfaceOperation = dropDownOption ? dropDownOption : undefined;
    }
}

class DropDownOption implements IDropDownOption {
    value: string;
    label: string;

    constructor(value: string, label?: string) {
        this.value = value;
        this.label = label || value;
    }
}
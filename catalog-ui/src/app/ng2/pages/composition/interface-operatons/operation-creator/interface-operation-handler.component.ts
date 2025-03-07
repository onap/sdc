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
import {Component, EventEmitter, Output, ViewChild} from '@angular/core';
import { FormControl } from '@angular/forms';
import {UIInterfaceModel} from "../interface-operations.component";
import {
    ActivityParameter,
    IActivityParameterList,
    IFilterParameterList,
    InputOperationParameter,
    InterfaceOperationModel,
    IOperationParamsList,
    FilterParameter,
    Milestone,
    MilestoneEnum
} from "../../../../../models/interfaceOperation";
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
import {InstanceFeDetails} from "../../../../../models/instance-fe-details";
import {TopologyTemplateService} from "app/ng2/services/component-services/topology-template.service";
import {CustomToscaFunction} from "../../../../../models/default-custom-functions";

@Component({
    selector: 'operation-handler',
    templateUrl: './interface-operation-handler.component.html',
    styleUrls: ['./interface-operation-handler.component.less'],
    providers: [TranslateService]
})
export class InterfaceOperationHandlerComponent {

    @Output('propertyChanged') emitter: EventEmitter<PropertyFEModel> = new EventEmitter<PropertyFEModel>();
    @ViewChild('interfaceOperationDropDown') interfaceOperationDropDown: DropDownComponent;

    input: {
        componentInstanceMap: Map<string, InstanceFeDetails>;
        toscaArtifactTypes: Array<DropdownValue>;
        selectedInterface: UIInterfaceModel;
        selectedInterfaceOperation: InterfaceOperationModel;
        validityChangedCallback: Function;
        isViewOnly: boolean;
        isEdit: boolean;
        validImplementationProps: boolean;
        validMilestoneActivities: boolean;
        validMilestoneFilters: boolean;
        modelName: string;
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
    isViewOnly: boolean;
    isEdit: boolean;
    validImplementationProps: boolean;
    validMilestoneActivities: boolean;
    validMilestoneFilters: boolean;
    interfaceTypes: Array<DropdownValue> = [];
    interfaceTypeOptions: Array<DropDownOption> = [];
    selectedInterfaceType: DropDownOption = undefined;
    interfaceOperationMap: Map<string, Array<string>> = new Map<string, Array<string>>();
    interfaceOperationOptions: Array<DropDownOption> = [];
    selectedInterfaceOperation: DropDownOption = undefined;
    modelName: string;
    toscaArtifactTypeSelected: string;
    toscaArtifactTypeProperties: Array<PropertyBEModel> = [];
    artifactTypeProperties: Array<InputOperationParameter> = [];
    toscaArtifactTypes: Array<DropdownValue> = [];
    componentInstanceMap: Map<string, InstanceFeDetails>;
    customToscaFunctions: Array<CustomToscaFunction>;
    enableAddArtifactImplementation: boolean;
    propertyValueValid: boolean = true;
    showActivities: boolean = false;
    inputTypeOptions: any[];
    timeoutValue = new FormControl('');
    timeoutType = new FormControl('');
    invalidMilestones: string[] = [];
    activeTab: string;
    milestones = Object.keys(MilestoneEnum)
            .map(key => MilestoneEnum[key]);

    constructor(private dataTypeService: DataTypeService,
                private componentServiceNg2: ComponentServiceNg2,
                private topologyTemplateService: TopologyTemplateService) {
    }

    ngOnInit() {
        this.isViewOnly = this.input.isViewOnly;
        this.isEdit = this.input.isEdit;
        this.validImplementationProps = this.input.validImplementationProps;
        this.validMilestoneActivities = this.input.validMilestoneActivities;
        this.validMilestoneFilters = this.input.validMilestoneFilters;
        this.componentInstanceMap =  this.input.componentInstanceMap ? this.input.componentInstanceMap : null;
        this.interfaceType = this.input.selectedInterface.type;
        this.operationToUpdate = new InterfaceOperationModel(this.input.selectedInterfaceOperation);
        this.operationToUpdate.interfaceId = this.input.selectedInterface.uniqueId;
        this.operationToUpdate.interfaceType = this.input.selectedInterface.type;
        this.modelName = this.input.modelName;
        this.timeoutType.setValue('sec');
        if (this.operationToUpdate.implementation && this.operationToUpdate.implementation.timeout != null) {
            this.timeoutValue.setValue(this.operationToUpdate.implementation.timeout);
            let timeout = this.timeoutValue.value / 3600;
            if (Number.isInteger(timeout)) {
                if (timeout > 23 && Number.isInteger(timeout / 24)) {
                    this.timeoutValue.setValue(timeout / 24);
                    this.timeoutType.setValue("day");
                } else {
                    this.timeoutValue.setValue(timeout);
                    this.timeoutType.setValue("hour");
                }
            } else if (Number.isInteger(timeout / 24)) {
                this.timeoutValue.setValue(timeout / 24);
                this.timeoutType.setValue("day");
            }
        }
        if (!this.operationToUpdate.milestones) {
            this.operationToUpdate.milestones = {};
        }
        this.initCustomToscaFunctions();
        this.initInputs();
        this.removeImplementationQuote();
        this.loadInterfaceOperationImplementation();

        this.dataTypeMap$ = new Observable<Map<string, DataTypeModel>>(subscriber => {
            this.dataTypeService.findAllDataTypesByModel(this.modelName)
            .then((dataTypesMap: Map<string, DataTypeModel>) => {
                subscriber.next(dataTypesMap);
                this.showActivities = dataTypesMap.has("tosca.dataTypes.tmf.milestoneJeopardyData");
            });
        });
        this.dataTypeMap$.subscribe(value => {
            this.dataTypeMap = value;
        });
    }

    private initInputs() {
        if (!this.operationToUpdate.inputs) {
            this.operationToUpdate.inputs = new class implements IOperationParamsList {
                listToscaDataDefinition: Array<InputOperationParameter> = [];
            }
        }

        this.inputs = Array.from(this.operationToUpdate.inputs.listToscaDataDefinition);
        this.removeImplementationQuote();
        this.loadInterfaceOperationImplementation();
        this.loadInterfaceType();
    }

    private initCustomToscaFunctions() {
        this.customToscaFunctions = [];
        this.topologyTemplateService.getDefaultCustomFunction().toPromise().then((data) => {
            if (data) {
                for (let customFunction of data) {
                    this.customToscaFunctions.push(new CustomToscaFunction(customFunction));
                }
            }
        });
    }

    private loadInterfaceType() {
        this.componentServiceNg2.getInterfaceTypesByModel(this.modelName)
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
        if(!this.operationToUpdate.implementation){
            let artifact = new ArtifactModel();
            this.operationToUpdate.implementation = artifact;
        }
        this.operationToUpdate.implementation.artifactName = value ? value : '';
    }

    onPropertyValueChange = (propertyValue) => {
        this.emitter.emit(propertyValue);
    }

    onMarkToAddArtifactToImplementation(event: boolean) {
        if (!event) {
            this.toscaArtifactTypeSelected = undefined;
            this.artifactVersion = undefined;
            if (this.operationToUpdate.implementation.artifactType) {
                this.operationToUpdate.implementation.artifactVersion = '';
                this.operationToUpdate.implementation.artifactType = '';
            }
            this.toscaArtifactTypeProperties = undefined;
            this.artifactTypeProperties = undefined;
        } else {
            this.getArtifactTypesSelected();
        }
        this.enableAddArtifactImplementation = event;
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
    }

    onArtifactVersionChange(value: string | undefined) {
            this.operationToUpdate.implementation.artifactVersion = value ? value : '';
    }

    onAddInput(inputOperationParameter: InputOperationParameter) {
        this.addInput(inputOperationParameter);
    }

    propertyValueValidation = (propertyValue): void => {
        this.onPropertyValueChange(propertyValue);
        this.propertyValueValid = propertyValue.isValid;
    }

    onRemoveInput = (inputParam: InputOperationParameter): void => {
        let index = this.inputs.indexOf(inputParam);
        this.inputs.splice(index, 1);
    }

    timeoutConversion = (): void => {
        let timeout = this.timeoutValue.value;
        if (timeout != null) {
            if (this.timeoutType.value == null || this.timeoutType.value == 'sec') {
                this.operationToUpdate.implementation.timeout = timeout;
                return;
            }
            if (this.timeoutType.value == 'hour') {
                this.operationToUpdate.implementation.timeout = timeout * 3600;
            } else if (this.timeoutType.value == 'day') {
                this.operationToUpdate.implementation.timeout = (timeout * 24) * 3600;
            }
        }
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
        inputOperationParameter.toscaFunction = null;
        inputOperationParameter.value = changedInput.value;
        inputOperationParameter.subPropertyToscaFunctions = changedInput.subPropertyToscaFunctions;
        if (changedInput.isToscaFunction()) {
            inputOperationParameter.toscaFunction = changedInput.toscaFunction;
            inputOperationParameter.value = changedInput.toscaFunction.buildValueString();
        }
    }

    onArtifactPropertyValueChange(changedProperty: InputOperationParameter) {
        const property = this.toscaArtifactTypeProperties.find(artifactProperty => artifactProperty.name == changedProperty.name);
        if (changedProperty.value instanceof Object) {
            changedProperty.value = JSON.stringify(changedProperty.value);
        }
        property.toscaFunction = null;
        property.value = changedProperty.value;
        if (changedProperty.isToscaFunction()) {
            property.toscaFunction = changedProperty.toscaFunction;
            property.value = changedProperty.toscaFunction.buildValueString();
        }
    }

    implementationPropsValidityChange(validImplementationProps: boolean) {
        this.validImplementationProps = validImplementationProps;
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
            console.error(`Could not delete input '${inputName}'. Input not found.`);
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
            input.toscaFunction = property.toscaFunction;
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

    getExistingFilters(key: string) {
        if (this.operationToUpdate.milestones[key] && this.operationToUpdate.milestones[key].filters) {
            return this.operationToUpdate.milestones[key].filters
        }
        return undefined;
    }

    filtersChangeEvent($event: any, milestone: string) {
        if ($event.valid) {
            if (this.invalidMilestones.indexOf(milestone) > -1) {
                this.invalidMilestones.splice(this.invalidMilestones.indexOf(milestone), 1);
                this.validMilestoneFilters = this.invalidMilestones.length < 1;
                this.validMilestoneActivities = this.invalidMilestones.length < 1;
            }
            let operationMilestone = this.operationToUpdate.milestones[milestone];
            if (!operationMilestone) {
                operationMilestone = new Milestone();
            }
            operationMilestone.filters = new class implements IFilterParameterList {
                listToscaDataDefinition: Array<FilterParameter> = [];
            }
            let milestoneFilters = $event.filters;
            for (let filter of milestoneFilters) {
                let filterParameter = new FilterParameter();
                filterParameter.constraint = filter.constraint;
                filterParameter.name = filter.name;
                filterParameter.filterValue = filter.filterValue;
                filterParameter.toscaFunction = filter.toscaFunction;
                operationMilestone.filters.listToscaDataDefinition.push(filterParameter);
            }
            this.operationToUpdate.milestones[milestone] = operationMilestone;
        } else {
            if (this.invalidMilestones.indexOf(milestone) == -1) {
                this.invalidMilestones.push(milestone);
            }
            this.validMilestoneFilters = false;
            this.validMilestoneActivities = false;
        }
    }

    getExistingActivities(key: string) {
        if (
            this.operationToUpdate.milestones[key]
            && this.operationToUpdate.milestones[key].activities
            && this.operationToUpdate.milestones[key].activities.listToscaDataDefinition
            && this.operationToUpdate.milestones[key].activities.listToscaDataDefinition.length > 0
        ) {
            return this.operationToUpdate.milestones[key].activities
        }
        return undefined;
    }

    activitiesChangeEvent($event: any, milestone: string) {
        if ($event.valid) {
            if (this.invalidMilestones.indexOf(milestone) > -1) {
                this.invalidMilestones.splice(this.invalidMilestones.indexOf(milestone), 1);
                this.validMilestoneActivities = this.invalidMilestones.length < 1;
                this.validMilestoneFilters  = this.invalidMilestones.length < 1;
            }
            let operationMilestone = this.operationToUpdate.milestones[milestone];
            if (!operationMilestone) {
                operationMilestone = new Milestone();
            }
            operationMilestone.activities = new class implements IActivityParameterList {
                listToscaDataDefinition: Array<ActivityParameter> = [];
            }
            let milestoneActivities = $event.activities;
            for (let activity of milestoneActivities) {
                let activityParameter = new ActivityParameter();
                activityParameter.type = activity.type;
                activityParameter.workflow = activity.workflow;
                activityParameter.inputs = activity.inputs;
                operationMilestone.activities.listToscaDataDefinition.push(activityParameter);
            }
            this.operationToUpdate.milestones[milestone] = operationMilestone;
        } else {
            if (this.invalidMilestones.indexOf(milestone) == -1) {
                this.invalidMilestones.push(milestone);
            }
            this.validMilestoneActivities = false;
            this.validMilestoneFilters = false;
        }
    }

    isActiveTab(title: string): boolean {
        if (this.activeTab) {
            return this.activeTab == title;
        }
        return this.milestones[0] == title;
    }

    tabChanged = (event) => {
        this.activeTab = event.title;
      }

    isInvalidActivity(title: string) {
        if (this.invalidMilestones.indexOf(title) > -1) {
            return "#cf2a2a";
        }
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
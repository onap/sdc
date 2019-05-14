/*!
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import * as _ from "lodash";
import { Component } from '@angular/core';
import {ServiceServiceNg2} from "app/ng2/services/component-services/service.service";
import {
    Service,
    ServiceInstanceObject,
    InstanceFePropertiesMap,
    InstanceBePropertiesMap,
    PropertyBEModel,
    InputBEModel,
    OperationModel,
    InterfaceModel,
    Capability
} from 'app/models';
import {ConsumptionInput, ConsumptionInputDetails, ServiceOperation} from 'app/ng2/components/logic/service-consumption/service-consumption.component';
import {PropertiesUtils} from "app/ng2/pages/properties-assignment/services/properties.utils";
import { PROPERTY_DATA } from 'app/utils';


@Component({
    selector: 'service-consumption-editor',
    templateUrl: './service-consumption-editor.component.html',
    styleUrls:['./service-consumption-editor.component.less'],
    providers: []
})

export class ServiceConsumptionCreatorComponent {

    input: {
        interfaceId: string,
        serviceOperationIndex: number,
        serviceOperations: Array<ServiceOperation>,
        parentService: Service,
        selectedService: Service,
        parentServiceInputs: Array<InputBEModel>,
        selectedServiceProperties: Array<PropertyBEModel>,
        selectedServiceInstanceId: String,
        selectedInstanceSiblings: Array<ServiceInstanceObject>,
        selectedInstanceCapabilitisList: Array<Capability>,
        siblingsCapabilitiesList: Map<string, Array<Capability>>
    };
    sourceTypes: Array<any> = [];
    serviceOperationsList: Array<ServiceOperation>;
    serviceOperation: ServiceOperation;
    currentIndex: number;
    isLoading: boolean = false;
    parentService: Service;
    selectedService: Service;
    selectedServiceInstanceId: String;
    parentServiceInputs: Array<InputBEModel>;
    selectedServiceProperties: Array<PropertyBEModel>;
    changedData: Array<ConsumptionInputDetails> = [];
    inputFePropertiesMap: any = [];

    SOURCE_TYPES = {
        STATIC: 'Static',
        SELF: 'Self',
        SERVICE_PROPERTY_LABEL: 'Service Property',
        SERVICE_INPUT_LABEL: 'Service Input'
    };

    constructor(private serviceServiceNg2: ServiceServiceNg2, private propertiesUtils:PropertiesUtils) {}

    ngOnInit() {
        this.serviceOperationsList = this.input.serviceOperations;
        this.currentIndex = this.input.serviceOperationIndex;
        this.serviceOperation = this.serviceOperationsList[this.currentIndex];
        this.parentService = this.input.parentService;
        this.selectedService = this.input.selectedService;
        this.selectedServiceInstanceId = this.input.selectedServiceInstanceId;
        this.parentServiceInputs = this.input.parentServiceInputs || [];
        this.selectedServiceProperties = this.input.selectedServiceProperties || [];
        this.initSourceTypes();
        this.initConsumptionInputs();
    }

    initSourceTypes() {
        this.sourceTypes = [
            {
                label: this.SOURCE_TYPES.STATIC,
                value: this.SOURCE_TYPES.STATIC,
                options: [],
                interfaces: [],
                capabilities: []
            },
            { label: this.SOURCE_TYPES.SELF + ' (' + this.selectedService.name + ')',
                value: this.selectedServiceInstanceId,
                options: this.selectedServiceProperties,
                interfaces: this.selectedService.interfaces,
                capabilities: this.input.selectedInstanceCapabilitisList
            },
            { label: this.parentService.name,
                value: this.parentService.uniqueId,
                options: this.parentServiceInputs,
                interfaces: [],
                capabilities: []
    }
        ];
        _.forEach(this.input.selectedInstanceSiblings, sib =>
            this.sourceTypes.push({
                label: sib.name,
                value: sib.id,
                options: _.unionBy(sib.inputs, sib.properties, 'uniqueId'),
                interfaces: sib.interfaces,
                capabilities: this.input.siblingsCapabilitiesList[sib.id]
            })
        );
    }

    onExpandCollapse(consumptionInput: ConsumptionInputDetails) {
        consumptionInput.expanded = !consumptionInput.expanded;
    }

    onExpandAll() {
        _.forEach(this.serviceOperation.consumptionInputs, coInput => {
            coInput.expanded = true;
        })
    }
    onCollapseAll() {
        _.forEach(this.serviceOperation.consumptionInputs, coInput => {
            coInput.expanded = false;
        })
    }

    isAllInputExpanded() {
        return _.every(this.serviceOperation.consumptionInputs, coInput => coInput.expanded === true);
    }
    isAllInputCollapsed() {
        return _.every(this.serviceOperation.consumptionInputs, coInput => coInput.expanded === false);
    }

    onChangePage(newIndex) {
        if (newIndex >= 0 && newIndex < this.serviceOperationsList.length) {
            this.currentIndex = newIndex;
            this.serviceOperation = this.serviceOperationsList[newIndex];
            if(!this.serviceOperation.consumptionInputs || this.serviceOperation.consumptionInputs.length === 0) {
                this.initConsumptionInputs();
            }
            this.getComplexPropertiesForCurrentInputsOfOperation(this.serviceOperation.consumptionInputs);
        }
    }

    private initConsumptionInputs() {
        this.isLoading = true;
        this.serviceServiceNg2.getServiceConsumptionInputs(this.parentService, this.selectedServiceInstanceId, this.input.interfaceId, this.serviceOperation.operation).subscribe((result: Array<ConsumptionInput>) => {
            this.isLoading = false;
            this.serviceOperation.consumptionInputs = this.analyzeCurrentConsumptionInputs(result);
            this.getComplexPropertiesForCurrentInputsOfOperation(this.serviceOperation.consumptionInputs);
        }, err=> {
            this.isLoading = false;
        });
    }

    private analyzeCurrentConsumptionInputs(result: Array<any>): Array<ConsumptionInputDetails> {
        let inputsResult: Array<ConsumptionInputDetails> = [];
        let currentOp = this.serviceOperation.operation;
        if(currentOp) {
            inputsResult = _.map(result, input => {
                let sourceVal = input.source || this.SOURCE_TYPES.STATIC;
                let consumptionInputDetails: ConsumptionInputDetails = _.cloneDeep(input);
                consumptionInputDetails.source = sourceVal;
                consumptionInputDetails.isValid = true;
                consumptionInputDetails.expanded = false;
                let filteredListsObj = this.getFilteredProps(sourceVal, input.type);
                consumptionInputDetails.assignValueLabel = this.getAssignValueLabel(sourceVal);
                consumptionInputDetails.associatedProps = filteredListsObj.associatedPropsList;
                consumptionInputDetails.associatedInterfaces = filteredListsObj.associatedInterfacesList;
                consumptionInputDetails.associatedCapabilities = filteredListsObj.associatedCapabilitiesList;
                return new ConsumptionInputDetails(consumptionInputDetails);
            });
        }
        return inputsResult;
    }

    private onSourceChanged(consumptionInput: ConsumptionInputDetails): void {
        consumptionInput.assignValueLabel = this.getAssignValueLabel(consumptionInput.source);
        let filteredListsObj = this.getFilteredProps(consumptionInput.source, consumptionInput.type);
        consumptionInput.associatedProps = filteredListsObj.associatedPropsList;
        consumptionInput.associatedInterfaces = filteredListsObj.associatedInterfacesList;
        consumptionInput.associatedCapabilities = filteredListsObj.associatedCapabilitiesList;
        if(consumptionInput.source === this.SOURCE_TYPES.STATIC) {
            if(PROPERTY_DATA.SIMPLE_TYPES.indexOf(consumptionInput.type) !== -1) {
                consumptionInput.value = consumptionInput.defaultValue || "";
            }
            else {
                consumptionInput.value = null;
                Object.assign(this.inputFePropertiesMap, this.processPropertiesOfComplexTypeInput(consumptionInput));
            }
        }
    }

    private getFilteredProps(sourceVal, inputType) {
        let currentSourceObj = this.sourceTypes.find(s => s.value === sourceVal);
        let associatedInterfacesList = [], associatedPropsList = [], associatedCapabilitiesPropsList: Array<Capability> = [];
        if(currentSourceObj) {
            if (currentSourceObj.interfaces) {
                associatedInterfacesList = this.getFilteredInterfaceOutputs(currentSourceObj, inputType);
            }
            associatedPropsList = currentSourceObj.options.reduce((result, prop) => {
                if (prop.type === inputType) {
                    result.push(prop.name);
                }
                return result;
            }, []);
            associatedCapabilitiesPropsList =
                _.reduce(currentSourceObj.capabilities,
                    (filteredCapsList, capability: Capability) => {
                        let filteredProps = _.filter(capability.properties, prop => prop.type === inputType);
                        if (filteredProps.length) {
                            let cap = new Capability(capability);
                            cap.properties = filteredProps;
                            filteredCapsList.push(cap);
                        }
                        return filteredCapsList
                    }, []);
        }
        return {
            associatedPropsList: associatedPropsList,
            associatedInterfacesList: associatedInterfacesList,
            associatedCapabilitiesList: associatedCapabilitiesPropsList
        }
    }

    private getFilteredInterfaceOutputs(currentSourceObj, inputType) {
        let currentServiceOperationId = this.serviceOperation.operation.uniqueId;
        let filteredInterfacesList = [];
        Object.keys(currentSourceObj.interfaces).map(interfId => {
            let interfaceObj: InterfaceModel = new InterfaceModel(currentSourceObj.interfaces[interfId]);
            Object.keys(interfaceObj.operations).map(opId => {
                if(currentServiceOperationId !== opId) {
                    let operationObj: OperationModel = interfaceObj.operations[opId];
                    let filteredOutputsList = _.filter(operationObj.outputs.listToscaDataDefinition, output => output.type === inputType);
                    if (filteredOutputsList.length) {
                        filteredInterfacesList.push({
                            name: `${interfaceObj.type}.${operationObj.name}`,
                            label: `${interfaceObj.displayType()}.${operationObj.name}`,
                            outputs: filteredOutputsList
                        });
                    }
                }
            });
        });
        return filteredInterfacesList;
    }

    getAssignValueLabel(selectedSource: string): string {
        if(selectedSource === this.SOURCE_TYPES.STATIC ||  selectedSource === "") {
            return this.SOURCE_TYPES.STATIC;
        }
        else {
            if(selectedSource === this.parentService.uniqueId) { //parent is the source
                return this.SOURCE_TYPES.SERVICE_INPUT_LABEL;
            }
            return this.SOURCE_TYPES.SERVICE_PROPERTY_LABEL;
        }
    }


    private isValidInputsValues(): boolean {
        return this.changedData.length > 0 && this.changedData.every((changedItem) => changedItem.isValid);
    }

    private isMandatoryFieldsValid(): boolean {
        const invalid: Array<ConsumptionInputDetails> = this.serviceOperation.consumptionInputs.filter(item =>
            item.required && (item.value === null || typeof item.value === 'undefined' || item.value === ''));
        if (invalid.length > 0) {
            return false;
        }
        return true;
    }

    checkFormValidForSubmit(): boolean {
        return this.isValidInputsValues() && this.isMandatoryFieldsValid();
    }

    checkFormValidForNavigation(): boolean {
        return this.isMandatoryFieldsValid() && (this.changedData.length === 0 || this.isValidInputsValues());
    }

    onChange(value: any, isValid: boolean, consumptionInput: ConsumptionInputDetails) {
        consumptionInput.updateValidity(isValid);
        const dataChangedIndex = this.changedData.findIndex((changedItem) => changedItem.inputId === consumptionInput.inputId);
        if (value !== consumptionInput.origVal) {
            if (dataChangedIndex === -1) {
                this.changedData.push(consumptionInput);
            }
        } else {
            if (dataChangedIndex !== -1) {
                this.changedData.splice(dataChangedIndex, 1);
            }
        }
    }

    private getComplexPropertiesForCurrentInputsOfOperation(opInputs: Array<ConsumptionInput>) {
        _.forEach(opInputs, input => {
            if(PROPERTY_DATA.SIMPLE_TYPES.indexOf(input.type) === -1 && input.source === this.SOURCE_TYPES.STATIC) {
                Object.assign(this.inputFePropertiesMap, this.processPropertiesOfComplexTypeInput(input));
            }
        });
    }

    private processPropertiesOfComplexTypeInput(input: ConsumptionInput): InstanceFePropertiesMap {
        let inputBePropertiesMap: InstanceBePropertiesMap = new InstanceBePropertiesMap();
        inputBePropertiesMap[input.name] = [input];
        let originTypeIsVF = false;
        return this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren(inputBePropertiesMap, originTypeIsVF); //create flattened children and init values
    }

    onComplexPropertyChanged(property, consumptionInput) {
        consumptionInput.value = JSON.stringify(property.valueObj);
        this.onChange(property.valueObj, property.valueObjIsValid , consumptionInput);
    }
}
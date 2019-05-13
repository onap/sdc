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

import {Component, Input, ComponentRef} from '@angular/core';
import {ComponentServiceNg2} from 'app/ng2/services/component-services/component.service';
import {ComponentInstanceServiceNg2} from 'app/ng2/services/component-instance-services/component-instance.service';
import {ServiceServiceNg2} from "app/ng2/services/component-services/service.service";
import {ModalService} from 'app/ng2/services/modal.service';
import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';
import {
    ModalModel,
    ButtonModel,
    OperationModel,
    Service,
    ServiceInstanceObject,
    PropertyFEModel,
    PropertyBEModel,
    InputBEModel,
    InterfaceModel,
    CapabilitiesGroup,
    Capability
} from 'app/models';
import {ServiceConsumptionCreatorComponent} from 'app/ng2/pages/service-consumption-editor/service-consumption-editor.component';


export class ConsumptionInput extends PropertyFEModel{
    inputId: string;
    type: string;
    source: string;
    value: any;
    constraints: Array<any>;

    constructor(input?: any) {
        super(input);
        if (input) {
            this.inputId = input.inputId;
            this.type = input.type;
            this.source = input.source;
            this.value = input.value || "";
            this.constraints = input.constraints;
        }
    }
}

export class ConsumptionInputDetails extends ConsumptionInput {
    name: string;
    expanded: boolean;
    assignValueLabel: string;
    associatedProps: Array<string>;
    associatedInterfaces: Array<any>;
    associatedCapabilities: Array<Capability>;
    origVal: string;
    isValid: boolean;

    constructor(input: any) {
        super(input);
        if (input) {
            this.name = input.name;
            this.expanded = input.expanded;
            this.assignValueLabel = input.assignValueLabel;
            this.associatedProps = input.associatedProps;
            this.associatedInterfaces = input.associatedInterfaces;
            this.associatedCapabilities = input.associatedCapabilities;
            this.origVal = input.value || "";
            this.isValid = input.isValid;
        }
    }

    public updateValidity(isValid: boolean) {
        this.isValid = isValid;
    }
}

export class ServiceOperation {
    operation: OperationModel;
    consumptionInputs: Array<ConsumptionInputDetails>;

    constructor(input?: any) {
        if (input) {
            this.operation = new OperationModel(input.operation || {});
            this.consumptionInputs = input.consumptionInputs || [];
        }
    }
}

export class InterfaceWithServiceOperation {
    interfaceId: string;
    displayName: string;
    operationsList: Array<ServiceOperation>;
    isExpanded: boolean;

    constructor(input?: InterfaceModel) {
        if (input) {
            this.interfaceId = input.uniqueId;
            this.displayName = input.displayType();
            this.operationsList = _.map(input.operations, operation => new ServiceOperation({operation: operation}));
            this.isExpanded = true;
        }
    }
}



@Component({
    selector: 'service-consumption',
    templateUrl: './service-consumption.component.html',
    styleUrls: ['service-consumption.component.less'],
    providers: [ModalService]
})

export class ServiceConsumptionComponent {

    modalInstance: ComponentRef<ModalComponent>;
    isLoading: boolean = false;
    interfacesList: Array<InterfaceWithServiceOperation>;
    operationsGroup: Array<ServiceOperation>;
    @Input() parentServiceInputs: Array<InputBEModel> = [];
    @Input() parentService: Service;
    @Input() selectedService: Service;
    @Input() selectedServiceInstanceId: string;
    @Input() instancesMappedList: Array<ServiceInstanceObject>;
    @Input() instancesCapabilitiesMap: Map<string, Array<Capability>>;
    @Input() readonly: boolean;

    selectedInstanceSiblings: Array<ServiceInstanceObject>;
    selectedInstancePropertiesList: Array<PropertyBEModel> = [];
    selectedInstanceCapabilitisList: Array<Capability> = [];

    constructor(private ModalServiceNg2: ModalService, private serviceServiceNg2: ServiceServiceNg2, private componentServiceNg2: ComponentServiceNg2, private componentInstanceServiceNg2:ComponentInstanceServiceNg2) {}

    ngOnInit() {
        this.updateSelectedInstancePropertiesAndSiblings();
        this.updateSelectedServiceCapabilities();
    }

    ngOnChanges(changes) {
        if(changes.selectedServiceInstanceId && changes.selectedServiceInstanceId.currentValue !== changes.selectedServiceInstanceId.previousValue) {
            this.selectedServiceInstanceId = changes.selectedServiceInstanceId.currentValue;
            if(changes.selectedService && changes.selectedService.currentValue !== changes.selectedService.previousValue) {
                this.selectedService = changes.selectedService.currentValue;
            }
            this.updateSelectedInstancePropertiesAndSiblings();
            this.updateSelectedServiceCapabilities();
        }
        if(changes.instancesMappedList && !_.isEqual(changes.instancesMappedList.currentValue, changes.instancesMappedList.previousValue)) {
            this.updateSelectedInstancePropertiesAndSiblings();
            this.updateSelectedServiceCapabilities();
        }
    }

    updateSelectedInstancePropertiesAndSiblings() {
        this.interfacesList = [];
        let selectedInstanceMetadata: ServiceInstanceObject = _.find(this.instancesMappedList, coInstance => coInstance.id === this.selectedServiceInstanceId);
        if (selectedInstanceMetadata) {
            _.forEach(selectedInstanceMetadata.interfaces, (interfaceData:InterfaceModel) => {
                this.interfacesList.push(new InterfaceWithServiceOperation(interfaceData));
            });
        }
        this.interfacesList.sort((interf1:InterfaceWithServiceOperation, interf2:InterfaceWithServiceOperation) => interf1.displayName.localeCompare(interf2.displayName));

        this.selectedInstancePropertiesList = selectedInstanceMetadata && selectedInstanceMetadata.properties;
        this.selectedInstanceSiblings = _.filter(this.instancesMappedList, coInstance => coInstance.id !== this.selectedServiceInstanceId);
    }

    updateSelectedServiceCapabilities() {
        this.selectedInstanceCapabilitisList = _.filter(
            CapabilitiesGroup.getFlattenedCapabilities(this.selectedService.capabilities),
            cap => cap.properties && cap.ownerId === this.selectedService.uniqueId
        );
    }

    expandCollapseInterfaceGroup(currInterface) {
        currInterface.isExpanded = !currInterface.isExpanded;
    }

    onSelectOperation(event, currInterface:InterfaceWithServiceOperation, opIndex: number) {
        event.stopPropagation();
        if(!this.readonly) {
            this.operationsGroup = currInterface.operationsList;
            let cancelButton: ButtonModel = new ButtonModel('Cancel', 'outline white', this.ModalServiceNg2.closeCurrentModal);
            let saveButton: ButtonModel = new ButtonModel('Save', 'blue', this.createOrUpdateOperationInput, this.getDisabled);
            let modalModel: ModalModel = new ModalModel('l', 'Modify Operation Consumption', '', [saveButton, cancelButton], 'standard');
            this.modalInstance = this.ModalServiceNg2.createCustomModal(modalModel);
            this.ModalServiceNg2.addDynamicContentToModal(
                this.modalInstance,
                ServiceConsumptionCreatorComponent,
                {
                    interfaceId: currInterface.interfaceId,
                    serviceOperationIndex: opIndex,
                    serviceOperations: this.operationsGroup,
                    parentService: this.parentService,
                    selectedService: this.selectedService,
                    parentServiceInputs: this.parentServiceInputs,
                    selectedServiceProperties: this.selectedInstancePropertiesList,
                    selectedServiceInstanceId: this.selectedServiceInstanceId,
                    selectedInstanceSiblings: this.selectedInstanceSiblings,
                    selectedInstanceCapabilitisList: this.selectedInstanceCapabilitisList,
                    siblingsCapabilitiesList: this.instancesCapabilitiesMap
                }
            );
            this.modalInstance.instance.open();
        }
    }

    createOrUpdateOperationInput  = ():void => {
        this.isLoading = true;
        let consumptionInputsList:Array<{[id: string]: Array<ConsumptionInput>}> = _.map(this.operationsGroup, (serviceOp) => {
            let consumptionInputsArr: Array<any> = [];
            if(serviceOp.consumptionInputs) {
                consumptionInputsArr = _.map(serviceOp.consumptionInputs, (input: ConsumptionInputDetails) => {
                    return {
                        inputId: input.inputId,
                        type: input.isSimpleType ? input.type : 'json',
                        source: input.source,
                        value: input.value
                    };
                });
            }
            return {
                [serviceOp.operation.uniqueId]: consumptionInputsArr
            };
        });
        this.serviceServiceNg2.createOrUpdateServiceConsumptionInputs(this.parentService,this.selectedServiceInstanceId, consumptionInputsList).subscribe(() => {
            this.isLoading = false;
        }, err=> {
            this.isLoading = false;
        });
        this.ModalServiceNg2.closeCurrentModal();
    };

    getDisabled = ():boolean =>  {
        return !this.modalInstance.instance.dynamicContent.instance.checkFormValidForSubmit();
    };

}
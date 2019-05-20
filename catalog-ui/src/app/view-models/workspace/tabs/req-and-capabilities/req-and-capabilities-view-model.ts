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

/**
 * Created by rcohen on 9/22/2016.
 */
'use strict';
import * as _ from "lodash";
import {ComponentRef} from '@angular/core';
import {IWorkspaceViewModelScope} from "app/view-models/workspace/workspace-view-model";
import {ModalsHandler, ResourceType} from "app/utils";
import {ComponentType} from "app/utils/constants";
import {
    Capability, PropertyModel, Requirement, Resource,
    RelationshipTypesMap, NodeTypesMap, CapabilityTypesMap
} from "app/models";
import {ComponentGenericResponse} from "app/ng2/services/responses/component-generic-response";
import {ComponentServiceNg2} from "app/ng2/services/component-services/component.service";
import {ToscaTypesServiceNg2} from "app/ng2/services/tosca-types.service";
import {ModalComponent} from 'app/ng2/components/ui/modal/modal.component';
import {ModalService} from 'app/ng2/services/modal.service';
import {RequirementsEditorComponent} from 'app/ng2/pages/req-and-capabilities-editor/requirements-editor/requirements-editor.component';
import {CapabilitiesEditorComponent} from 'app/ng2/pages/req-and-capabilities-editor/capabilities-editor/capabilities-editor.component';
import {ModalService as ModalServiceSdcUI} from "sdc-ui/lib/angular/modals/modal.service";
import {IModalConfig} from "sdc-ui/lib/angular/modals/models/modal-config";
import {ModalButtonComponent} from "sdc-ui/lib/angular/components";

export class SortTableDefined {
    reverse:boolean;
    sortByField:string;
}

class RequirementUI extends Requirement {
    isCreatedManually: boolean;

    constructor(input: Requirement, componentUniqueId: string) {
        super(input);
        this.isCreatedManually = input.ownerId === componentUniqueId;
    }
}
class CapabilityUI extends Capability {
    isCreatedManually: boolean;

    constructor(input: Capability, componentUniqueId: string) {
        super(input);
        this.isCreatedManually = input.ownerId === componentUniqueId;
    }
}

interface IReqAndCapabilitiesViewModelScope extends IWorkspaceViewModelScope {
    requirementsTableHeadersList:Array<any>;
    editableRequirementsTableHeadersList: Array<any>;
    capabilitiesTableHeadersList:Array<any>;
    editableCapabilitiesTableHeadersList: Array<any>;
    capabilityPropertiesTableHeadersList:Array<any>;
    requirementsSortTableDefined:SortTableDefined;
    capabilitiesSortTableDefined:SortTableDefined;
    propertiesSortTableDefined:SortTableDefined;
    requirements: Array<RequirementUI>;
    filteredRequirementsList: Array<RequirementUI>;
    capabilities: Array<CapabilityUI>;
    filteredCapabilitiesList: Array<CapabilityUI>;
    mode:string;
    filteredProperties:Array<Array<PropertyModel>>;
    searchText:string;
    isEditable: boolean;
    modalInstance: ComponentRef<ModalComponent>;
    filter: {txt: string; show: boolean};

    sort(sortBy: string, sortByTableDefined: SortTableDefined, autoCollapseCapabilitiesRows: boolean): void;
    sortByIsCreatedManually(arrToSort: Array<RequirementUI|CapabilityUI>): Array<any>;
    updateProperty(property:PropertyModel, indexInFilteredProperties:number):void;
    allCapabilitiesSelected(selected:boolean):void;
    onAddBtnClicked(): void;
    onEditRequirement(req: RequirementUI): void;
    onEditCapability(cap: CapabilityUI): void;
    onDeleteReq(event, req: RequirementUI): void;
    onDeleteCap(event, cap: CapabilityUI): void;
    onFilter(): void;
    isListEmpty(): boolean;
    onSwitchTab(): void;
    onSearchIconClick(): void;
    cutToscaTypePrefix(valToCut: string, textToStartCut: string): string;
    isReadonly(): boolean;
}

export class ReqAndCapabilitiesViewModel {

    static '$inject' = [
        '$scope',
        '$filter',
        'ModalsHandler',
        'ComponentServiceNg2',
        'ToscaTypesServiceNg2',
        'ModalServiceNg2',
        'ModalServiceSdcUI'
    ];


    constructor(private $scope:IReqAndCapabilitiesViewModelScope,
                private $filter:ng.IFilterService,
                private ModalsHandler:ModalsHandler,
                private ComponentServiceNg2: ComponentServiceNg2,
                private ToscaTypesServiceNg2: ToscaTypesServiceNg2,
                private ModalServiceNg2: ModalService,
                private ModalServiceSdcUI: ModalServiceSdcUI) {

        this.initCapabilitiesAndRequirements();
        this.fetchCapabilitiesRelatedData();
    }

    private initCapabilitiesAndRequirements = (): void => {

        this.$scope.isEditable = this.getIsEditableByComponentType();
        this.$scope.isLoading = true;
        this.ComponentServiceNg2.getCapabilitiesAndRequirements(this.$scope.component.componentType, this.$scope.component.uniqueId).subscribe((response: ComponentGenericResponse) => {
            this.$scope.component.capabilities = response.capabilities;
            this.$scope.component.requirements = response.requirements;
            this.initScope();
            this.$scope.isLoading = false;
        }, () => {
            this.$scope.isLoading = false;
        });

    }

    private openEditPropertyModal = (property:PropertyModel, indexInFilteredProperties:number):void => {
        //...because there is not be api
        _.forEach(this.$scope.filteredProperties[indexInFilteredProperties], (prop:PropertyModel)=> {
            prop.readonly = true;
        });
        this.ModalsHandler.openEditPropertyModal(property, this.$scope.component, this.$scope.filteredProperties[indexInFilteredProperties], false, "component", this.$scope.component.uniqueId).then(() => {

        });
    };

    private initScope = (currentMode = 'requirements'): void => {
        this.$scope.isReadonly = (): boolean => {
            return this.$scope.isViewMode() || !this.$scope.isDesigner();
        };
        this.$scope.filter = {txt: '', show: false};
        this.$scope.requirementsSortTableDefined = {
            reverse: false,
            sortByField: this.$scope.isEditable ? 'other' : 'name'
        };
        this.$scope.capabilitiesSortTableDefined = {
            reverse: false,
            sortByField: this.$scope.isEditable ? 'other' : 'name'
        };
        this.$scope.propertiesSortTableDefined = {
            reverse: false,
            sortByField: 'name'
        };

        this.$scope.setValidState(true);
        this.$scope.requirementsTableHeadersList = [
            {title: 'Name', property: 'name'},
            {title: 'Capability', property: 'capability'},
            {title: 'Node', property: 'node'},
            {title: 'Relationship', property: 'relationship'},
            {title: 'Connected To', property: ''},
            {title: 'Occurrences', property: ''}
        ];
        this.$scope.capabilitiesTableHeadersList = [
            {title: 'Name', property: 'name'},
            {title: 'Type', property: 'type'},
            {title: 'Description', property: ''},
            {title: 'Valid Source', property: ''},
            {title: 'Occurrences', property: ''}
        ];
        this.$scope.editableRequirementsTableHeadersList = [
            {title: 'Name', property: 'name'},
            {title: 'Capability', property: 'capability'},
            {title: 'Node', property: 'node'},
            {title: 'Relationship', property: 'relationship'},
            {title: 'Occurrences', property: 'occurrences'},
            {title: '●●●', property: 'other'}
        ];
        this.$scope.editableCapabilitiesTableHeadersList = [
            {title: 'Name', property: 'name'},
            {title: 'Type', property: 'type'},
            {title: 'Description', property: 'description'},
            {title: 'Valid Sources', property: 'valid-sources'},
            {title: 'Occurrences', property: 'occurrences'},
            {title: '●●●', property: 'other'}
        ];
        this.$scope.capabilityPropertiesTableHeadersList = [
            {title: 'Name', property: 'name'},
            {title: 'Type', property: 'type'},
            {title: 'Schema', property: 'schema.property.type'},
            {title: 'Description', property: 'description'},
        ];
        this.$scope.filteredProperties = [];

        this.$scope.mode = currentMode;
        this.$scope.requirements = [];
        _.forEach(this.$scope.component.requirements, (req:Array<Requirement>, capName)=> {
            let reqUIList: Array<RequirementUI> = _.map(req, reqObj => new RequirementUI(reqObj, this.$scope.component.uniqueId));
            this.$scope.requirements = this.$scope.requirements.concat(reqUIList);
        });
        this.$scope.filteredRequirementsList = this.$scope.requirements;

        this.$scope.capabilities = [];
        _.forEach(this.$scope.component.capabilities, (cap:Array<Capability>, capName)=> {
            let capUIList: Array<CapabilityUI> = _.map(cap, capObj => new CapabilityUI(capObj, this.$scope.component.uniqueId));
            this.$scope.capabilities = this.$scope.capabilities.concat(capUIList);
        });

        this.$scope.sortByIsCreatedManually = (arrToSort: Array<RequirementUI|CapabilityUI>): Array<any> => {
            return arrToSort.sort((elem1: RequirementUI|CapabilityUI, elem2: RequirementUI|CapabilityUI) => +elem2.isCreatedManually - (+elem1.isCreatedManually));
        };
        this.$scope.filteredCapabilitiesList = this.$scope.sortByIsCreatedManually(this.$scope.capabilities);
        this.$scope.filteredRequirementsList = this.$scope.sortByIsCreatedManually(this.$scope.requirements);

        this.$scope.sort = (sortBy: string, sortByTableDefined: SortTableDefined, autoCollapseCapabilitiesRows: boolean): void => {
            sortByTableDefined.reverse = (sortByTableDefined.sortByField === sortBy) ? !sortByTableDefined.reverse : false;
            sortByTableDefined.sortByField = sortBy;
            if (autoCollapseCapabilitiesRows) {
                this.$scope.allCapabilitiesSelected(false);
            }
        };

        this.$scope.updateProperty = (property:PropertyModel, indexInFilteredProperties:number):void => {
            this.openEditPropertyModal(property, indexInFilteredProperties);
        };

        this.$scope.allCapabilitiesSelected = (selected:boolean):void => {
            _.forEach(this.$scope.capabilities, (cap:Capability)=> {
                cap.selected = selected;
            });
        };
        this.$scope.onAddBtnClicked = (): void => {
            switch (this.$scope.mode) {
                case 'requirements':
                    this.openRequirementsModal();
                    break;
                case 'capabilities':
                    this.openCapabilitiesModal();
                    break;
            }
        };
        this.$scope.onEditRequirement = (req: RequirementUI): void => {
            this.openRequirementsModal(req);
        };
        this.$scope.onEditCapability = (cap: CapabilityUI): void => {
            this.openCapabilitiesModal(cap);
        };
        this.$scope.onDeleteReq = (event: Event, req: RequirementUI): void => {
            event.stopPropagation();
            this.ModalServiceSdcUI.openAlertModal('Delete Requirement',
                `Are you sure you want to delete requirement: ${req.name}?`, 'OK', () => this.deleteRequirement(req), 'Cancel');
        };
        this.$scope.onDeleteCap = (event: Event, cap: CapabilityUI): void => {
            event.stopPropagation();
            this.ModalServiceSdcUI.openAlertModal('Delete Capability',
                `Are you sure you want to delete capability: ${cap.name}?`, 'OK', () => this.deleteCapability(cap), 'Cancel');
        };
        this.$scope.onSearchIconClick = (): void => {
            this.$scope.filter.show = !!this.$scope.filter.txt || !this.$scope.filter.show;
        };
        this.$scope.onFilter = (): void => {
            switch (this.$scope.mode) {
                case 'requirements':
                    this.$scope.filteredRequirementsList = _.filter(this.$scope.requirements, req => req.name.includes(this.$scope.filter.txt));
                    break;
                case 'capabilities':
                    this.$scope.filteredCapabilitiesList = _.filter(this.$scope.capabilities, cap => cap.name.includes(this.$scope.filter.txt));
                    break;
            }
        };
        this.$scope.isListEmpty = (): boolean => {
            switch (this.$scope.mode) {
                case 'requirements':
                    return this.$scope.requirements.length === 0;
                case 'capabilities':
                    return this.$scope.capabilities.length === 0;
            }
        };
        this.$scope.onSwitchTab = (): void => {
            this.$scope.mode = this.$scope.mode === 'requirements' ? 'capabilities' : 'requirements';
            this.$scope.filter.txt = '';
            this.$scope.filter.show = false;
            this.$scope.filteredRequirementsList = this.$scope.requirements;
            this.$scope.filteredCapabilitiesList = this.$scope.capabilities;
        };
        this.$scope.cutToscaTypePrefix = (valToCut: string, textToStartCut: string): string => {
            let index = valToCut.indexOf(textToStartCut);
            return index !== -1 ? valToCut.substr(index + textToStartCut.length) : valToCut;
        };
    };

    private getIsEditableByComponentType() {
        if (this.$scope.componentType === ComponentType.SERVICE) {
            return true;
        }
        if (this.$scope.component.isResource()) {
            let componentAsResource: Resource = <Resource>this.$scope.component;
            return componentAsResource.resourceType === ResourceType.VF ||
                componentAsResource.resourceType === ResourceType.PNF;
        }
        return false;
    };

    private fetchCapabilitiesRelatedData() {
        if (this.$scope.isEditable) {
            this.$scope.capabilityTypesList = [];
            this.ToscaTypesServiceNg2.fetchCapabilityTypes().subscribe((result: CapabilityTypesMap) => {
                _.forEach(result, capabilityType => this.$scope.capabilityTypesList.push(capabilityType));
            });
            this.$scope.nodeTypesList = [];
            this.ToscaTypesServiceNg2.fetchNodeTypes().subscribe((result: NodeTypesMap) => {
                _.forEach(result, nodeType => this.$scope.nodeTypesList.push(nodeType));
            });
            this.$scope.relationshipTypesList = [];
            this.ToscaTypesServiceNg2.fetchRelationshipTypes().subscribe((result: RelationshipTypesMap) => {
                _.forEach(result, relshipType => this.$scope.relationshipTypesList.push(relshipType));
            });
        }
    }

    private openRequirementsModal(req?: RequirementUI) {
        let modalConfig: IModalConfig = {
            size: 'md',
            title: (req ? 'Update' : 'Add') + ' Requirement',
            type: 'custom',
            buttons: [
                {
                    id: 'saveButton',
                    text: (req ? 'Update' : 'Create'),
                    size: "'x-small'",
                    callback: () => this.createOrUpdateRequirement(),
                    closeModal: true
                },
                {text: "Cancel", size: "'x-small'", closeModal: true}]
        };
        let modalInputs = {
            requirement: req,
            relationshipTypesList: this.$scope.relationshipTypesList,
            nodeTypesList: this.$scope.nodeTypesList,
            capabilityTypesList: this.$scope.capabilityTypesList,
            isReadonly: this.$scope.isViewMode() || !this.$scope.isDesigner(),
            validityChangedCallback: this.getDisabled
        };

        this.ModalServiceSdcUI.openCustomModal(modalConfig, RequirementsEditorComponent, {input: modalInputs});
    }

    private openCapabilitiesModal(cap?: CapabilityUI) {
        let modalConfig: IModalConfig = {
            size: 'md',
            title: (cap ? 'Update' : 'Add') + ' Capability',
            type: 'custom',
            buttons: [
                {
                    id: 'saveButton',
                    text: (cap ? 'Update' : 'Create'),
                    size: "'x-small'",
                    callback: () => this.createOrUpdateCapability(),
                    closeModal: true
                },
                {text: "Cancel", size: "'x-small'", closeModal: true}]
        };
        let modalInputs = {
            capability: cap,
            capabilityTypesList: this.$scope.capabilityTypesList,
            isReadonly: this.$scope.isViewMode() || !this.$scope.isDesigner(),
            validityChangedCallback: this.getDisabled
        };

        this.ModalServiceSdcUI.openCustomModal(modalConfig, CapabilitiesEditorComponent, {input: modalInputs});
    }

    getDisabled = (shouldEnable: boolean): void => {
        let saveButton: ModalButtonComponent = this.ModalServiceSdcUI.getCurrentInstance().getButtonById('saveButton');
        saveButton.disabled = this.$scope.isViewMode() || !this.$scope.isDesigner() || !shouldEnable;
    };

    private createOrUpdateRequirement() {
        let requirement = this.ModalServiceSdcUI.getCurrentInstance().innerModalContent.instance.requirementData;
        this.$scope.isLoading = true;
        if (!requirement.uniqueId) {
            this.ComponentServiceNg2.createRequirement(this.$scope.component, requirement).subscribe(result => {
                this.$scope.requirements.unshift(new RequirementUI(result[0], this.$scope.component.uniqueId));
                this.$scope.isLoading = false;
            }, () => {
                this.$scope.isLoading = false;
            });
        }
        else {
            this.ComponentServiceNg2.updateRequirement(this.$scope.component, requirement).subscribe(result => {
                let index = this.$scope.requirements.findIndex(req => result[0].uniqueId === req.uniqueId);
                this.$scope.requirements[index] = new RequirementUI(result[0], this.$scope.component.uniqueId);
                this.$scope.isLoading = false;
                this.$scope.$apply();
            }, () => {
                this.$scope.isLoading = false;
            });
        }
    }

    private createOrUpdateCapability() {
        let capability = this.ModalServiceSdcUI.getCurrentInstance().innerModalContent.instance.capabilityData;
        this.$scope.isLoading = true;
        if (!capability.uniqueId) {
            this.ComponentServiceNg2.createCapability(this.$scope.component, capability).subscribe(result => {
                this.$scope.capabilities.unshift(new CapabilityUI(result[0], this.$scope.component.uniqueId));
                this.$scope.isLoading = false;
            }, () => {
                this.$scope.isLoading = false;
            });
        }
        else {
            this.ComponentServiceNg2.updateCapability(this.$scope.component, capability).subscribe(result => {
                let index = this.$scope.capabilities.findIndex(cap => result[0].uniqueId === cap.uniqueId);
                this.$scope.capabilities[index] = new CapabilityUI(result[0], this.$scope.component.uniqueId);
                this.$scope.isLoading = false;
                this.$scope.$apply();
            }, () => {
                this.$scope.isLoading = false;
            });
        }
    }

    private deleteRequirement(req) {
        this.$scope.isLoading = true;
        this.ComponentServiceNg2.deleteRequirement(this.$scope.component, req.uniqueId).subscribe(() => {
            this.ComponentServiceNg2.getCapabilitiesAndRequirements(this.$scope.componentType, this.$scope.component.uniqueId).subscribe(response => {
                this.$scope.component.requirements = response.requirements;
                this.initScope('requirements');
                this.$scope.isLoading = false;
            }, () => {
                this.$scope.isLoading = false;
            });
        }, () => {
            this.$scope.isLoading = false;
        });
    }

    private deleteCapability(cap) {
        this.$scope.isLoading = true;
        this.ComponentServiceNg2.deleteCapability(this.$scope.component, cap.uniqueId).subscribe(() => {
            this.ComponentServiceNg2.getCapabilitiesAndRequirements(this.$scope.componentType, this.$scope.component.uniqueId).subscribe(response => {
                this.$scope.component.capabilities = response.capabilities;
                this.initScope('capabilities');
                this.$scope.isLoading = false;
            }, () => {
                this.$scope.isLoading = false;
            });
        }, () => {
            this.$scope.isLoading = false;
        });
    }
}


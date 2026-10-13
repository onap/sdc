/**
 * Created by rc2122 on 9/4/2017.
 */
import { Component, EventEmitter, Input, OnInit, Output, SimpleChanges } from '@angular/core';
import { Component as ComponentModel } from 'app/models/components/component';
import { PropertyModel } from 'app/models/properties';
import { InstanceFePropertiesMap } from 'app/models/properties-inputs/property-fe-map';
import { RadioButtonModel } from 'app/models/radio-button';
import { PropertiesUtils } from 'app/pages/properties-assignment/services/properties.utils';
import { WorkspaceService } from 'app/pages/workspace/workspace.service';
import * as _ from 'lodash';
import { Dictionary } from 'lodash';
import { Capability, RequirementCapabilityModel } from '../../../models/capability';
import { Requirement } from '../../../models/requirement';
import { ComponentInstanceService } from '../../../services/component-instance-services/component-instance.service';

const REQUIREMENT = 'Requirement';
const CAPABILITY = 'Capability';

@Component({
    selector: 'select-requirement-or-capability',
    templateUrl: './select-requirement-or-capability.component.html',
    styleUrls: ['./select-reqiurement-or-capability.component.less']
})

export class SelectRequirementOrCapabilityComponent implements OnInit {

    @Input() optionalRequirementsMap: Dictionary<Requirement[]>; // optional requirement map - key is type, value is array of requirements
    @Input() optionalCapabilitiesMap: Dictionary<Capability[]>; // optional capabilities map - key is type, value is array of capabilities
    @Input() selectedReqOrCapOption: string; // the selection value chosen by the user (options: requirement / capability )
    @Input() componentInstanceId: string;
    @Input() selectedReqOrCapModel: RequirementCapabilityModel;
    @Output() updateSelectedReqOrCap: EventEmitter<RequirementCapabilityModel> = new EventEmitter<RequirementCapabilityModel>();

    types: string[] = [];
    selectedType: string;

    selectOptions: RadioButtonModel[];

    requirementsTypes: string[] = [];
    capabilitiesTypes: string[] = [];

    disabledSelectReqOrCapOption: boolean; // If we need to disable the option to choose requirement or capability
    displayCapReqListFilterByType: RequirementCapabilityModel[];

    capabilityProperties: InstanceFePropertiesMap;
    loadingCapabilityProperties: boolean;

    private _loadingCapabilityProperties: Capability[];

    constructor(private componentInstanceService: ComponentInstanceService,
                private propertiesUtils: PropertiesUtils,
                private workspaceService: WorkspaceService) {
        this.selectOptions = [new RadioButtonModel(REQUIREMENT, REQUIREMENT), new RadioButtonModel(CAPABILITY, CAPABILITY)];
        this._loadingCapabilityProperties = [];
    }

    initCapabilityPropertiesTable = (): void => {
        if (this.selectedReqOrCapModel instanceof Capability ) {
            const selectedCapability = this.selectedReqOrCapModel as Capability;
            if (selectedCapability.properties && selectedCapability.properties.length) {
                this.capabilityProperties = this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren({ CAPABILITY : selectedCapability.properties}, false);
            } else {
                this.capabilityProperties = null;
            }
        }
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes.selectedReqOrCapModel) {
            this.capabilityProperties = null;
            if (this.selectedReqOrCapModel && this.selectedReqOrCapOption === CAPABILITY) {
                this.setCapabilityProperties();
            }
        }
    }

    ngOnInit() {
        this.initTypesList();
        this.initDefaultReqOrCapSelection();
        this.initCapabilityPropertiesTable();
    }

    onTypeSelected = (): void => {
        this.initCapReqListFilterByType();
        if (this.displayCapReqListFilterByType.indexOf(this.selectedReqOrCapModel) === -1) {
            this.selectReqOrCapFromList(null);
        }
    }

    onSelectRequirementOrCapability = (): void => {
        this.types = this.selectedReqOrCapOption === REQUIREMENT ? this.requirementsTypes : this.capabilitiesTypes;
        this.selectReqOrCapFromList(null);
        this.setDefaultValueType();
    }

    private initDefaultReqOrCapSelection = (): void => {
        if (this.selectedReqOrCapOption) {// for second step
            this.disabledSelectReqOrCapOption = true;
        }
        if (this.selectedReqOrCapModel) {// init when there is selected req or cap
            if (this.selectedReqOrCapModel instanceof Capability) {
                this.selectedReqOrCapOption = this.selectOptions[1].value;
                this.selectedType = this.selectedReqOrCapModel.type;
            } else {
                this.selectedReqOrCapOption = this.selectOptions[0].value;
                this.selectedType = (this.selectedReqOrCapModel as Requirement).capability;
            }
        }
        if (Object.keys(this.optionalCapabilitiesMap).length === 0) { // If instance don't have capabilities
            this.disabledSelectReqOrCapOption = true;
            this.selectedReqOrCapOption = this.selectOptions[0].value;
        } else if (Object.keys(this.optionalRequirementsMap).length === 0) { // If instance don't have requirements
            this.disabledSelectReqOrCapOption = true;
            this.selectedReqOrCapOption = this.selectOptions[1].value;
        }
        this.selectedReqOrCapOption = this.selectedReqOrCapOption || this.selectOptions[1].value;
        this.types = this.selectedReqOrCapOption == this.selectOptions[0].value ? this.requirementsTypes : this.capabilitiesTypes;
        setTimeout(() => {
            if (this.selectedType) {
                this.initCapReqListFilterByType();
            } else {
                this.setDefaultValueType();
            }
        });
    }

    private initTypesList = (): void => {
        this.requirementsTypes = _.keys(this.optionalRequirementsMap);
        this.requirementsTypes.unshift('All');
        this.capabilitiesTypes = _.keys(this.optionalCapabilitiesMap);
        this.capabilitiesTypes.unshift('All');
    }

    private fillInDisplayCapReqListFilterByType = (allOptionalTypesMap: Dictionary<RequirementCapabilityModel[]>): void => {
        if (this.selectedType === 'All') {
            this.displayCapReqListFilterByType = [];
            _.map(allOptionalTypesMap, (reqOrCapArray: RequirementCapabilityModel[]) => {
                this.displayCapReqListFilterByType = this.displayCapReqListFilterByType.concat(reqOrCapArray);
            });
        } else {
            this.displayCapReqListFilterByType = allOptionalTypesMap[this.selectedType];
        }

        // automatically select a *single* requirement or capability:
        if (this.displayCapReqListFilterByType.length === 1) {
            const selectedReqCap: RequirementCapabilityModel = this.displayCapReqListFilterByType[0];
            this.selectReqOrCapFromList((this.selectedType === CAPABILITY) ? selectedReqCap as Capability : selectedReqCap as Requirement);
        }
    }

    private initCapReqListFilterByType = (): void => {
        if (this.selectedReqOrCapOption === CAPABILITY) {
            this.fillInDisplayCapReqListFilterByType(this.optionalCapabilitiesMap);
        } else {
            this.fillInDisplayCapReqListFilterByType(this.optionalRequirementsMap);
        }
    }

    private setDefaultValueType = (): void => {
        // automatically select a *single* type from the list:
        this.selectedType = (this.types.length === 2) ? this.types[1] : this.types[0];
        this.initCapReqListFilterByType();
    }

    private selectReqOrCapFromList = (selected: Requirement|Capability): void => {
        if (this.selectedReqOrCapModel !== selected) {
            this.selectedReqOrCapModel = selected;
            this.updateSelectedReqOrCap.emit(selected);
        }
    }

    private setCapabilityProperties = (): void => {
        const selectedCapability = this.selectedReqOrCapModel as Capability;
        if (!selectedCapability.properties) {
            this.loadingCapabilityProperties = true;
            if (this._loadingCapabilityProperties.indexOf(selectedCapability) == -1) {
                this._loadingCapabilityProperties.push(selectedCapability);
                this.componentInstanceService.getInstanceCapabilityProperties(this.workspaceService.metadata.componentType, this.workspaceService.metadata.uniqueId, this.componentInstanceId, selectedCapability)
                    .subscribe((response: PropertyModel[]) => {
                        if (this.selectedReqOrCapModel === selectedCapability) {
                            delete this.loadingCapabilityProperties;
                        }
                        this.initCapabilityPropertiesTable();
                    }, (error) => {
                        if (this.selectedReqOrCapModel === selectedCapability) {
                            delete this.loadingCapabilityProperties;
                        }
                    }, () => {
                        this._loadingCapabilityProperties.splice(this._loadingCapabilityProperties.indexOf(selectedCapability), 1);
                    });
            }
        } else {
            delete this.loadingCapabilityProperties;
            this.initCapabilityPropertiesTable();
        }
    }
}

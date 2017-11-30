/**
 * Created by rc2122 on 9/4/2017.
 */
import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {RadioButtonModel, Match, PropertyModel, InstanceFePropertiesMap, Component as ComponentModel} from "app/models";
import {Dictionary} from "lodash";
import {DropdownValue} from "../../ui/form-components/dropdown/ui-element-dropdown.component";
import {ComponentInstanceServiceNg2} from "../../../services/component-instance-services/component-instance.service";
import {PropertiesUtils} from "app/ng2/pages/properties-assignment/services/properties.utils";
import {Requirement} from "../../../../models/requirement";
import {Capability, RequirementCapabilityModel} from "../../../../models/capability";

const REQUIREMENT = 'Requirement';
const CAPABILITY = 'Capability';

@Component({
    selector: 'select-requirement-or-capability',
    templateUrl: './select-requirement-or-capability.component.html',
    styleUrls: ['./select-reqiurement-or-capability.component.less']
})

export class SelectRequirementOrCapabilityComponent implements OnInit {


    @Input() optionalRequirementsMap:Dictionary<Requirement[]>; //optional requirement map - key is type, value is array of requirements
    @Input() optionalCapabilitiesMap:Dictionary<Capability[]>; //optional capabilities map - key is type, value is array of capabilities

    @Input() selectedReqOrCapOption:string; // the selection value chosen by the user (options: requirement / capability )

    @Input() currentComponent:ComponentModel;
    @Input() componentInstanceId:string;

    @Input() selectedReqOrCapModel:RequirementCapabilityModel;

    @Output() updateSelectedReqOrCap:EventEmitter<RequirementCapabilityModel> = new EventEmitter<RequirementCapabilityModel>();
    @Output() updateCapabilityProperties:EventEmitter<Array<PropertyModel>> = new EventEmitter<Array<PropertyModel>>();

    types:Array<string> = [];
    selectedType:string;

    selectOptions:Array<RadioButtonModel>;

    requirementsTypes:Array<string> = [];
    capabilitiesTypes:Array<string> = [];

    disabledSelectReqOrCapOption: boolean; // If we need to disable the option to choose requirement or capability
    displayCapReqListFilterByType:RequirementCapabilityModel[];

    capabilityProperties:InstanceFePropertiesMap;

    constructor(private componentInstanceServiceNg2:ComponentInstanceServiceNg2,
                private propertiesUtils:PropertiesUtils) {
        this.selectOptions = [new RadioButtonModel(REQUIREMENT, REQUIREMENT), new RadioButtonModel(CAPABILITY, CAPABILITY)];
    }

    private initDefaultReqOrCapSelection = (): void => {
        if(this.selectedReqOrCapOption){//for second step
            this.disabledSelectReqOrCapOption = true;
        }
        if (this.selectedReqOrCapModel) {//init when there is selected req or cap
            if (this.selectedReqOrCapModel instanceof Capability) {
                this.selectedReqOrCapOption = this.selectOptions[1].value;
                this.selectedType = this.selectedReqOrCapModel.type;
            } else {
                this.selectedReqOrCapOption = this.selectOptions[0].value;
                this.selectedType = (<Requirement>this.selectedReqOrCapModel).capability;
            }
        }
        if(Object.keys(this.optionalCapabilitiesMap).length === 0) { // If instance don't have capabilities
            this.disabledSelectReqOrCapOption = true;
            this.selectedReqOrCapOption = this.selectOptions[0].value;
        } else if(Object.keys(this.optionalRequirementsMap).length === 0) { // If instance don't have requirements
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

    initCapabilityPropertiesTable = ():void => {
        if(this.selectedReqOrCapModel instanceof Capability ) {
            let selectedCapability = <Capability>this.selectedReqOrCapModel;
            if(selectedCapability.properties){
                this.capabilityProperties = this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren({ CAPABILITY : selectedCapability.properties}, false);
            }
        }
    }

    ngOnChanges(changes:SimpleChanges) {
        if (changes.selectedReqOrCapModel) {
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

    private initTypesList = ():void => {
        this.requirementsTypes = _.keys(this.optionalRequirementsMap);
        this.requirementsTypes.unshift('All');
        this.capabilitiesTypes = _.keys(this.optionalCapabilitiesMap);
        this.capabilitiesTypes.unshift('All');
    }

    private fillInDisplayCapReqListFilterByType = (allOptionalTypesMap:Dictionary<RequirementCapabilityModel[]>):void => {
        if(this.selectedType === 'All'){
            this.displayCapReqListFilterByType = [];
            _.map(allOptionalTypesMap,(reqOrCapArray:RequirementCapabilityModel[])=>{
                this.displayCapReqListFilterByType = this.displayCapReqListFilterByType.concat(reqOrCapArray);
            })
        }else{
            this.displayCapReqListFilterByType = allOptionalTypesMap[this.selectedType];
        }

        // automatically select a *single* requirement or capability:
        if (this.displayCapReqListFilterByType.length === 1) {
            const selectedReqCap:RequirementCapabilityModel = this.displayCapReqListFilterByType[0];
            this.selectReqOrCapFromList((this.selectedType === CAPABILITY) ? <Capability>selectedReqCap : <Requirement>selectedReqCap);
        }
    }
    
    private initCapReqListFilterByType = ():void => {
        if (this.selectedReqOrCapOption === CAPABILITY) {
            this.fillInDisplayCapReqListFilterByType(this.optionalCapabilitiesMap);
        } else {
            this.fillInDisplayCapReqListFilterByType(this.optionalRequirementsMap);
        }
    }

    private onTypeSelected = ():void => {
        this.initCapReqListFilterByType();
        if (this.displayCapReqListFilterByType.indexOf(this.selectedReqOrCapModel) === -1) {
            this.selectReqOrCapFromList(null);
        }
    }

    private setDefaultValueType = ():void =>{
        // automatically select a *single* type from the list:
        this.selectedType = (this.types.length === 2) ? this.types[1] : this.types[0];
        this.initCapReqListFilterByType();
    }
    
    private onSelectRequirementOrCapability = ():void => {
        this.types = this.selectedReqOrCapOption === REQUIREMENT ? this.requirementsTypes : this.capabilitiesTypes;
        this.selectReqOrCapFromList(null);
        this.setDefaultValueType();
    }

    private selectReqOrCapFromList = (selected:Requirement|Capability):void => {
        if (this.selectedReqOrCapModel !== selected) {
            this.selectedReqOrCapModel = selected;
            this.updateSelectedReqOrCap.emit(selected);
        }
    }


    private setCapabilityProperties = ():void => {
        let selectedCapability = <Capability>this.selectedReqOrCapModel;
        if (selectedCapability.properties === undefined) {
            this.componentInstanceServiceNg2.getInstanceCapabilityProperties(this.currentComponent, this.componentInstanceId, selectedCapability.type, selectedCapability.name)
                .subscribe((response:Array<PropertyModel>) => {
                    this.capabilityProperties = (response && response.length) ? this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren({CAPABILITY : response}, false) : null;
                    this.updateCapabilityProperties.emit(response);
                }, error => {});
        }else{
            this.capabilityProperties = this.propertiesUtils.convertPropertiesMapToFEAndCreateChildren({CAPABILITY : selectedCapability.properties}, false);
            this.updateCapabilityProperties.emit(selectedCapability.properties);
        }
    }
}

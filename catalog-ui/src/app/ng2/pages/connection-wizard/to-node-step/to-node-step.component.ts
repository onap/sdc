import {Component, forwardRef, Inject} from '@angular/core';
import {IStepComponent} from "app/models"
import {Dictionary} from "lodash";
import {ConnectionWizardService} from "../connection-wizard.service";
import {Match} from "../../../../models/graph/match-relation";
import {Requirement} from "../../../../models/requirement";
import {Capability} from "../../../../models/capability";
import {PropertyModel} from "../../../../models/properties";

@Component({
    selector: 'to-node-step',
    templateUrl: './to-node-step.component.html'
})

export class ToNodeStepComponent implements IStepComponent{

    displayRequirementsOrCapabilities:string; //get 'Requirement' or 'Capability'
    optionalRequirementsMap: Dictionary<Requirement[]> = {};
    optionalCapabilitiesMap: Dictionary<Capability[]> ={};

    constructor(@Inject(forwardRef(() => ConnectionWizardService)) public connectWizardService: ConnectionWizardService) {
    }

    ngOnInit(){
        if(this.connectWizardService.selectedMatch.isFromTo){
            this.displayRequirementsOrCapabilities = 'Capability';
            this.optionalRequirementsMap = {};
            this.optionalCapabilitiesMap = this.connectWizardService.getOptionalCapabilitiesByInstanceUniqueId(true, this.connectWizardService.selectedMatch.requirement);
        }else{
            this.displayRequirementsOrCapabilities = 'Requirement';
            this.optionalRequirementsMap = this.connectWizardService.getOptionalRequirementsByInstanceUniqueId(false, this.connectWizardService.selectedMatch.capability);
            this.optionalCapabilitiesMap = {}
        }


    }

    preventNext = ():boolean => {
        return !this.connectWizardService.selectedMatch.capability || !this.connectWizardService.selectedMatch.requirement;
    }

    preventBack = ():boolean => {
        return false;
    }

    onCapabilityPropertiesUpdate(capabilityProperties:Array<PropertyModel>) {
        this.connectWizardService.selectedMatch.capabilityProperties = capabilityProperties;
    }

    private updateSelectedReqOrCap = (selected:Requirement|Capability):void => {
        if (!selected) {
            if (this.connectWizardService.selectedMatch.isFromTo) {
                this.connectWizardService.selectedMatch.capability = undefined;
                this.connectWizardService.selectedMatch.toNode = undefined;
            } else {
                this.connectWizardService.selectedMatch.requirement = undefined;
                this.connectWizardService.selectedMatch.fromNode = undefined;
            }
        } else if (selected instanceof Requirement) {
            this.connectWizardService.selectedMatch.requirement = <Requirement>selected;
            this.connectWizardService.selectedMatch.fromNode = this.connectWizardService.connectRelationModel.toNode.componentInstance.uniqueId;
        } else {
            this.connectWizardService.selectedMatch.capability = <Capability>selected;
            this.connectWizardService.selectedMatch.toNode = this.connectWizardService.connectRelationModel.toNode.componentInstance.uniqueId;
        }
        this.connectWizardService.selectedMatch.relationship = undefined;
    }

}

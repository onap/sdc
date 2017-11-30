import {Component, OnInit, Inject, forwardRef} from "@angular/core";
import {IStepComponent} from "../../../../models/wizard-step";
import {Dictionary} from "lodash";
import { Match} from "app/models";
import {ConnectionWizardService} from "../connection-wizard.service";
import {Requirement} from "../../../../models/requirement";
import {Capability} from "../../../../models/capability";
import {PropertyModel} from "../../../../models/properties";

@Component({
    selector: 'from-node-step',
    templateUrl: './from-node-step.component.html'
})

export class FromNodeStepComponent implements IStepComponent, OnInit{

    constructor(@Inject(forwardRef(() => ConnectionWizardService)) public connectWizardService: ConnectionWizardService) {}

    optionalRequirementsMap: Dictionary<Requirement[]>;
    optionalCapabilitiesMap: Dictionary<Capability[]>;

    ngOnInit(){
        this.optionalRequirementsMap = this.connectWizardService.getOptionalRequirementsByInstanceUniqueId(true);
        this.optionalCapabilitiesMap = this.connectWizardService.getOptionalCapabilitiesByInstanceUniqueId(false);
    }

    preventNext = ():boolean => {
        return !this.connectWizardService.selectedMatch || (!this.connectWizardService.selectedMatch.capability && !this.connectWizardService.selectedMatch.requirement);
    }

    preventBack = ():boolean => {
        return true;
    }

    onCapabilityPropertiesUpdate(capabilityProperties:Array<PropertyModel>) {
        this.connectWizardService.selectedMatch.capabilityProperties = capabilityProperties;
    }

    private updateSelectedReqOrCap = (selected:Requirement|Capability):void => {
        if(!selected){
            this.connectWizardService.selectedMatch = null;
        } else if(selected instanceof Requirement){
            this.connectWizardService.selectedMatch = new Match(<Requirement>selected, null, true, this.connectWizardService.connectRelationModel.fromNode.componentInstance.uniqueId, null);
        } else{
            this.connectWizardService.selectedMatch = new Match(null,<Capability>selected , false, null, this.connectWizardService.connectRelationModel.fromNode.componentInstance.uniqueId);
        }
    }

}

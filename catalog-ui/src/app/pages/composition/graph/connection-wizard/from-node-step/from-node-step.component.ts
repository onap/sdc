import { Component, forwardRef, Inject, OnInit } from '@angular/core';
import { Capability } from 'app/models/capability';
import { Match } from 'app/models/graph/match-relation';
import { Requirement } from 'app/models/requirement';
import { IStepComponent } from 'app/models/wizard-step';
import { Dictionary } from 'lodash';
import { ConnectionWizardService } from '../connection-wizard.service';

@Component({
    selector: 'from-node-step',
    templateUrl: './from-node-step.component.html'
})

export class FromNodeStepComponent implements IStepComponent, OnInit {

    optionalRequirementsMap: Dictionary<Requirement[]>;
    optionalCapabilitiesMap: Dictionary<Capability[]>;

    constructor(@Inject(forwardRef(() => ConnectionWizardService)) public connectWizardService: ConnectionWizardService) {}

    ngOnInit() {
        this.optionalRequirementsMap = this.connectWizardService.getOptionalRequirementsByInstanceUniqueId(true);
        this.optionalCapabilitiesMap = this.connectWizardService.getOptionalCapabilitiesByInstanceUniqueId(false);
    }

    preventNext = (): boolean => {
        return !this.connectWizardService.selectedMatch || (!this.connectWizardService.selectedMatch.capability && !this.connectWizardService.selectedMatch.requirement);
    }

    preventBack = (): boolean => {
        return true;
    }

    updateSelectedReqOrCap = (selected: Requirement|Capability): void => {
        if (!selected) {
            this.connectWizardService.selectedMatch = null;
        } else if (selected instanceof Requirement) {
            this.connectWizardService.selectedMatch = new Match(selected as Requirement, null, true, this.connectWizardService.connectRelationModel.fromNode.componentInstance.uniqueId, null);
        } else {
            this.connectWizardService.selectedMatch = new Match(null, selected as Capability , false, null, this.connectWizardService.connectRelationModel.fromNode.componentInstance.uniqueId);
        }
    }

}

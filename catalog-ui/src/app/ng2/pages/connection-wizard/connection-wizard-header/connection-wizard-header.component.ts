/**
 * Created by rc2122 on 9/27/2017.
 */
import {Component, Inject, forwardRef} from "@angular/core";
import {ConnectionWizardService} from "../connection-wizard.service";
import {WizardHeaderBaseComponent} from "app/ng2/components/ui/multi-steps-wizard/multi-steps-wizard-header-base.component";

@Component({
    selector: 'connection-wizard-header',
    templateUrl: './connection-wizard-header.component.html',
    styleUrls:['./connection-wizard-header.component.less']
})

export class ConnectionWizardHeaderComponent extends WizardHeaderBaseComponent{

    constructor(@Inject(forwardRef(() => ConnectionWizardService)) public connectWizardService: ConnectionWizardService) {
        super();
    }

    private getSelectedReqOrCapName = (isFromNode:boolean):string => {
        if(!this.connectWizardService.selectedMatch){
            return '';
        }
        if(this.connectWizardService.selectedMatch.isFromTo){
            if(isFromNode){
                return this.connectWizardService.selectedMatch.relationship.requirement;
            }
            return this.connectWizardService.selectedMatch.relationship.capability ? this.connectWizardService.selectedMatch.relationship.capability : '';
        }
        if(isFromNode){
            return this.connectWizardService.selectedMatch.relationship.capability;
        }
        return this.connectWizardService.selectedMatch.relationship.requirement ? this.connectWizardService.selectedMatch.relationship.requirement : '';
    }
}


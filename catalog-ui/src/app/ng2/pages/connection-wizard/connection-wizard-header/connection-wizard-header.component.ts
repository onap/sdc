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

    private _getReqOrCapName(isFromNode:boolean) {
        const attributeReqOrCap:string = isFromNode ? 'requirement' : 'capability';
        if (this.connectWizardService.selectedMatch[attributeReqOrCap]) {
            return this.connectWizardService.selectedMatch[attributeReqOrCap].getTitle();
        } else if (this.connectWizardService.selectedMatch.relationship) {
            return this.connectWizardService.selectedMatch.relationship.relation[attributeReqOrCap];
        }
        return '';
    }

    private getSelectedReqOrCapName = (isFromNode:boolean):string => {
        if(!this.connectWizardService.selectedMatch){
            return '';
        }
        return this._getReqOrCapName(this.connectWizardService.selectedMatch.isFromTo ? isFromNode : !isFromNode);
    }
}


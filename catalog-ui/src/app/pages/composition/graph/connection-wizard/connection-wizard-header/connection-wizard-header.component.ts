/**
 * Created by rc2122 on 9/27/2017.
 */
import { Component, forwardRef, Inject } from '@angular/core';
import { WizardHeaderBaseComponent } from 'app/components/ui/multi-steps-wizard/multi-steps-wizard-header-base.component';
import { ConnectionWizardService } from '../connection-wizard.service';

@Component({
    selector: 'connection-wizard-header',
    templateUrl: './connection-wizard-header.component.html',
    styleUrls: ['./connection-wizard-header.component.less']
})

export class ConnectionWizardHeaderComponent extends WizardHeaderBaseComponent {

    constructor(@Inject(forwardRef(() => ConnectionWizardService)) public connectWizardService: ConnectionWizardService) {
        super();
    }

    public getSelectedReqOrCapName = (isFromNode: boolean): string => {
        if (!this.connectWizardService.selectedMatch) {
            return '';
        }
        return this._getReqOrCapName(this.connectWizardService.selectedMatch.isFromTo ? isFromNode : !isFromNode);
    }

    private _getReqOrCapName(isFromNode: boolean) {
        const attributeReqOrCap: string = isFromNode ? 'requirement' : 'capability';
        if (this.connectWizardService.selectedMatch[attributeReqOrCap]) {
            return this.connectWizardService.selectedMatch[attributeReqOrCap].getTitle();
        } else if (this.connectWizardService.selectedMatch.relationship) {
            return this.connectWizardService.selectedMatch.relationship.relation[attributeReqOrCap];
        }
        return '';
    }
}

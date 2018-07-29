/**
 * Created by ob0695 on 4/24/2018.
 */
import {Component, Input} from "@angular/core";
import Dictionary = _.Dictionary;
import {AutomatedUpgradeStatusResponse} from "../../../services/responses/automated-upgrade-response";
import {ServiceContainerToUpgradeUiObject} from "../automated-upgrade-models/ui-component-to-upgrade";

@Component({
    selector: 'automated-upgrade-status',
    templateUrl: './automated-upgrade-status.component.html',
    styleUrls: ['./../automated-upgrade.component.less']
})
export class AutomatedUpgradeStatusComponent {

    @Input() upgradedComponentsList: Array<ServiceContainerToUpgradeUiObject>;
    @Input() upgradeStatusMap: Dictionary<AutomatedUpgradeStatusResponse>;
    @Input() statusText: string;

    constructor () {

    }

}

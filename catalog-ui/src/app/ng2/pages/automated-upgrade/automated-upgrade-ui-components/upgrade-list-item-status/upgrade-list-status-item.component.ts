import {Component, Input} from "@angular/core";
import {AutomatedUpgradeStatusResponse} from "../../../../services/responses/automated-upgrade-response";
import {ServiceContainerToUpgradeUiObject} from "../../automated-upgrade-models/ui-component-to-upgrade";

@Component({
    selector: 'upgrade-list-status-item',
    templateUrl: './upgrade-list-status-item.component.html',
    styleUrls: ['./../upgrade-list-item.component.less']
})
export class UpgradeListItemStatusComponent {

    @Input() upgradedComponent: ServiceContainerToUpgradeUiObject;
    @Input() upgradeComponentStatus: AutomatedUpgradeStatusResponse;

    constructor () {

    }

}

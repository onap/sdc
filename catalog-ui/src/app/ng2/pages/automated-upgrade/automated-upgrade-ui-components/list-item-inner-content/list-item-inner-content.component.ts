/**
 * Created by ob0695 on 5/7/2018.
 */
/**
 * Created by ob0695 on 5/2/2018.
 */
import {Component, Input} from "@angular/core";
import {
    VspInstanceUiObject,
    AutomatedUpgradeInstanceType
} from "../../automated-upgrade-models/ui-component-to-upgrade";


@Component({
    selector: 'upgrade-list-item-inner-content',
    templateUrl: './list-item-inner-content.component.html',
    styleUrls: ['./list-item-inner-content.component.less']
})
export class UpgradeListItemInnerContent {

    @Input() vspInstances:Array<VspInstanceUiObject>;

    automatedUpgradeType = AutomatedUpgradeInstanceType;
}

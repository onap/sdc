/**
 * Created by ob0695 on 4/18/2018.
 */
import { NgModule } from "@angular/core";
import { SdcUiComponentsModule } from "onap-ui-angular";
import { CommonModule } from "@angular/common";
import { AutomatedUpgradeStatusComponent } from "./automated-upgrade-status/automated-upgrade-status.component";
import { AutomatedUpgradeComponent } from "./automated-upgrade.component";
import { UpgradeListItemComponent } from "./automated-upgrade-ui-components/upgrade-list-item/upgrade-list-item.component";
import { UpgradeListItemStatusComponent } from "./automated-upgrade-ui-components/upgrade-list-item-status/upgrade-list-status-item.component";
import { UpgradeListItemInnerContent } from "./automated-upgrade-ui-components/list-item-inner-content/list-item-inner-content.component";
import { UpgradeLineItemComponent } from "./automated-upgrade-ui-components/upgrade-line-item/upgrade-line-item.component";
import { UpgradeListItemOrderPipe } from "./automated-upgrade-ui-components/list-item-order-pipe/list-item-order-pipe";

@NgModule({
    declarations: [
        AutomatedUpgradeStatusComponent,
        UpgradeListItemComponent,
        UpgradeListItemStatusComponent,
        AutomatedUpgradeComponent,
        UpgradeListItemInnerContent,
        UpgradeLineItemComponent,
        UpgradeListItemOrderPipe
    ],
    imports: [CommonModule, SdcUiComponentsModule],
    exports: [],
    entryComponents: [
        AutomatedUpgradeComponent, AutomatedUpgradeStatusComponent
    ]
})
export class AutomatedUpgradeModule {
}   
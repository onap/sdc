import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { ZoneContainerComponent } from "./zone-container.component";
import { ZoneInstanceComponent } from "./zone-instance/zone-instance.component";
import { SdcUiComponentsModule } from "onap-ui-angular";

@NgModule({
    declarations: [ZoneContainerComponent, ZoneInstanceComponent],
    imports: [CommonModule, SdcUiComponentsModule],
    entryComponents: [ZoneContainerComponent, ZoneInstanceComponent],
    exports: [ZoneContainerComponent, ZoneInstanceComponent],
    providers: []
})
export class ZoneModules {
}
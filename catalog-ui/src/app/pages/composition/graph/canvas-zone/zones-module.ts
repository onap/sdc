import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { ZoneContainerComponent } from './zone-container.component';
import { ZoneInstanceComponent } from './zone-instance/zone-instance.component';

@NgModule({
    declarations: [ZoneContainerComponent, ZoneInstanceComponent],
    imports: [CommonModule, SdcUiComponentsModule],
    entryComponents: [ZoneContainerComponent, ZoneInstanceComponent],
    exports: [ZoneContainerComponent, ZoneInstanceComponent],
    providers: []
})
export class ZoneModules {
}

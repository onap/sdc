import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { DistributionComponentArtifactTableComponent } from './distribution-component-table/distribution-component-artifact-table/distribution-component-artifact-table.component';
import { DistributionComponentTableComponent } from './distribution-component-table/distribution-component-table.component';
import { DistributionComponent } from './distribution.component';
import { DistributionService } from './distribution.service';

@NgModule({
    declarations: [
        DistributionComponent,
        DistributionComponentTableComponent,
        DistributionComponentArtifactTableComponent,
    ],
    imports: [
        // TranslateModule,
        CommonModule,
        SdcUiComponentsModule,
        NgxDatatableModule,
    ],
    exports: [
        DistributionComponent,
        DistributionComponentTableComponent
    ],
    entryComponents: [
        DistributionComponent,
        DistributionComponentTableComponent,
        DistributionComponentArtifactTableComponent
    ],
    providers: [DistributionService]
})
export class DistributionModule {
}

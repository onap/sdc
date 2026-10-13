import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { GlobalPipesModule } from '../../../pipes/global-pipes.module';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';
import { TranslateModule } from '../../../shared/translator/translate.module';
import { AttributeModalComponent } from './attribute-modal.component';
import { AttributesComponent } from './attributes.component';

@NgModule({
    declarations: [
        AttributesComponent,
        AttributeModalComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        GlobalPipesModule,
        NgxDatatableModule,
        TranslateModule
    ],
    exports: [
        AttributesComponent
    ],
    entryComponents: [
        AttributesComponent, AttributeModalComponent
    ],
    providers: [TopologyTemplateService]
})
export class AttributesModule {
}

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { GlobalPipesModule } from '../../../pipes/global-pipes.module';
import { AttributesComponent } from './attributes.component';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { TopologyTemplateService } from '../../../services/component-services/topology-template.service';
import { AttributeModalComponent } from './attribute-modal.component';
import { TranslateModule } from '../../../shared/translator/translate.module';

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

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { LayoutModule } from '../../components/layout/layout.module';
import { ModalImportTypeComponent } from '../../components/ui/modal-import-type/modal-import-type.component';
import { UiElementsModule } from '../../components/ui/ui-elements.module';
import { GlobalPipesModule } from '../../pipes/global-pipes.module';
import { ResourceService } from '../../services/component-services/resource.service';
import { TranslateModule } from '../../shared/translator/translate.module';
import { HomeComponent } from './home.component';

@NgModule({
    declarations: [
        HomeComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        LayoutModule,
        UiElementsModule,
        GlobalPipesModule,
        TranslateModule

    ],
    exports: [
        HomeComponent
    ],
    entryComponents: [
        HomeComponent,
        ModalImportTypeComponent
    ],
    providers: [ResourceService]
})
export class HomeModule {
}

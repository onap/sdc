import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { UiElementsModule } from 'app/components/ui/ui-elements.module';
import { TranslateModule } from 'app/shared/translator/translate.module';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { InterfaceOperationComponent } from './interface-operation.page.component';

@NgModule({
    declarations: [
        InterfaceOperationComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        UiElementsModule,
        TranslateModule
    ],
    exports: [],
    entryComponents: [
        InterfaceOperationComponent
    ],
    providers: []
})

export class InterfaceOperationModule {}

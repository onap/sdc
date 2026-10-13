import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FormElementsModule } from 'app/components/ui/form-components/form-elements.module';
import { UiElementsModule } from 'app/components/ui/ui-elements.module';
import { SdcUiComponentsModule } from 'onap-ui-angular/dist';
import { TranslateModule } from '../../../shared/translator/translate.module';
import { PropertyCreatorComponent } from './property-creator.component';

@NgModule({
  declarations: [
    PropertyCreatorComponent,
  ],
  imports: [
    CommonModule,
    FormsModule,
    SdcUiComponentsModule,
    FormElementsModule,
    UiElementsModule,
    TranslateModule
  ],
  exports: [],
  entryComponents: [
    PropertyCreatorComponent
  ],
  providers: []
})

export class PropertyCreatorModule {
}

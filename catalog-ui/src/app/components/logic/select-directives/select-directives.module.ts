import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgSelectModule } from '@ng-select/ng-select';
import { AccordionModule } from 'onap-ui-angular/dist/accordion/accordion.module';
import { TranslateModule } from '../../../shared/translator/translate.module';
import { UiElementsModule } from '../../ui/ui-elements.module';
import { SelectDirectivesComponent } from './select-directives.component';

@NgModule({
  declarations: [
    SelectDirectivesComponent
  ],
  imports: [
    CommonModule,
    UiElementsModule,
    TranslateModule,
    AccordionModule,
    NgSelectModule,
    FormsModule
  ],
  exports: [
    SelectDirectivesComponent
  ],
  entryComponents: [
    SelectDirectivesComponent
  ],
  providers: []
})
export class SelectDirectivesModule {
}

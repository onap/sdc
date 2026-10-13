import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UiElementsModule } from 'app/components/ui/ui-elements.module';
import { TranslateModule } from 'app/shared/translator/translate.module';
import { AccordionModule } from 'onap-ui-angular/dist/accordion/accordion.module';
import { SvgIconModule } from 'onap-ui-angular/dist/svg-icon/svg-icon.module';
import { SelectDirectivesModule } from '../select-directives/select-directives.module';
import { ServiceDependenciesComponent } from './service-dependencies.component';

@NgModule({
    declarations: [
        ServiceDependenciesComponent
    ],
  imports: [
    CommonModule,
    UiElementsModule,
    TranslateModule,
    AccordionModule,
    SelectDirectivesModule,
    SvgIconModule,
    FormsModule
  ],
    exports: [
        ServiceDependenciesComponent
    ],
    entryComponents: [
        ServiceDependenciesComponent
    ],
    providers: []
})
export class ServiceDependenciesModule {
}

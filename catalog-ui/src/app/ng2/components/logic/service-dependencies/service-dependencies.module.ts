import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { UiElementsModule } from 'app/ng2/components/ui/ui-elements.module';
import { TranslateModule } from 'app/ng2/shared/translator/translate.module';
import { ServiceDependenciesComponent } from './service-dependencies.component';
import {AccordionModule} from "onap-ui-angular/dist/accordion/accordion.module";

@NgModule({
    declarations: [
        ServiceDependenciesComponent
    ],
  imports: [
    CommonModule,
    UiElementsModule,
    TranslateModule,
    AccordionModule
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

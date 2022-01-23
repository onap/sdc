import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { UiElementsModule } from 'app/ng2/components/ui/ui-elements.module';
import { TranslateModule } from 'app/ng2/shared/translator/translate.module';
import { ServiceDependenciesComponent } from './service-dependencies.component';
import {AccordionModule} from "onap-ui-angular/dist/accordion/accordion.module";
import {MultiSelectModule} from "../multi-select/multi-select.module";
import {SvgIconModule} from "onap-ui-angular/dist/svg-icon/svg-icon.module";
import {FormsModule} from "@angular/forms";

@NgModule({
    declarations: [
        ServiceDependenciesComponent
    ],
  imports: [
    CommonModule,
    UiElementsModule,
    TranslateModule,
    AccordionModule,
    MultiSelectModule,
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

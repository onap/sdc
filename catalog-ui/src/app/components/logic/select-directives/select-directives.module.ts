import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {UiElementsModule} from "../../ui/ui-elements.module";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {AccordionModule} from "onap-ui-angular/dist/accordion/accordion.module";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule} from "@angular/forms";
import {SelectDirectivesComponent} from "./select-directives.component";

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
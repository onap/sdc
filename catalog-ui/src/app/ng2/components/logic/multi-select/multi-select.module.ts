import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {UiElementsModule} from "../../ui/ui-elements.module";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {AccordionModule} from "onap-ui-angular/dist/accordion/accordion.module";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule} from "@angular/forms";
import {MultiSelectComponent} from "./multi-select.component";

@NgModule({
  declarations: [
    MultiSelectComponent
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
    MultiSelectComponent
  ],
  entryComponents: [
    MultiSelectComponent
  ],
  providers: []
})
export class MultiSelectModule {
}
import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { TranslateModule } from "../../shared/translator/translate.module";
import { TopNavComponent } from "./top-nav/top-nav.component";

/**
 * Imported by the lazy-loaded GlossaryPageModule and OnboardVendorPageModule, so it must not import
 * SdcUiComponentsModule: onap-ui-angular's autocomplete and dropdown modules import BrowserModule,
 * and a lazy module that pulls that in fails with "BrowserModule has already been loaded".
 * TopNavComponent uses nothing from onap-ui-angular.
 */
@NgModule({
    declarations: [
        TopNavComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        TranslateModule
    ],
    exports: [
        TopNavComponent
    ],
    entryComponents: [ //need to add anything that will be dynamically created
        TopNavComponent
    ],
    providers: []
})
export class LayoutModule {

}
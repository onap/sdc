import { NgModule } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { TranslateModule } from "../../shared/translator/translate.module";
import { TopNavComponent } from "./top-nav/top-nav.component";

@NgModule({
    declarations: [
        TopNavComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        TranslateModule
    ],
    exports: [],
    entryComponents: [ //need to add anything that will be dynamically created
        TopNavComponent
    ],
    providers: []
})
export class LayoutModule {

}
import { NgModule } from "@angular/core";
import {CommonModule} from "@angular/common";
import {ServiceDependenciesEditorComponent} from "./service-dependencies-editor.component";
import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "app/ng2/components/ui/form-components/form-elements.module";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";

@NgModule({
    declarations: [
        ServiceDependenciesEditorComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        FormElementsModule,
        UiElementsModule
    ],
    exports: [],
    entryComponents: [
        ServiceDependenciesEditorComponent
    ],
    providers: []
})
export class ServiceDependenciesEditorModule {
}
import { NgModule } from "@angular/core";
import {CommonModule} from "@angular/common";
import {ServicePathCreatorComponent} from "./service-path-creator.component";
import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "app/ng2/components/ui/form-components/form-elements.module";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";
import {LinkRowComponent} from './link-row/link-row.component'
@NgModule({
    declarations: [
        ServicePathCreatorComponent,
        LinkRowComponent
    ],
    imports: [CommonModule,
        FormsModule,
        FormElementsModule,
        UiElementsModule
    ],
    exports: [],
    entryComponents: [
        ServicePathCreatorComponent
    ],
    providers: []
})
export class ServicePathCreatorModule {
}
import { NgModule } from "@angular/core";
import {CommonModule} from "@angular/common";
import {ServicePathSelectorComponent} from "./service-path-selector.component";
import {UiElementsModule} from "app/components/ui/ui-elements.module";

@NgModule({
    declarations: [
        ServicePathSelectorComponent
    ],
    imports: [
    	CommonModule,
    	UiElementsModule
    ],
    exports: [ServicePathSelectorComponent],
    entryComponents: [
        ServicePathSelectorComponent
    ],
})
export class ServicePathSelectorModule {
}
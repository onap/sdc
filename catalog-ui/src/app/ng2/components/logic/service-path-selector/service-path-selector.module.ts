import { NgModule } from "@angular/core";
import {CommonModule} from "@angular/common";
import {ServicePathSelectorComponent} from "./service-path-selector.component";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";

@NgModule({
    declarations: [
        ServicePathSelectorComponent
    ],
    imports: [
    	CommonModule,
    	UiElementsModule
    ],
    exports: [],
    entryComponents: [
        ServicePathSelectorComponent
    ],
    providers: []
})
export class ServicePathSelectorModule {
}
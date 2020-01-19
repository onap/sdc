import { NgModule } from "@angular/core";
import {CommonModule} from "@angular/common";
import {ServicePathSelectorComponent} from "./service-path-selector.component";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";
import {CompositionService} from "app/ng2/pages/composition/composition.service";

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
    providers: [CompositionService]
})
export class ServicePathSelectorModule {
}
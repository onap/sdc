import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {InterfaceOperationComponent} from "./interface-operation.page.component";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";

@NgModule({
    declarations: [
        InterfaceOperationComponent
    ],
    imports: [
        CommonModule,
        UiElementsModule
    ],
    exports: [],
    entryComponents: [
        InterfaceOperationComponent
    ],
    providers: []
})

export class InterfaceOperationModule {}

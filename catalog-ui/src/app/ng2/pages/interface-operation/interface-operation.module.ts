import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {InterfaceOperationComponent} from "./interface-operation.page.component";
import {SdcUiComponentsModule} from "sdc-ui/lib/angular/index";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";
import {TranslateModule} from "app/ng2/shared/translator/translate.module";

@NgModule({
    declarations: [
        InterfaceOperationComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        UiElementsModule,
        TranslateModule
    ],
    exports: [],
    entryComponents: [
        InterfaceOperationComponent
    ],
    providers: []
})

export class InterfaceOperationModule {}

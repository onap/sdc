import {NgModule} from "@angular/core";
import {ParamRowComponent} from "./param-row/param-row.component";
import {OperationCreatorInterfaceDefinitionComponent} from "./operation-creator-interface-definition.component";
import {UiElementsModule} from "../../../components/ui/ui-elements.module";
import {TranslateModule} from "../../../shared/translator/translate.module";
import {SdcUiComponentsModule} from "onap-ui-angular";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "../../../components/ui/form-components/form-elements.module";

@NgModule({
    declarations: [
        OperationCreatorInterfaceDefinitionComponent,
        ParamRowComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        FormsModule,
        FormElementsModule,
        TranslateModule,
        UiElementsModule
    ],
    exports: [],
    entryComponents: [
        OperationCreatorInterfaceDefinitionComponent
    ],
    providers: []
})

export class OperationCreatorInterfaceDefinitionModule {
}

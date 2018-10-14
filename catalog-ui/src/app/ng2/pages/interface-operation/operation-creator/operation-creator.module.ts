import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "app/ng2/components/ui/form-components/form-elements.module";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";

import {OperationCreatorComponent} from "./operation-creator.component";
import {ParamRowComponent} from './param-row/param-row.component';

@NgModule({
    declarations: [
        OperationCreatorComponent,
        ParamRowComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        FormElementsModule,
        UiElementsModule
    ],
    exports: [],
    entryComponents: [
        OperationCreatorComponent
    ],
    providers: []
})

export class OperationCreatorModule {}

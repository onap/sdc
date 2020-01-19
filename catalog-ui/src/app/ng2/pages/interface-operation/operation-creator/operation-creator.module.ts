import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";

import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "app/ng2/components/ui/form-components/form-elements.module";
import {TranslateModule} from "app/ng2/shared/translator/translate.module";

import { SdcUiComponentsModule } from 'onap-ui-angular';
import { UiElementsModule } from '../../../components/ui/ui-elements.module';
import {OperationCreatorComponent} from "./operation-creator.component";
import {ParamRowComponent} from './param-row/param-row.component';

@NgModule({
    declarations: [
        OperationCreatorComponent,
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
        OperationCreatorComponent
    ],
    providers: []
})

export class OperationCreatorModule {}

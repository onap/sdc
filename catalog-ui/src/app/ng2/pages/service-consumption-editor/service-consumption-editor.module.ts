import { NgModule } from "@angular/core";
import {CommonModule} from "@angular/common";
import {ServiceConsumptionCreatorComponent} from "./service-consumption-editor.component";
import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "app/ng2/components/ui/form-components/form-elements.module";
import {UiElementsModule} from "app/ng2/components/ui/ui-elements.module";
import {PropertyTableModule} from 'app/ng2/components/logic/properties-table/property-table.module';
import {TranslateModule} from 'app/ng2/shared/translator/translate.module';

@NgModule({
    declarations: [
        ServiceConsumptionCreatorComponent
    ],
    imports: [CommonModule,
        FormsModule,
        FormElementsModule,
        UiElementsModule,
        PropertyTableModule,
        TranslateModule
    ],
    exports: [],
    entryComponents: [
        ServiceConsumptionCreatorComponent
    ],
    providers: []
})
export class ServiceConsumptionCreatorModule {
}
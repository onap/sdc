import {ToNodeStepComponent} from "./to-node-step/to-node-step.component";
import {NgModule} from "@angular/core";
import {FromNodeStepComponent} from "./from-node-step/from-node-step.component";
import {PropertiesStepComponent} from "./properties-step/properties-step.component";
import {ConnectionWizardService} from "./connection-wizard.service";
import {SelectRequirementOrCapabilityModule} from "../../components/logic/select-requirement-or-capability/select-requirement-or-capability.module";
import {PropertyTableModule} from "../../components/logic/properties-table/property-table.module";
import {FormElementsModule} from "../../components/ui/form-components/form-elements.module";
import {ConnectionWizardHeaderComponent} from "./connection-wizard-header/connection-wizard-header.component";
import {ConnectionPropertiesViewComponent} from "./connection-properties-view/connection-properties-view.component";
import {BrowserModule} from "@angular/platform-browser";

@NgModule({
    declarations: [
        FromNodeStepComponent,
        ToNodeStepComponent,
        PropertiesStepComponent,
        ConnectionWizardHeaderComponent,
        ConnectionPropertiesViewComponent
    ],
    imports: [
        FormElementsModule,
        PropertyTableModule,
        SelectRequirementOrCapabilityModule,
        BrowserModule
    ],
    exports: [
        FromNodeStepComponent,
        ToNodeStepComponent,
        PropertiesStepComponent,
        ConnectionWizardHeaderComponent,
        ConnectionPropertiesViewComponent
    ],
    entryComponents: [FromNodeStepComponent,
        ToNodeStepComponent,
        PropertiesStepComponent,
        ConnectionWizardHeaderComponent,
        ConnectionPropertiesViewComponent
    ],
    providers: [ConnectionWizardService]
})
export class ConnectionWizardModule {
}
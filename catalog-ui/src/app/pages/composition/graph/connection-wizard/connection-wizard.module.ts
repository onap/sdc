import {ToNodeStepComponent} from "./to-node-step/to-node-step.component";
import {NgModule} from "@angular/core";
import {FromNodeStepComponent} from "./from-node-step/from-node-step.component";
import {PropertiesStepComponent} from "./properties-step/properties-step.component";
import {ConnectionWizardService} from "./connection-wizard.service";
import {SelectRequirementOrCapabilityModule} from "../../../../components/logic/select-requirement-or-capability/select-requirement-or-capability.module";
import {PropertyTableModule} from "../../../../components/logic/properties-table/property-table.module";
import {FormElementsModule} from "../../../../components/ui/form-components/form-elements.module";
import {ConnectionWizardHeaderComponent} from "./connection-wizard-header/connection-wizard-header.component";
import {ConnectionPropertiesViewComponent} from "./connection-properties-view/connection-properties-view.component";
import {BrowserModule} from "@angular/platform-browser";
import {RelationshipOperationsStepComponent} from './relationship-operations-step/relationship-operations-step.component';
import {InterfaceOperationModule} from "../../../interface-operation/interface-operation.module";
import {UiElementsModule} from "../../../../components/ui/ui-elements.module";
import {TranslateModule} from "../../../../shared/translator/translate.module";
import {SvgIconModule} from "onap-ui-angular/dist/svg-icon/svg-icon.module";
import {OperationCreatorModule} from "../../../interface-operation/operation-creator/operation-creator.module";
import {CreateInterfaceOperationComponent} from './create-interface-operation/create-interface-operation.component';
import {DropdownModule} from "onap-ui-angular/dist/form-elements/dropdown/dropdown.module";
import {InputModule} from "onap-ui-angular/dist/form-elements/text-elements/input/input.module";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SdcUiComponentsModule} from "onap-ui-angular/dist";
import {CreateInputRowComponent} from './create-interface-operation/create-input-row/create-input-row.component';
import {InterfaceOperationListComponent} from './relationship-operations-step/interface-operation-list/interface-operation-list.component';

@NgModule({
    declarations: [
        FromNodeStepComponent,
        ToNodeStepComponent,
        PropertiesStepComponent,
        ConnectionWizardHeaderComponent,
        ConnectionPropertiesViewComponent,
        RelationshipOperationsStepComponent,
        CreateInterfaceOperationComponent,
        CreateInputRowComponent,
        InterfaceOperationListComponent
    ],
  imports: [
    FormElementsModule,
    PropertyTableModule,
    SelectRequirementOrCapabilityModule,
    BrowserModule,
    InterfaceOperationModule,
    UiElementsModule,
    TranslateModule,
    SvgIconModule,
    OperationCreatorModule,
    DropdownModule,
    InputModule,
    FormsModule,
    SdcUiComponentsModule,
    ReactiveFormsModule
  ],
    exports: [
        FromNodeStepComponent,
        ToNodeStepComponent,
        PropertiesStepComponent,
        RelationshipOperationsStepComponent,
        ConnectionWizardHeaderComponent,
        ConnectionPropertiesViewComponent
    ],
    entryComponents: [FromNodeStepComponent,
        ToNodeStepComponent,
        PropertiesStepComponent,
        RelationshipOperationsStepComponent,
        ConnectionWizardHeaderComponent,
        ConnectionPropertiesViewComponent
    ],
    providers: [ConnectionWizardService]
})
export class ConnectionWizardModule {
}
import {NgModule} from "@angular/core";
import {SelectRequirementOrCapabilityComponent} from "./select-requirement-or-capability.component";
import {FormsModule} from "@angular/forms";
import {FormElementsModule} from "../../ui/form-components/form-elements.module";
import {CommonModule} from "@angular/common";
import {GlobalPipesModule} from "app/ng2/pipes/global-pipes.module";
import {PropertyTableModule} from "../properties-table/property-table.module";

@NgModule({
    declarations: [
        SelectRequirementOrCapabilityComponent
    ],
    imports: [
        CommonModule,
        FormsModule,
        FormElementsModule,
        GlobalPipesModule,
        PropertyTableModule],

    exports: [SelectRequirementOrCapabilityComponent],
    providers: []
})
export class SelectRequirementOrCapabilityModule {
}
/**
 * Created by rc2122 on 8/15/2017.
 */
import { NgModule } from "@angular/core";
import {MultiStepsWizardComponent} from "./multi-steps-wizard.component";
import {CommonModule} from "@angular/common";
import {ConnectionWizardModule} from "../../../pages/connection-wizard/connection-wizard.module";

@NgModule({
    declarations: [
        MultiStepsWizardComponent
    ],
    imports: [CommonModule
    ],
    exports: [],
    entryComponents: [
        MultiStepsWizardComponent
    ],
    providers: []
})
export class MultiStepsWizardModule {
}
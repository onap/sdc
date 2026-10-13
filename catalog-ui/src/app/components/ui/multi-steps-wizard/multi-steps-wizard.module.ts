/**
 * Created by rc2122 on 8/15/2017.
 */
import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { ConnectionWizardModule } from 'app/pages/composition/graph/connection-wizard/connection-wizard.module';
import { MultiStepsWizardComponent } from './multi-steps-wizard.component';

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

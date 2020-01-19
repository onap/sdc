/**
 * Created by rc2122 on 5/24/2018.
 */
import { NgModule } from "@angular/core";
import { TranslateModule } from "app/ng2/shared/translator/translate.module";
import { SdcUiComponentsModule } from "onap-ui-angular";
import { CommonModule } from '@angular/common';
import {ArtifactFormComponent} from "./artifact-form.component";

@NgModule({
    declarations: [ArtifactFormComponent],
    imports: [TranslateModule,
        SdcUiComponentsModule,
        CommonModule],
    exports: [ArtifactFormComponent],
    entryComponents: [ArtifactFormComponent]
})


export class ArtifactFormModule {
}
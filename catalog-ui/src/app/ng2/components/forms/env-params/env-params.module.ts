import { NgModule } from "@angular/core";
import { EnvParamsComponent } from "./env-params.component";
import { NgxDatatableModule } from "@swimlane/ngx-datatable";
import { SdcUiComponentsModule, SdcUiServices } from "onap-ui-angular";


@NgModule({
    declarations: [
        EnvParamsComponent
    ],
    imports: [
        NgxDatatableModule,
        SdcUiComponentsModule
    ],
    exports: [
        EnvParamsComponent
    ],
    entryComponents: [ //need to add anything that will be dynamically created
        EnvParamsComponent
    ],
    providers: [
        SdcUiServices.ModalService
    ]
})

export class EnvParamsModule {

}

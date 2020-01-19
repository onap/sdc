import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {SdcUiComponentsModule} from "onap-ui-angular";
import {GlobalPipesModule} from "../../../pipes/global-pipes.module";
import {ActivityLogComponent} from "./activity-log.component";
import {ActivityLogService} from "../../../services/activity-log.service";
import {NgxDatatableModule} from "@swimlane/ngx-datatable";

@NgModule({
    declarations: [
        ActivityLogComponent
    ],
    imports: [
        CommonModule,
        SdcUiComponentsModule,
        GlobalPipesModule,
        NgxDatatableModule
    ],
    exports: [
        ActivityLogComponent
    ],
    entryComponents: [
        ActivityLogComponent
    ],
    providers: [ ActivityLogService ]
})
export class ActivityLogModule {
}

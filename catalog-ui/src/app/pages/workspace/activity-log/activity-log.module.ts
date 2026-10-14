import {CommonModule} from "@angular/common";
import {NgModule} from "@angular/core";
import {SdcUiComponentsModule} from "onap-ui-angular";
import { DatatableA11yModule } from '../../../components/ui/datatable-a11y/datatable-a11y.module';
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
        NgxDatatableModule,
        DatatableA11yModule
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

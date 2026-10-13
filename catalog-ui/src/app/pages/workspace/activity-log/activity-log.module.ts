import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { SdcUiComponentsModule } from 'onap-ui-angular';
import { GlobalPipesModule } from '../../../pipes/global-pipes.module';
import { ActivityLogService } from '../../../services/activity-log.service';
import { ActivityLogComponent } from './activity-log.component';

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

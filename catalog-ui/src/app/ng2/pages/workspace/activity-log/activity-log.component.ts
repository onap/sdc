import { Component, OnInit, ViewChild } from '@angular/core';
import { SdcUiServices } from 'onap-ui-angular';
import { Activity } from '../../../../models/activity';
import { ActivityLogService } from '../../../services/activity-log.service';
import { WorkspaceService } from '../workspace.service';

@Component({
    selector: 'activity-log',
    templateUrl: './activity-log.component.html',
    styleUrls: ['./activity-log.component.less', '../../../../../assets/styles/table-style.less']
})
export class ActivityLogComponent implements OnInit {

    activities: Activity[] = [];
    temp: Activity[] = [];

    constructor(private workspaceService: WorkspaceService,
                private activityLogService: ActivityLogService,
                private loaderService: SdcUiServices.LoaderService) {
    }

    ngOnInit(): void {
        this.loaderService.activate();
        const componentId: string = this.workspaceService.metadata.uniqueId;
        const componentType: string = this.workspaceService.metadata.componentType;
        this.activityLogService.getActivityLog(componentType, componentId).subscribe((logs) => {
            this.activities = logs;
            this.temp = [...logs];
            this.loaderService.deactivate();
        }, (error) => { this.loaderService.deactivate(); });
    }

    updateFilter(event) {
        const val = event.target.value.toLowerCase();

        // filter our data
        const temp = this.temp.filter((activity: Activity) => {
            return !val ||
                activity.COMMENT.toLowerCase().indexOf(val) !== -1 ||
                activity.STATUS.toLowerCase().indexOf(val) !== -1 ||
                activity.ACTION.toLowerCase().indexOf(val) !== -1 ||
                activity.MODIFIER.toLowerCase().indexOf(val) !== -1;
        });

        // update the rows
        this.activities = temp;
    }
}

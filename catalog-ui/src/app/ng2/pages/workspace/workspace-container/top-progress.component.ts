import {Component as NgComponent, Input} from '@angular/core';

@NgComponent({
    selector: 'workspace-top-progress',
    template: `
        <div class="top-progress">
            <div *ngIf="progressValue > 0 && progressValue < 100">
                <span class="sdc-progress-title">{{progressMessage}}<span class="progress-percentage">{{progressValue}}&nbsp;%</span></span>
                <div class="sdc-progress">
                    <div class="progress-bar progress-bar-striped active" role="progressbar"
                         [style.width.%]="progressValue"></div>
                </div>
            </div>
            <div class="sdc-progress-success-wrapper" *ngIf="progressValue === 100">
                <span class="sdc-progress-success"></span>
                <span class="sdc-progress-success-title">{{progressMessage}}</span>
            </div>
            <div class="sdc-progress-error-wrapper" *ngIf="progressValue === -1">
                <span class="sdc-progress-error"></span>
                <span class="sdc-progress-error-title">{{progressMessage}}</span>
            </div>
        </div>
    `
})
export class WorkspaceTopProgressComponent {
    @Input() progressValue: number = 0;
    @Input() progressMessage: string = '';
}

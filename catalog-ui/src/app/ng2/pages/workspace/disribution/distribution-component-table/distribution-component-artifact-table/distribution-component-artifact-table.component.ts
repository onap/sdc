import { Component, Input, OnInit, ViewChild } from '@angular/core';
import * as _ from 'lodash';
import { DistributionService } from '../../distribution.service';

// tslint:disable:no-string-literal

@Component({
    selector: 'app-distribution-component-artifact-table',
    templateUrl: './distribution-component-artifact-table.component.html',
    styleUrls: ['./distribution-component-artifact-table.component.less']
})
export class DistributionComponentArtifactTableComponent implements OnInit {

    @ViewChild('statusTable', {}) table: any;

    @Input() componentName: string;
    @Input() rowDistributionID: string;
    @Input() statusFilter: string;

    public artifacts = [];

    constructor(private distributionService: DistributionService) {
    }

    ngOnInit() {
        this.artifacts = this.distributionService.getArtifactstByDistributionIDAndComponentsName(this.rowDistributionID, this.componentName);
        if (this.statusFilter) {
            this.artifacts.forEach(
            (artifact) => {
                artifact.statuses = _.filter(artifact.statuses, {status: this.statusFilter});
            });
        }
    }

    public getLatestArtifact(artifactName: string) {
        const selectedArtifact = this.artifacts.filter((artifact) => artifact.name === artifactName);
        if (selectedArtifact && selectedArtifact[0] && selectedArtifact[0]['statuses'] && selectedArtifact[0]['statuses'][0]) {
            return selectedArtifact[0]['statuses'][0];
        } else {
            return null;
        }
    }

    private copyToClipboard(urlToCopy: any) {

        const inputForCopyToClipboard = document.getElementById('inputForCopyToClipboard') as HTMLInputElement;
        inputForCopyToClipboard.value = urlToCopy;
        /* Select the text field */
        inputForCopyToClipboard.select();

        /* Copy the text inside the text field */
        document.execCommand('copy');

    }

    private generateDataTestID(preFix: string, componentName: string, artifactName: string, status?: string) {
        if (!status) {
            return preFix + componentName + '_' + artifactName;
        } else {
            return preFix + status + '_' + componentName + '_' + artifactName;
        }
    }

    private expandRow(row: any) {
        this.table.rowDetail.toggleExpandRow(row);
    }

}

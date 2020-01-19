import { Component, Input, OnInit, ViewChild } from '@angular/core';
import { SdcUiCommon, SdcUiServices } from 'onap-ui-angular';
import { EventListenerService } from '../../../../services/event-listener-service';
import { AuthenticationService } from '../../../services/authentication.service';
import { WorkspaceService } from '../workspace.service';
import { DistributionService } from './distribution.service';
import { EVENTS } from '../../../../utils/constants';

@Component({
  selector: 'distribution',
  templateUrl: './distribution.component.html',
  styleUrls: ['../../../../../assets/styles/table-style.less', './distribution.component.less']
})
export class DistributionComponent implements OnInit {

  @ViewChild('distributionTable', { }) table: any;

  @Input() isModal: boolean = false;
  @Input() statusFilter: string;
  @Input() rowDistributionID: string;
  public componentUuid: string;
  public distributions = [];
  private expanded: any = {};
  private serviceHasDistibutions: boolean = false;
  private readonly uniqueId: string;
  private  userRole: string;

  constructor(private workspaceService: WorkspaceService,
              private distributionService: DistributionService,
              private loaderService: SdcUiServices.LoaderService,
              private authService: AuthenticationService,
              private eventListenerService: EventListenerService) {
    this.componentUuid = this.workspaceService.metadata.uuid;
    this.uniqueId = this.workspaceService.metadata.uniqueId;
  }



  async ngOnInit() {
    this.userRole = this.authService.getLoggedinUser().role;
    this.eventListenerService.registerObserverCallback(EVENTS.ON_DISTRIBUTION_SUCCESS, async () => {
      await this.refreshDistributions();
    });
    await this.initDistributions(this.componentUuid, this.rowDistributionID);
  }

  ngOnDestroy(): void {
    this.eventListenerService.unRegisterObserver(EVENTS.ON_DISTRIBUTION_SUCCESS);
  }

  async initDistributions(componentUuid: string, specificDistributionID?: string)  {
    this.loaderService.activate();
    await this.distributionService.initDistributionsList(componentUuid);
    this.distributions = this.distributionService.getDistributionList();
    this.distributions.length > 0 ? this.serviceHasDistibutions = true : this.serviceHasDistibutions = false;
    if (specificDistributionID) {
      this.distributions = this.distributionService.getDistributionList(specificDistributionID);
    }
    this.loaderService.deactivate();
  }

  getIconName(rowStatus: string ) {
    if (rowStatus === 'Distributed') {
      return 'distributed';
    }
    if (rowStatus === 'Deployed') {
      return 'v-circle';
    }
  }

  getIconMode(rowStatus: string) {
    if (rowStatus === 'Distributed') {
      return 'primary';
    }
    if (rowStatus === 'Deployed') {
      return 'secondary';
    }
  }

  private async markDeploy(distributionID: string, status: string) {
    if (status === 'Distributed') {
      console.log('Should send MarkDeploy POST Request ServiceID:' + this.uniqueId + ' DISTID:' + distributionID);
      await this.distributionService.markDeploy(this.uniqueId, distributionID);
      this.refreshDistributions();
    }
  }

  private async refreshDistributions() {
    await this.initDistributions(this.componentUuid);
  }

  private updateFilter(event) {
    const val = event.target.value.toLowerCase();

    // filter our data
    this.distributions = this.distributionService.getDistributionList().filter((distribution: any[]) => {
      return !val ||
          // tslint:disable:no-string-literal
          distribution['distributionID'].toLowerCase().indexOf(val) !== -1;
    });
  }

  private generateDataTestID(preFix: string, distributionID: string, isModal?: boolean ): string {
    if (isModal) {
      return preFix + distributionID.substring(0, 5) + '_Modal';
    } else {
      return preFix + distributionID.substring(0, 5);
    }
  }

  private async expandRow(row: any, expanded: boolean) {
    if (!expanded) {
      await this.distributionService.initDistributionsStatusForDistributionID(row.distributionID);
    }
    this.table.rowDetail.toggleExpandRow(row);
  }
}

import { Component, Input, OnInit } from '@angular/core';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { ModalComponent } from 'onap-ui-angular/dist/modals/modal.component';
import { DistributionComponent } from '../distribution.component';
import { DistributionService } from '../distribution.service';

@Component({
  selector: 'app-distribution-component-table',
  templateUrl: './distribution-component-table.component.html',
  styleUrls: ['./distribution-component-table.component.less']
})
export class DistributionComponentTableComponent implements OnInit {

  @Input() rowDistributionID: string;
  @Input() isModal: boolean = false;
  @Input() statusFilter: string;
  public components = [];
  private customModalInstance: ModalComponent;
  private expanded = [];
  constructor(private distributionService: DistributionService,
              private modalService: SdcUiServices.ModalService) {
  }

  ngOnInit() {
    this.initComponents();
  }

  private generateTotalComponentArtifactsLabel(componentName: any, status: string): string {
    return 'total' + componentName + status + 'ArtifactsLabel';
  }

  private generateExpandDataTestID(componentName: string) {
    return 'expandIcon_' + componentName;
  }

  private initComponents() {
    this.components = this.distributionService.getComponentsByDistributionID(this.rowDistributionID);
    this.components.map((component) => this.expanded.push({componentName: component, expanded: false}));
  }

  private getTotalArtifactsForDistributionID(distributionID: string, componentName?: string): number {
    return this.distributionService.getArtifactstByDistributionIDAndComponentsName(distributionID, componentName).length;
  }

  private getLengthArtifactsForDistributionIDByStatus(distributionID: string, statusToSerach: string, componentName?: string): number {
    if (componentName) {
      return this.distributionService.getArtifactsForDistributionIDAndComponentByStatus(distributionID, statusToSerach, componentName).length;
    } else {
      return this.distributionService.getArtifactsForDistributionIDAndComponentByStatus(distributionID, statusToSerach).length;
    }
  }

  private openModal(rowDistributionID: string, statusFilter: string) {

    const title: string = 'Distribution by Status';
    const attributeModalConfig = {
      title,
      size: 'sdc-xl',
      type: SdcUiCommon.ModalType.custom,
      buttons: [
        {
          id: 'close',
          text: 'Close',
          size: 'sm',
          closeModal: true,
          disabled: false,
        }
      ] as SdcUiCommon.IModalButtonComponent[]
    };

    this.customModalInstance = this.modalService.openCustomModal(attributeModalConfig, DistributionComponent, {
        // inputs
        rowDistributionID,
        statusFilter,
        isModal: true,
    });
  }

  private expandRow(componentName: string) {
    console.log('Should expand componentSummary for componentName = ' + componentName);
    const selectedComponent = this.expanded.find((component) => component.componentName === componentName);
    // tslint:disable:no-string-literal
    const selectedComponentExpandedVal = selectedComponent['expanded'];
    // this.expanded = !this.expanded;
    for (const i in this.expanded) {
      if (this.expanded[i].componentName === componentName) {
        this.expanded[i].expanded = !this.expanded[i].expanded;
        break; //Stop this loop, we found it!
      }
    }
    const selectedComponentAfter = this.expanded.find((component) => component.componentName === componentName);
    const selectedComponentExpandedValAfter = selectedComponentAfter['expanded'];
  }

  private isExpanded(componentName: string) {
    const selectedComponent = this.expanded.find((component) => component.componentName === componentName);
    return selectedComponent['expanded'];
  }


    private getMSOStatus(rowDistributionID: string, componentName: string): string {
        return this.distributionService.getMSOStatus(rowDistributionID, componentName);
    }
}

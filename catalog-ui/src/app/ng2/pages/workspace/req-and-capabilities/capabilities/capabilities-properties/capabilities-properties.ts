
import { ViewChild, Input, OnInit, Component } from "@angular/core";
import {SdcUiServices} from "onap-ui-angular";
import { ModalsHandler } from "../../../../../../utils/modals-handler";
import { WorkspaceService } from "../../../workspace.service";
import { PropertyModel } from "../../../../../../models/properties";


@Component({
    selector: 'capabilities-properties',
    templateUrl: './capabilities-properties.html',
    styleUrls: ['./capabilities-properties.less', '../../../../../../../assets/styles/table-style.less']
})
export class CapabilitiesPropertiesComponent {
    @Input() public capabilitiesProperties: Array<PropertyModel> = [];

    private capabilityPropertiesColumns = [
        {name: 'Name', prop: 'name', flexGrow: 1},
        {name: 'Type', prop: 'type', flexGrow: 1},
        {name: 'Schema', prop: 'schema', flexGrow: 1},
        {name: 'Description', prop: 'description', flexGrow: 1},
    ];
    constructor(private modalsHandler: ModalsHandler,
                private workspaceService: WorkspaceService) {}

    private updateProperty(property: PropertyModel): void {
        _.forEach(this.capabilitiesProperties, (prop: PropertyModel) => {
            prop.readonly = true;
        });
        this.modalsHandler.openEditPropertyModal(property, this.workspaceService.metadata, this.capabilitiesProperties, false, 'component',
        this.workspaceService.metadata.uniqueId);
    }
}
import {Component, OnInit, ViewChild} from "@angular/core";
import {WorkspaceService} from "../workspace.service";
import {SdcUiServices} from "onap-ui-angular";
import {ArtifactModel} from "../../../../models";
import {Select, Store} from "@ngxs/store";
import {WorkspaceState} from "../../../store/states/workspace.state";
import {ArtifactGroupType} from "../../../../utils";
import {GetArtifactsByTypeAction} from "../../../store/actions/artifacts.action";
import {Observable} from "rxjs/index";
import {ArtifactsState} from "../../../store/states/artifacts.state";
import {map} from "rxjs/operators";

@Component({
    selector: 'tosca-artifact-page',

    templateUrl: './tosca-artifact-page.component.html',
    styleUrls: ['./tosca-artifact-page.component.less', '../../../../../assets/styles/table-style.less']
})
export class ToscaArtifactPageComponent implements OnInit {

    @Select(WorkspaceState.isViewOnly) isViewOnly$: boolean;
    @ViewChild('toscaArtifactsTable') table: any;
    public toscaArtifacts$: Observable<ArtifactModel[]>;
    public componentId: string;
    public componentType:string;

    constructor(private serviceLoader: SdcUiServices.LoaderService, private workspaceService: WorkspaceService, private store: Store) {
    }


    ngOnInit(): void {
        this.componentId = this.workspaceService.metadata.uniqueId;
        this.componentType = this.workspaceService.metadata.componentType;

        this.store.dispatch(new GetArtifactsByTypeAction({componentType:this.componentType, componentId:this.componentId, artifactType:ArtifactGroupType.TOSCA}));
        this.toscaArtifacts$ = this.store.select(ArtifactsState.getArtifactsByType).pipe(map(filterFn => filterFn(ArtifactGroupType.TOSCA)));
    }

    onActivate(event) {
        if(event.type === 'click'){
            this.table.rowDetail.toggleExpandRow(event.row);
        }
    }
<<<<<<< PATCH SET (9da9d7 Provide UI support to upload csar to update service)

    getExtension(artifactType: string) {
        switch (artifactType) {
            case (ArtifactType.TOSCA.TOSCA_CSAR):
                return "csar";
            case (ArtifactType.TOSCA.TOSCA_TEMPLATE):
                return "yaml,yml";
        }
    }

    isService() {
        return ComponentType.SERVICE === this.componentType;
    }

    isCheckedOut() {
        return this.workspaceService.metadata.lifecycleState === ComponentState.NOT_CERTIFIED_CHECKOUT;
    }

    onFileUpload(file, artifactType) {
        if (file && file.name) {
            this.isLoading = true;
            switch (artifactType) {
                case (ArtifactType.TOSCA.TOSCA_CSAR):
                    this.componentService.putServiceToscaModel(this.componentId, this.componentType, file).subscribe((response)=> {
                        this.Notification.success({
                            message: "Service " + response.name + " has been updated",
                            title: "Success"
                        });
                        this.isLoading = false;
                    }, () => {
                        this.isLoading = false;
                    });
                    break;
                case (ArtifactType.TOSCA.TOSCA_TEMPLATE):
                    this.componentService.putServiceToscaTemplate(this.componentId, this.componentType, file).subscribe((response)=> {
                        this.Notification.success({
                            message: "Service " + response.name + " has been updated",
                            title: "Success"
                        });
                        this.isLoading = false;
                    }, () => {
                        this.isLoading = false;
                    });
                    break;
            }
        }
    }
=======
>>>>>>> BASE      (142503 Implement 'Update Service by importing Tosca Model'-story)
}
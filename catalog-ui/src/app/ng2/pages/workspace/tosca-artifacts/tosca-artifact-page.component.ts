import { Component, Inject, OnInit, ViewChild } from "@angular/core";
import { WorkspaceService } from "../workspace.service";
import { ArtifactModel } from "../../../../models";
import { Select, Store } from "@ngxs/store";
import { WorkspaceState } from "../../../store/states/workspace.state";
import { ArtifactGroupType } from "../../../../utils";
import { GetArtifactsByTypeAction } from "../../../store/actions/artifacts.action";
import { Observable } from "rxjs/index";
import { ArtifactsState } from "../../../store/states/artifacts.state";
import { map } from "rxjs/operators";
import { ArtifactType, ComponentState, ComponentType } from "app/utils/constants"
import { TopologyTemplateService } from "app/ng2/services/component-services/topology-template.service";

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
    public isLoading: boolean = false;

    constructor(
        private workspaceService: WorkspaceService,
        private store: Store,
        @Inject("Notification") private Notification: any,
        private componentService: TopologyTemplateService) {
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
                    this.Notification.error({
                        message: "Feature not implemented yet",
                        title: "Error"
                    });
                    this.isLoading = false;
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
}
import {Component, OnInit, ViewChild} from "@angular/core";
import {WorkspaceService} from "../workspace.service";
import {SdcUiCommon, SdcUiComponents, SdcUiServices} from "onap-ui-angular";
import {TopologyTemplateService} from "../../../services/component-services/topology-template.service";
import * as _ from "lodash";
import {ArtifactGroupType, ArtifactType} from "../../../../utils/constants";
import {ArtifactsService} from "../../../components/forms/artifacts-form/artifacts.service";
import {DeleteArtifactAction, GetArtifactsByTypeAction} from "../../../store/actions/artifacts.action";
import {Select, Store} from "@ngxs/store";
import {Observable} from "rxjs/index";
import {ArtifactsState} from "../../../store/states/artifacts.state";
import {map} from "rxjs/operators";
import {WorkspaceState} from "../../../store/states/workspace.state";
import {ArtifactModel} from "../../../../models/artifacts";

@Component({
    selector: 'information-artifact-page',
    templateUrl: './information-artifact-page.component.html',
    styleUrls: ['./information-artifact-page.component.less', '../../../../../assets/styles/table-style.less']
})
export class InformationArtifactPageComponent implements OnInit {

    public componentId: string;
    public componentType: string;
    public informationArtifacts$: Observable<ArtifactModel[]>;
    public informationArtifactsAsButtons$: Observable<ArtifactModel[]>;
    @Select(WorkspaceState.isViewOnly) isViewOnly$: boolean;
    @ViewChild('informationArtifactsTable') table: any;

    constructor(private workspaceService: WorkspaceService,
                private artifactsService: ArtifactsService,
                private store: Store) {
    }

    ngOnInit(): void {
        this.componentId = this.workspaceService.metadata.uniqueId;
        this.componentType = this.workspaceService.metadata.componentType;

        this.store.dispatch(new GetArtifactsByTypeAction({
            componentType: this.componentType,
            componentId: this.componentId,
            artifactType: ArtifactGroupType.INFORMATION
        }));

        let artifacts = this.store.select(ArtifactsState.getArtifactsByType).pipe(map(filterFn => filterFn(ArtifactType.INFORMATION)));
        this.informationArtifacts$ = artifacts.pipe(map(artifacts => _.filter(artifacts, (artifact) => {
            return artifact.esId;
        })));

        this.informationArtifactsAsButtons$ = artifacts.pipe(map(artifacts => _.filter(artifacts, (artifact) => {
            return !artifact.esId;
        })));
    }

    onActivate(event) {
        if (event.type === 'click') {
            this.table.rowDetail.toggleExpandRow(event.row);
        }
    }

    public addOrUpdateArtifact = (artifact: ArtifactModel, isViewOnly?: boolean) => {
        this.artifactsService.openArtifactModal(this.componentId, this.componentType, artifact, ArtifactGroupType.INFORMATION, isViewOnly);
    }

    public deleteArtifact = (artifactToDelete) => {
      this.artifactsService.deleteArtifact(this.componentType, this.componentId, artifactToDelete)
    }

}
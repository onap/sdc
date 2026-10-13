import { Component, OnInit, ViewChild } from '@angular/core';
import { Select, Store } from '@ngxs/store';
import * as _ from 'lodash';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { ArtifactsService } from '../../../components/forms/artifacts-form/artifacts.service';
import { ArtifactModel } from '../../../models/artifacts';
import { GetArtifactsByTypeAction } from '../../../store/actions/artifacts.action';
import { ArtifactsState } from '../../../store/states/artifacts.state';
import { WorkspaceState } from '../../../store/states/workspace.state';
import { ArtifactGroupType, ArtifactType } from '../../../utils/constants';
import { WorkspaceService } from '../workspace.service';

@Component({
    selector: 'information-artifact-page',
    templateUrl: './information-artifact-page.component.html',
    styleUrls: ['./information-artifact-page.component.less', '../../../../assets/styles/table-style.less']
})
export class InformationArtifactPageComponent implements OnInit {

    public componentId: string;
    public componentType: string;
    public resourceType: string;
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
        this.resourceType = this.workspaceService.metadata.resourceType;

        this.store.dispatch(new GetArtifactsByTypeAction({
            componentType: this.componentType,
            componentId: this.componentId,
            artifactType: ArtifactGroupType.INFORMATION
        }));

        const artifacts = this.store.select(ArtifactsState.getArtifactsByType).pipe(map((filterFn) => filterFn(ArtifactType.INFORMATION)));
        this.informationArtifacts$ = artifacts.pipe(map((artifacts) => _.filter(artifacts, (artifact) => {
            return artifact.esId;
        })));

        this.informationArtifactsAsButtons$ = artifacts.pipe(map((artifacts) => _.filter(artifacts, (artifact) => {
            return !artifact.esId;
        })));
    }

    onActivate(event) {
        if (event.type === 'click') {
            this.table.rowDetail.toggleExpandRow(event.row);
        }
    }

    public addOrUpdateArtifact = (artifact?: ArtifactModel, isViewOnly?: boolean) => {
        this.artifactsService.openArtifactModal(this.componentId, this.componentType, artifact, ArtifactGroupType.INFORMATION, isViewOnly, null, this.resourceType);
    }

    public deleteArtifact = (artifactToDelete) => {
      this.artifactsService.deleteArtifact(this.componentType, this.componentId, artifactToDelete);
    }

}

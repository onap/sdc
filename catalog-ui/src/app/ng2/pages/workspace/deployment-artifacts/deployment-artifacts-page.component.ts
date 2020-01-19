import { Component, OnInit, ViewChild } from '@angular/core';
import { Select, Store } from '@ngxs/store';
import { ArtifactModel } from 'app/models';
import * as _ from 'lodash';
import { SdcUiCommon, SdcUiComponents, SdcUiServices } from 'onap-ui-angular';
import { Observable } from 'rxjs/index';
import { map } from 'rxjs/operators';
import { GabConfig } from '../../../../models/gab-config';
import { PathsAndNamesDefinition } from '../../../../models/paths-and-names';
import { GenericArtifactBrowserComponent } from '../../../../ng2/components/logic/generic-artifact-browser/generic-artifact-browser.component';
import { ArtifactGroupType, ArtifactType } from '../../../../utils/constants';
import { ArtifactsService } from '../../../components/forms/artifacts-form/artifacts.service';
import { PopoverContentComponent } from '../../../components/ui/popover/popover-content.component';
import { CacheService } from '../../../services/cache.service';
import { TranslateService } from '../../../shared/translator/translate.service';
import { GetArtifactsByTypeAction } from '../../../store/actions/artifacts.action';
import { ArtifactsState } from '../../../store/states/artifacts.state';
import { WorkspaceState, WorkspaceStateModel } from '../../../store/states/workspace.state';
import { WorkspaceService } from '../workspace.service';
import { ModalService } from 'app/ng2/services/modal.service';

export interface IPoint {
    x: number;
    y: number;
}

@Component({
    selector: 'deployment-artifact-page',
    templateUrl: './deployment-artifacts-page.component.html',
    styleUrls: ['./deployment-artifacts-page.component.less', '../../../../../assets/styles/table-style.less']
})
export class DeploymentArtifactsPageComponent implements OnInit {

    public componentId: string;
    public componentType: string;
    public deploymentArtifacts$: Observable<ArtifactModel[]>;
    public isComponentInstanceSelected: boolean;

    @Select(WorkspaceState) workspaceState$: Observable<WorkspaceStateModel>;
    @ViewChild('informationArtifactsTable') table: any;
    @ViewChild('popoverForm') popoverContentComponent: PopoverContentComponent;

    constructor(private workspaceService: WorkspaceService,
                private artifactsService: ArtifactsService,
                private store: Store,
                private popoverService: SdcUiServices.PopoverService,
                private cacheService: CacheService,
                private modalService: SdcUiServices.ModalService,
                private translateService: TranslateService) {
    }

    private getEnvArtifact = (heatArtifact: ArtifactModel, artifacts: ArtifactModel[]): ArtifactModel => {
        return _.find(artifacts, (item: ArtifactModel) => {
            return item.generatedFromId === heatArtifact.uniqueId;
        });
    };

    // we need to sort the artifact in a way that the env artifact is always under the artifact he is connected to- this is cause of the way the ngx databale work
    private sortArtifacts = ((artifacts) => {
        const sortedArtifacts = [];
        _.forEach(artifacts, (artifact: ArtifactModel): void => {
            const envArtifact = this.getEnvArtifact(artifact, artifacts);
            if (!artifact.generatedFromId) {
                sortedArtifacts.push(artifact);
            }
            if (envArtifact) {
                sortedArtifacts.push(envArtifact);
            }
        });
        return sortedArtifacts;
    })

    ngOnInit(): void {
        this.componentId = this.workspaceService.metadata.uniqueId;
        this.componentType = this.workspaceService.metadata.componentType;

        this.store.dispatch(new GetArtifactsByTypeAction({
            componentType: this.componentType,
            componentId: this.componentId,
            artifactType: ArtifactGroupType.DEPLOYMENT
        }));
        this.deploymentArtifacts$ = this.store.select(ArtifactsState.getArtifactsByType).pipe(map((filterFn) => filterFn(ArtifactType.DEPLOYMENT))).pipe(map(artifacts => {
            return this.sortArtifacts(artifacts);
        }));
    }

    onActivate(event) {
        if (event.type === 'click') {
            this.table.rowDetail.toggleExpandRow(event.row);
        }
    }

    public addOrUpdateArtifact = (artifact: ArtifactModel, isViewOnly: boolean) => {
        this.artifactsService.openArtifactModal(this.componentId, this.componentType, artifact, ArtifactGroupType.DEPLOYMENT, isViewOnly);
    }

    public deleteArtifact = (artifactToDelete) => {
        this.artifactsService.deleteArtifact(this.componentType, this.componentId, artifactToDelete);
    }

    private openPopOver = (title: string, content: string, positionOnPage: IPoint, location: string) => {
        this.popoverService.createPopOver(title, content, positionOnPage, location);
    }

    public updateEnvParams = (artifact: ArtifactModel, isViewOnly: boolean) => {
        this.artifactsService.openUpdateEnvParams(this.componentType, this.componentId, artifact );
    }

    private openGenericArtifactBrowserModal = (artifact: ArtifactModel): void => {
        const titleStr = 'Generic Artifact Browser';
        const modalConfig = {
            size: 'sdc-xl',
            title: titleStr,
            type: SdcUiCommon.ModalType.custom,
            buttons: [{
                id: 'closeGABButton',
                text: 'Close',
                size: 'sm',
                closeModal: true
            }] as SdcUiCommon.IModalButtonComponent[]
        };

        const uiConfiguration: any = this.cacheService.get('UIConfiguration');
        let noConfig: boolean = false;
        let pathsandnamesArr: PathsAndNamesDefinition[] = [];

        if (typeof uiConfiguration.gab === 'undefined') {
            noConfig = true;
        } else {
            const gabConfig: GabConfig = uiConfiguration.gab
                .find((config) => config.artifactType === artifact.artifactType);
            if (typeof gabConfig === 'undefined') {
                noConfig = true;
            } else {
                pathsandnamesArr = gabConfig.pathsAndNamesDefinitions;
            }
        }


        if (noConfig) {
            const msg = this.translateService.translate('DEPLOYMENT_ARTIFACT_GAB_NO_CONFIG');
            this.modalService.openAlertModal(titleStr, msg);
        }

        const modalInputs = {
            pathsandnames: pathsandnamesArr,
            artifactid: artifact.esId,
            resourceid: this.componentId
        };

        this.modalService.openCustomModal(modalConfig, GenericArtifactBrowserComponent, modalInputs);

    }

}

import { Component, Input } from '@angular/core';
import { Store } from '@ngxs/store';
import { ArtifactModel, Component as TopologyTemplate, FullComponentInstance, Resource } from 'app/models';
import { WorkspaceService } from 'app/ng2/pages/workspace/workspace.service';
import { ResourceNamePipe } from 'app/ng2/pipes/resource-name.pipe';
import { ComponentInstanceServiceNg2 } from 'app/ng2/services/component-instance-services/component-instance.service';
import { TopologyTemplateService } from 'app/ng2/services/component-services/topology-template.service';
import { ArtifactType } from 'app/utils';
import * as _ from 'lodash';
import { SdcUiServices } from 'onap-ui-angular';
import { Observable } from 'rxjs/Observable';
import { map } from 'rxjs/operators';
import { ArtifactsService } from '../../../../../components/forms/artifacts-form/artifacts.service';
import { GetArtifactsByTypeAction } from '../../../../../store/actions/artifacts.action';
import { GetInstanceArtifactsByTypeAction } from '../../../../../store/actions/instance-artifacts.actions';
import { ArtifactsState } from '../../../../../store/states/artifacts.state';
import { InstanceArtifactsState } from '../../../../../store/states/instance-artifacts.state';
import { SelectedComponentType, TogglePanelLoadingAction } from '../../../common/store/graph.actions';
import { CompositionService } from '../../../composition.service';

@Component({
    selector: 'artifacts-tab',
    styleUrls: ['./artifacts-tab.component.less'],
    templateUrl: './artifacts-tab.component.html',
    providers: [SdcUiServices.ModalService]
})

export class ArtifactsTabComponent {

    @Input() component: FullComponentInstance | TopologyTemplate;
    @Input() isViewOnly: boolean;
    @Input() input: any;
    @Input() componentType: SelectedComponentType;

    public title: string;
    public type: string;
    public isComponentInstanceSelected: boolean;
    public artifacts$: Observable<ArtifactModel[]>;
    private topologyTemplateType: string;
    private topologyTemplateId: string;
    private heatToEnv: Map<string, ArtifactModel>;
    private resourceType: string;
    private isComplex: boolean;

    constructor(private store: Store,
                private compositionService: CompositionService,
                private workspaceService: WorkspaceService,
                private componentInstanceService: ComponentInstanceServiceNg2,
                private topologyTemplateService: TopologyTemplateService,
                private artifactService: ArtifactsService) {
        this.heatToEnv = new Map();
    }

    ngOnInit() {
        this.topologyTemplateType = this.workspaceService.metadata.componentType;
        this.topologyTemplateId = this.workspaceService.metadata.uniqueId;
        this.type = this.input.type;
        this.title = this.getTitle(this.type);
        this.isComponentInstanceSelected = this.componentType === SelectedComponentType.COMPONENT_INSTANCE;
        this.resourceType = this.component['resourceType'];
        this.isComplex = this.component.isComplex();
        this.loadArtifacts();
    }

    public addOrUpdate = (artifact: ArtifactModel): void => {
        if (this.isComponentInstanceSelected) {
            this.artifactService.openArtifactModal(this.topologyTemplateId, this.topologyTemplateType, artifact, this.type, this.isViewOnly, this.component.uniqueId);
        } else {
            this.artifactService.openArtifactModal(this.topologyTemplateId, this.topologyTemplateType, artifact, this.type, this.isViewOnly);
        }
    }

    public updateEnvParams = (artifact: ArtifactModel) => {
        if (this.isComponentInstanceSelected) {
            this.artifactService.openUpdateEnvParams(this.topologyTemplateType, this.topologyTemplateId, this.heatToEnv.get(artifact.uniqueId), this.component.uniqueId);
        } else {
            this.artifactService.openUpdateEnvParams(this.topologyTemplateType, this.topologyTemplateId, artifact);
        }
    }

    public viewEnvParams = (artifact: ArtifactModel) => {
        if (this.isComponentInstanceSelected) {
            this.artifactService.openViewEnvParams(this.topologyTemplateType, this.topologyTemplateId, this.heatToEnv.get(artifact.uniqueId), this.component.uniqueId);
        } else {
            this.artifactService.openViewEnvParams(this.topologyTemplateType, this.topologyTemplateId, artifact);
        }
    }

    public getEnvArtifact = (heatArtifact: ArtifactModel, artifacts: ArtifactModel[]): ArtifactModel => {
        const envArtifact = _.find(artifacts, (item: ArtifactModel) => {
            return item.generatedFromId === heatArtifact.uniqueId;
        });
        if (envArtifact && heatArtifact) {
            envArtifact.artifactDisplayName = heatArtifact.artifactDisplayName;
            envArtifact.timeout = heatArtifact.timeout;
        }
        return envArtifact;
    }

    public delete = (artifact: ArtifactModel): void => {
        if (this.isComponentInstanceSelected) {
            this.artifactService.deleteArtifact(this.topologyTemplateType, this.topologyTemplateId, artifact, this.component.uniqueId);
        } else {
            this.artifactService.deleteArtifact(this.topologyTemplateType, this.topologyTemplateId, artifact);
        }
    }

    public isVfOrPnf(): boolean {
        if (this.component.isResource()){
            if (this.resourceType) {
                return this.resourceType === 'VF' || this.resourceType == 'PNF';
            }
            return false;
        }

        return false;
    }

    private envArtifactOf(artifact: ArtifactModel): ArtifactModel {
        return this.heatToEnv.get(artifact.uniqueId);
    }

    private isLicenseArtifact = (artifact: ArtifactModel): boolean => {
        let isLicense: boolean = false;
        if (this.component.isResource && (this.component as Resource).isCsarComponent) {
            if (ArtifactType.VENDOR_LICENSE === artifact.artifactType || ArtifactType.VF_LICENSE === artifact.artifactType) {
                isLicense = true;
            }
        }

        return isLicense;
    }

    private getTitle = (artifactType: string): string => {
        switch (artifactType) {
            case ArtifactType.SERVICE_API:
                return 'API Artifacts';
            case ArtifactType.DEPLOYMENT:
                return 'Deployment Artifacts';
            case ArtifactType.INFORMATION:
                return 'Informational Artifacts';
            default:
                return ResourceNamePipe.getDisplayName(artifactType) + ' Artifacts';
        }
    }

    private loadArtifacts = (forceLoad?: boolean): void => {

        this.store.dispatch(new TogglePanelLoadingAction({isLoading: true}));

        let action;
        if (this.isComponentInstanceSelected) {
            action = new GetInstanceArtifactsByTypeAction(({
                componentType: this.topologyTemplateType,
                componentId: this.topologyTemplateId,
                instanceId: this.component.uniqueId,
                artifactType: this.type
            }));
        } else {
            action = new GetArtifactsByTypeAction({
                componentType: this.topologyTemplateType,
                componentId: this.topologyTemplateId,
                artifactType: this.type
            });
        }
        this.store.dispatch(action).subscribe(() => {
            const stateSelector = this.isComponentInstanceSelected ? InstanceArtifactsState.getArtifactsByType : ArtifactsState.getArtifactsByType;
            this.artifacts$ = this.store.select(stateSelector).pipe(map((filterFn) => filterFn(this.type))).pipe(map((artifacts) => {
                _.forEach(artifacts, (artifact: ArtifactModel): void => {
                    const envArtifact = this.getEnvArtifact(artifact, artifacts); // Extract the env artifact (if exist) of the HEAT artifact
                    if (envArtifact) {
                        // Set a mapping between HEAT to HEAT_ENV
                        this.heatToEnv.set(artifact.uniqueId, envArtifact);
                    }
                });
                return _.orderBy(artifacts, ['mandatory', 'artifactDisplayName'], ['desc', 'asc']);
            }));

            this.artifacts$.subscribe((artifacts) => {
                _.forEach(artifacts, (artifact: ArtifactModel) => {
                    artifact.allowDeleteAndUpdate = this.allowDeleteAndUpdateArtifact(artifact);
                });
                if (this.component instanceof FullComponentInstance) {
                    this.compositionService.updateInstance(this.component);
                }
            });
            this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
        }, () => {
            this.store.dispatch(new TogglePanelLoadingAction({isLoading: false}));
        });
    }

    private allowDeleteAndUpdateArtifact = (artifact: ArtifactModel): boolean => {
        if (!this.isViewOnly) {
            if (artifact.artifactGroupType === ArtifactType.DEPLOYMENT) {
                return !artifact.isFromCsar;
            } else {

                return (!artifact.isHEAT() && !artifact.isThirdParty() && !this.isLicenseArtifact(artifact));
            }
        }
        return false;
    }
}

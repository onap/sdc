/**
 * Created by ob0695 on 7/17/2018.
 */
import { Action, Selector, State, StateContext } from '@ngxs/store';
import * as _ from 'lodash';
import { tap } from 'rxjs/operators';
import { ArtifactModel } from '../../../models/artifacts';
import { ArtifactGroupType } from '../../../utils/constants';
import { TopologyTemplateService } from '../../services/component-services/topology-template.service';
import { ComponentGenericResponse } from '../../services/responses/component-generic-response';
import { ServiceGenericResponse } from '../../services/responses/service-generic-response';
import { CreateOrUpdateArtifactAction, DeleteArtifactAction, GetArtifactsByTypeAction } from '../actions/artifacts.action';

export interface ArtifactsStateModel {
    artifacts: ArtifactModel[];
    deploymentArtifacts: ArtifactModel[];
    toscaArtifacts: ArtifactModel[];
    serviceApiArtifacts: ArtifactModel[];
}

@State<ArtifactsStateModel>({
    name: 'artifacts',
    defaults: {
        artifacts: [],
        deploymentArtifacts: [],
        toscaArtifacts: [],
        serviceApiArtifacts: []
    }
})

export class ArtifactsState {

    constructor(protected topologyTemplateService: TopologyTemplateService) {
    }

    @Selector()
    static getEnvArtifact(state: ArtifactsStateModel, heatEnvArtifact: ArtifactModel) {
        return (heatEnvArtifact: ArtifactModel) => {
            _.find(state.deploymentArtifacts, (artifact)=> {
                return artifact.generatedFromId === heatEnvArtifact.uniqueId
            })
        };
    }

    @Selector()
    static getArtifactsByType(state: ArtifactsStateModel, type: string) {
        return (type: string) => {
            switch (type) {
                case ArtifactGroupType.TOSCA:
                    return state.toscaArtifacts;
                case ArtifactGroupType.INFORMATION:
                    return state.artifacts;
                case ArtifactGroupType.DEPLOYMENT:
                    return state.deploymentArtifacts;
                case ArtifactGroupType.SERVICE_API:
                    return state.serviceApiArtifacts;
            }
        };
    }

    private updateArtifactState = (artifactsState: ArtifactModel[], artifactToUpdate: ArtifactModel, updatedArtifact: ArtifactModel) => {
        if (!artifactToUpdate.uniqueId) { // Create Artifact
            return [...artifactsState, updatedArtifact]
        } else { // Update Artifact
            let artifactToUpdateIndex = _.findIndex(artifactsState, (artifact) => {
                return artifact.uniqueId === artifactToUpdate.uniqueId
            })
            let artifacts = Array.from(artifactsState);
            artifacts[artifactToUpdateIndex] = updatedArtifact;
            return [...artifacts];
        }
    }

    @Action(GetArtifactsByTypeAction)
    getArtifactsByType({getState, patchState}: StateContext<ArtifactsStateModel>, action: GetArtifactsByTypeAction) {
        const state = getState();
        return this.topologyTemplateService.getArtifactsByType(action.payload.componentType, action.payload.componentId, action.payload.artifactType)
            .pipe(tap((resp: ComponentGenericResponse) => {
                switch (action.payload.artifactType) {
                    case ArtifactGroupType.INFORMATION:
                        patchState({
                            artifacts: <ArtifactModel[]>_.values(resp.artifacts)
                        });

                    case ArtifactGroupType.DEPLOYMENT:
                        patchState({
                            deploymentArtifacts: <ArtifactModel[]>_.values(resp.deploymentArtifacts)
                        });

                    case ArtifactGroupType.TOSCA:
                        patchState({
                            toscaArtifacts: <ArtifactModel[]>_.values(resp.toscaArtifacts)
                        });

                    case ArtifactGroupType.SERVICE_API:
                        patchState({
                            serviceApiArtifacts: <ArtifactModel[]>_.values((<ServiceGenericResponse>resp).serviceApiArtifacts)
                        });
                }
            }));
    }

    @Action(CreateOrUpdateArtifactAction)
    createOrUpdateArtifact({getState, patchState}: StateContext<ArtifactsStateModel>, action: CreateOrUpdateArtifactAction) {
        const state = getState();
        return this.topologyTemplateService.addOrUpdateArtifact(action.payload.componentType, action.payload.componentId, action.payload.artifact)
            .pipe(tap((resp: ArtifactModel) => {

                switch (resp.artifactGroupType) {
                    case ArtifactGroupType.DEPLOYMENT:
                        patchState({
                            deploymentArtifacts: this.updateArtifactState(state.deploymentArtifacts, action.payload.artifact, resp)
                        });

                    case ArtifactGroupType.INFORMATION:
                        patchState({
                            artifacts: this.updateArtifactState(state.artifacts, action.payload.artifact, resp)
                        });
                }
            }));
    }

    @Action(DeleteArtifactAction)
    deleteArtifact({getState, patchState}: StateContext<ArtifactsStateModel>, action: DeleteArtifactAction) {
        const state = getState();
        return this.topologyTemplateService.deleteArtifact(action.payload.componentId, action.payload.componentType, action.payload.artifact.uniqueId, action.payload.artifact.artifactLabel)
            .pipe(tap((resp: ArtifactModel) => {
                switch (resp.artifactGroupType) {
                    case ArtifactGroupType.DEPLOYMENT:
                        patchState({
                            deploymentArtifacts: state.deploymentArtifacts.filter(({uniqueId}) => uniqueId !== action.payload.artifact.uniqueId)
                        });
                    case ArtifactGroupType.INFORMATION:
                        patchState({
                            artifacts: state.artifacts.filter(({uniqueId}) => uniqueId !== action.payload.artifact.uniqueId)
                        });
                }
            }));
    }
}
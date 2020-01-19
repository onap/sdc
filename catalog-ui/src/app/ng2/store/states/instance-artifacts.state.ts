/**
 * Created by ob0695 on 7/17/2018.
 */
import { Action, Selector, State, StateContext, Store } from '@ngxs/store';
import * as _ from 'lodash';
import { tap } from 'rxjs/operators';
import { ArtifactModel } from '../../../models/artifacts';
import { ArtifactGroupType } from '../../../utils/constants';
import { ComponentInstanceServiceNg2 } from '../../services/component-instance-services/component-instance.service';
import { ComponentGenericResponse } from '../../services/responses/component-generic-response';
import {
    CreateInstanceArtifactAction,
    DeleteInstanceArtifactAction,
    GetInstanceArtifactsByTypeAction,
    UpdateInstanceArtifactAction
} from '../actions/instance-artifacts.actions';
import { ArtifactsStateModel } from './artifacts.state';

export interface InstanceArtifactsStateModel {
    artifacts: ArtifactModel[];
    deploymentArtifacts: ArtifactModel[];
}

@State<InstanceArtifactsStateModel>({
    name: 'instance_artifacts',
    defaults: {
        artifacts: [],
        deploymentArtifacts: []
    }
})
export class InstanceArtifactsState {

    constructor(private store: Store, protected componentInstanceService: ComponentInstanceServiceNg2) {
    }

    @Selector()
    static getArtifactsByType(state: InstanceArtifactsStateModel) {
        return (type: string) => {
            switch (type) {
                case  ArtifactGroupType.INFORMATION:
                    return state.artifacts;
                case ArtifactGroupType.DEPLOYMENT:
                    return state.deploymentArtifacts;
            }
        };
    }

    @Action(GetInstanceArtifactsByTypeAction)
    getInstanceArtifactsByType({getState, patchState}: StateContext<InstanceArtifactsStateModel>, action: GetInstanceArtifactsByTypeAction) {
        const state = getState();
        return this.componentInstanceService.getComponentInstanceArtifactsByGroupType(action.payload.componentType, action.payload.componentId, action.payload.instanceId, action.payload.artifactType)
            .pipe(tap((resp: ComponentGenericResponse) => {
                switch (action.payload.artifactType) {
                    case ArtifactGroupType.INFORMATION:
                        patchState({
                            artifacts: _.values(resp) as ArtifactModel[]
                        });
                        break;
                    case ArtifactGroupType.DEPLOYMENT:
                        patchState({
                            deploymentArtifacts: _.values(resp) as ArtifactModel[]
                        });
                        break;
                }
            }));
    }

    @Action(CreateInstanceArtifactAction)
    createArtifact({getState, patchState}: StateContext<ArtifactsStateModel>, action: CreateInstanceArtifactAction) {
        const state = getState();
        return this.componentInstanceService.addInstanceArtifact(action.payload.componentType, action.payload.componentId, action.payload.instanceId, action.payload.artifact)
            .pipe(tap((resp: ArtifactModel) => {
                switch (resp.artifactGroupType) {
                    case ArtifactGroupType.DEPLOYMENT:
                        patchState({
                            deploymentArtifacts: [...state.deploymentArtifacts, resp]
                        });
                        break;
                    case ArtifactGroupType.INFORMATION:
                        patchState({
                            artifacts: [...state.artifacts, resp]
                        });
                        break;
                }
            }));
    }

    @Action(UpdateInstanceArtifactAction)
    updateArtifact({getState, patchState}: StateContext<ArtifactsStateModel>, action: UpdateInstanceArtifactAction) {
        const state = getState();
        return this.componentInstanceService.updateInstanceArtifact(action.payload.componentType, action.payload.componentId, action.payload.instanceId, action.payload.artifact)
            .pipe(tap((resp: ArtifactModel) => {
                switch (resp.artifactGroupType) {
                    case ArtifactGroupType.DEPLOYMENT:
                        // We cannot simply update the updated artifact state because updating a deployment ENV file may cause an update to his parent HEAT
                        // file.
                        // Just dispatch an action to refresh the deployment artifacts list
                        this.store.dispatch(new GetInstanceArtifactsByTypeAction(({
                            componentType: action.payload.componentType,
                            componentId: action.payload.componentId,
                            instanceId: action.payload.instanceId,
                            artifactType: ArtifactGroupType.DEPLOYMENT
                        })));
                        break;
                    case ArtifactGroupType.INFORMATION:
                        patchState({
                            artifacts: this.updateInstanceArtifactState(state.artifacts, action.payload.artifact, resp)
                        });
                        break;
                }
            }));
    }

    @Action(DeleteInstanceArtifactAction)
    deleteInstanceArtifact({getState, patchState}: StateContext<ArtifactsStateModel>, action: DeleteInstanceArtifactAction) {
        const state = getState();
        return this.componentInstanceService.
                    deleteInstanceArtifact(action.payload.componentId, action.payload.componentType, action.payload.instanceId, action.payload.artifact.uniqueId, action.payload.artifact.artifactLabel)
            .pipe(tap((resp: ArtifactModel) => {
                switch (resp.artifactGroupType) {
                    case ArtifactGroupType.DEPLOYMENT:
                        patchState({
                            deploymentArtifacts: state.deploymentArtifacts.filter(({uniqueId}) => uniqueId !== action.payload.artifact.uniqueId)
                        });
                        break;
                    case ArtifactGroupType.INFORMATION:
                        patchState({
                            artifacts: state.artifacts.filter(({uniqueId}) => uniqueId !== action.payload.artifact.uniqueId)
                        });
                        break;
                }
            }));
    }

    private updateInstanceArtifactState = (artifactsState: ArtifactModel[], artifactToUpdate: ArtifactModel, updatedArtifact: ArtifactModel) => {
        const artifactToUpdateIndex = _.findIndex(artifactsState, (artifact) => {
            return artifact.uniqueId === artifactToUpdate.uniqueId;
        });
        const artifacts = Array.from(artifactsState);
        artifacts[artifactToUpdateIndex] = updatedArtifact;
        const ret = [...artifacts];
        return ret;
    }

}

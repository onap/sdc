import { Store } from '@ngxs/store';
import { Observable } from 'rxjs/Rx';
import { Mock } from 'ts-mockery';
import { ArtifactModel } from '../../../models/artifacts';
import { ArtifactGroupType } from '../../../utils/constants';
import { ComponentInstanceServiceNg2 } from '../../services/component-instance-services/component-instance.service';
import { GetInstanceArtifactsByTypeAction, UpdateInstanceArtifactAction } from '../actions/instance-artifacts.actions';
import { InstanceArtifactsState } from './instance-artifacts.state';

describe('Test Artifact State', () => {

    const heat1 = Mock.of<ArtifactModel>({
        uniqueId: '1', artifactName: 'heat1', timeout: 0, artifactDisplayName: 'heat1', artifactGroupType: ArtifactGroupType.DEPLOYMENT
    });

    const heat1env = Mock.of<ArtifactModel>({
        uniqueId: '2', artifactName: 'heat1env', timeout: 0, generatedFromId: '1', artifactDisplayName: 'heat1env', artifactGroupType: ArtifactGroupType.DEPLOYMENT
    });

    const storeMock = Mock.of<Store>( { dispatch : jest.fn() });

    const artifacts = [
         heat1,
         heat1env
    ];

    /**
     * NGXS Store state before we run the update
     */
    const ngxsState = {
        deploymentArtifacts : artifacts
    };

    /**
     * The ENV artifact that we wish to update
     */
    const updatedArtifact = Mock.of<ArtifactModel>({
        uniqueId: '2', artifactName: 'heat1env', timeout: 33, generatedFromId: '1', artifactDisplayName: 'heat1env-UPDATE', artifactGroupType: ArtifactGroupType.DEPLOYMENT
    });

    const componentInstanceServiceMock: ComponentInstanceServiceNg2 = Mock.of<ComponentInstanceServiceNg2>({
        updateInstanceArtifact: jest.fn().mockImplementation(() => Observable.of(updatedArtifact)),
        getComponentInstanceArtifactsByGroupType: jest.fn().mockImplementation(() => Observable.of([heat1, updatedArtifact]))
    });

    const actionMock: UpdateInstanceArtifactAction = Mock.of<UpdateInstanceArtifactAction>({
        payload: {
            componentType: '',
            componentId: '',
            instanceId: '',
            artifact: updatedArtifact
        }
    });

    it('Test that HEAT timeout is updated', () => {
        const state: InstanceArtifactsState = new InstanceArtifactsState(storeMock, componentInstanceServiceMock);
        const context = { getState: jest.fn().mockImplementation(() => ngxsState), patchState: jest.fn(), setState: jest.fn(), dispatch: jest.fn() };
        state.updateArtifact(context, actionMock ).subscribe( (v) => console.log('OK'));
        expect(storeMock.dispatch).toBeCalled();
    });

});

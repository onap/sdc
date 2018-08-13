/*
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import store from 'sdc-app/AppStore.js';
import FlowsActions from 'sdc-app/flows/FlowsActions.js';
import { enums } from 'sdc-app/flows/FlowsConstants.js';

import {
    FlowCreateFactory,
    FlowPostRequestFactory,
    FlowPostResponseFactory,
    FlowFetchRequestFactory,
    FlowFetchResponseFactory,
    FlowDeleteRequestFactory,
    FlowUpdateRequestFactory
} from 'test-utils/factories/flows/FlowsFactories.js';

import { buildFromExistingObject } from 'test-utils/Util.js';

const NEW_FLOW = true;

let assertFlowDataAfterCreateFetchAndUpdate = data => {
    let { flowList, serviceID, diagramType } = store.getState().flows;
    expect(serviceID).toBe(data.serviceID);
    expect(diagramType).toBe(data.artifactType);
    let uniqueId = data.uniqueId || `${data.serviceID}.${data.artifactName}`;
    let index = flowList.findIndex(flow => flow.uniqueId === uniqueId);
    expect(index).not.toBe(-1);
};

describe('Workflows and Management Flows Module Tests:', function() {
    it('empty artifact should open flow creation modal', () => {
        const artifacts = {};

        deepFreeze(store.getState());
        deepFreeze(artifacts);
        FlowsActions.fetchFlowArtifacts(store.dispatch, {
            artifacts,
            diagramType: enums.WORKFLOW,
            participants: [],
            serviceID: '1234'
        });
        let state = store.getState();
        expect(state.modal).toBeDefined();
    });

    it('Close flow details editor modal', () => {
        deepFreeze(store.getState());
        FlowsActions.closeEditCreateWFModal(store.dispatch);
        let state = store.getState();
        expect(state.modal).toBeFalsy();
    });

    it('Get Flows List from loaded artifact', () => {
        deepFreeze(store.getState());

        const artifacts = {
            test1: FlowPostResponseFactory.build({ artifactName: 'test1' }),
            kukuriku: FlowPostResponseFactory.build({
                artifactType: 'PUPPET',
                artifactName: 'kukuriku'
            }),
            test3: FlowPostResponseFactory.build({ artifactName: 'test3' })
        };

        const artifactsArray = Object.keys(artifacts).map(artifact => artifact);

        deepFreeze(artifacts);

        deepFreeze(store.getState());

        let actionData = {
            artifacts,
            diagramType: enums.WORKFLOW,
            participants: [],
            serviceID: '1234'
        };
        FlowsActions.fetchFlowArtifacts(store.dispatch, actionData);

        let state = store.getState();
        expect(state.flows.flowList.length).toEqual(artifactsArray.length);
        expect(state.flows.flowParticipants).toEqual(actionData.participants);
        expect(state.flows.serviceID).toBe(actionData.serviceID);
        expect(state.flows.diagramType).toBe(actionData.diagramType);
    });

    it('Add New Flow', () => {
        deepFreeze(store.getState());

        const flowCreateData = FlowCreateFactory.build();
        let expectedDataToBeSentInTheRequest = buildFromExistingObject(
            FlowPostRequestFactory,
            flowCreateData
        );

        mockRest.addHandler('post', ({ data, baseUrl, options }) => {
            expect(baseUrl).toBe(
                `/sdc1/feProxy/rest/v1/catalog/services/${
                    flowCreateData.serviceID
                }/artifacts/`
            );
            expect(data.artifactLabel).toBe(
                expectedDataToBeSentInTheRequest.artifactLabel
            );
            expect(data.artifactName).toBe(
                expectedDataToBeSentInTheRequest.artifactName
            );
            expect(data.artifactType).toBe(
                expectedDataToBeSentInTheRequest.artifactType
            );
            expect(data.description).toBe(
                expectedDataToBeSentInTheRequest.description
            );
            expect(data.payloadData).toBe(
                expectedDataToBeSentInTheRequest.payloadData
            );
            expect(options.md5).toBe(true);
            return buildFromExistingObject(
                FlowPostResponseFactory,
                expectedDataToBeSentInTheRequest
            );
        });

        return FlowsActions.createOrUpdateFlow(
            store.dispatch,
            { flow: flowCreateData },
            NEW_FLOW
        ).then(() => {
            assertFlowDataAfterCreateFetchAndUpdate(flowCreateData);
        });
    });

    it('Fetch Flow', () => {
        deepFreeze(store.getState());

        const flowFetchData = FlowFetchRequestFactory.build();

        mockRest.addHandler('fetch', ({ baseUrl }) => {
            //sdc1/feProxy/rest/v1/catalog/services/338d75f0-aec8-4eb4-89c9-8733fcd9bf3b/artifacts/338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi
            expect(baseUrl).toBe(
                `/sdc1/feProxy/rest/v1/catalog/services/${
                    flowFetchData.serviceID
                }/artifacts/${flowFetchData.uniqueId}`
            );
            return buildFromExistingObject(
                FlowFetchResponseFactory,
                flowFetchData
            );
        });

        return FlowsActions.fetchArtifact(store.dispatch, {
            flow: flowFetchData
        }).then(() => {
            assertFlowDataAfterCreateFetchAndUpdate(flowFetchData);
        });
    });

    it('Update Existing Flow', () => {
        deepFreeze(store.getState());
        const flowUpdateData = FlowUpdateRequestFactory.build();

        mockRest.addHandler('post', ({ baseUrl }) => {
            expect(baseUrl).toBe(
                `/sdc1/feProxy/rest/v1/catalog/services/${
                    flowUpdateData.serviceID
                }/artifacts/${flowUpdateData.uniqueId}`
            );

            return buildFromExistingObject(
                FlowPostResponseFactory,
                flowUpdateData
            );
        });

        return FlowsActions.createOrUpdateFlow(
            store.dispatch,
            { flow: flowUpdateData },
            !NEW_FLOW
        ).then(() => {
            assertFlowDataAfterCreateFetchAndUpdate(flowUpdateData);
        });
    });

    it('Delete Flow', () => {
        deepFreeze(store.getState());

        const flowDeleteData = FlowDeleteRequestFactory.build();

        mockRest.addHandler('destroy', ({ baseUrl }) => {
            expect(baseUrl).toBe(
                `/sdc1/feProxy/rest/v1/catalog/services/${
                    flowDeleteData.serviceID
                }/artifacts/${flowDeleteData.uniqueId}`
            );
            return {};
        });

        return FlowsActions.deleteFlow(store.dispatch, {
            flow: flowDeleteData
        }).then(() => {
            let { flowList } = store.getState().flows;
            let index = flowList.findIndex(
                flow => flow.uniqueId === flowDeleteData.uniqueId
            );
            expect(index).toBe(-1);
        });
    });
});

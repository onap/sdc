/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

import expect from 'expect';
import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import store from 'sdc-app/AppStore.js';
import FlowsActions from 'sdc-app/flows/FlowsActions.js';
import {enums} from 'sdc-app/flows/FlowsConstants.js';

const NEW_FLOW = true;

let assertFlowDataAfterCreateFetchAndUpdate = (data) => {
	let {flowList, serviceID, diagramType} = store.getState().flows;
	expect(serviceID).toBe(data.serviceID);
	expect(diagramType).toBe(data.artifactType);
	let uniqueId = data.uniqueId || `${data.serviceID}.${data.artifactName}`;
	let index = flowList.findIndex(flow => flow.uniqueId === uniqueId);
	expect(index).toNotBe(-1);
};

describe('Workflows and Management Flows Module Tests:', function () {


	it('empty artifact should open flow creation modal', done => {

		const artifacts = {};

		deepFreeze(store.getState());
		deepFreeze(artifacts);
		FlowsActions.fetchFlowArtifacts(store.dispatch, {
			artifacts,
			diagramType: enums.WORKFLOW,
			participants: [],
			serviceID: '1234'
		});
		setTimeout(() => {
			let state = store.getState();
			expect(state.flows.isDisplayModal).toBe(true);
			expect(state.flows.isModalInEditMode).toBe(false);
			done();
		}, 50);
	});

	it('Close flow details editor modal', done => {
		deepFreeze(store.getState());
		FlowsActions.closeFlowDetailsEditor(store.dispatch);
		setTimeout(() => {
			let state = store.getState();
			expect(state.flows.isDisplayModal).toBe(false);
			expect(state.flows.isModalInEditMode).toBe(false);
			done();
		}, 50);
	});

	it('Get Flows List from loaded artifact', done => {

		deepFreeze(store.getState());

		const artifacts = {
			'test1': {
				'uniqueId': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.test1',
				'artifactType': 'NETWORK_CALL_FLOW',
				'artifactName': 'test1',
				'artifactChecksum': 'MzYxZGIyNjlkNjRmMTM4ZWMxM2FjNDUyNDQwMTI3NzM=',
				'attUidLastUpdater': 'cs0008',
				'updaterFullName': 'Carlos Santana',
				'creationDate': 1468164899724,
				'lastUpdateDate': 1468164899724,
				'esId': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.test1',
				'artifactLabel': 'test1',
				'artifactCreator': 'cs0008',
				'description': 'www',
				'mandatory': false,
				'artifactDisplayName': 'test1',
				'serviceApi': false,
				'artifactGroupType': 'INFORMATIONAL',
				'timeout': 0,
				'artifactVersion': '1',
				'artifactUUID': '28d4cb95-bb46-4666-b858-e333671e6444',
				'payloadUpdateDate': 1468164900232
			},
			'kukuriku': {
				'uniqueId': '0280b577-2c7b-426e-b7a2-f0dc16508c37.kukuriku',
				'artifactType': 'PUPPET',
				'artifactName': 'fuel.JPG',
				'artifactChecksum': 'OWEyYTVjMWFiNWQ4ZDIwZDUxYTE3Y2EzZmI3YTYyMjA=',
				'attUidLastUpdater': 'cs0008',
				'updaterFullName': 'Carlos Santana',
				'creationDate': 1467877631512,
				'lastUpdateDate': 1467877631512,
				'esId': '0280b577-2c7b-426e-b7a2-f0dc16508c37.kukuriku',
				'artifactLabel': 'kukuriku',
				'artifactCreator': 'cs0008',
				'description': 'asdfasdf',
				'mandatory': false,
				'artifactDisplayName': 'kukuriku',
				'serviceApi': false,
				'artifactGroupType': 'INFORMATIONAL',
				'timeout': 0,
				'artifactVersion': '1',
				'artifactUUID': 'c1e98336-03f4-4b2a-b6a5-08eca44fe3c4',
				'payloadUpdateDate': 1467877632722
			},
			'test3': {
				'uniqueId': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.test3',
				'artifactType': 'NETWORK_CALL_FLOW',
				'artifactName': 'test3',
				'artifactChecksum': 'ZmJkZGU1M2M2ZWUxZTdmNGU5NTNiNTdiYTAzMmM1YzU=',
				'attUidLastUpdater': 'cs0008',
				'updaterFullName': 'Carlos Santana',
				'creationDate': 1468165068570,
				'lastUpdateDate': 1468165128827,
				'esId': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.test3',
				'artifactLabel': 'test3',
				'artifactCreator': 'cs0008',
				'description': '333',
				'mandatory': false,
				'artifactDisplayName': 'test3',
				'serviceApi': false,
				'artifactGroupType': 'INFORMATIONAL',
				'timeout': 0,
				'artifactVersion': '2',
				'artifactUUID': '0988027c-d19c-43db-8315-2c68fc773775',
				'payloadUpdateDate': 1468165129335
			}
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

		setTimeout(() => {
			let state = store.getState();
			expect(state.flows.isDisplayModal).toBe(false);
			expect(state.flows.isModalInEditMode).toBe(false);
			expect(state.flows.flowList.length).toEqual(artifactsArray.length);
			expect(state.flows.flowParticipants).toEqual(actionData.participants);
			expect(state.flows.serviceID).toBe(actionData.serviceID);
			expect(state.flows.diagramType).toBe(actionData.diagramType);
			done();
		}, 50);

	});


	it('Add New Flow', done => {

		deepFreeze(store.getState());

		const flowCreateData = deepFreeze({
			artifactName: 'zizizi',
			artifactType: 'WORKFLOW',
			description: 'aslkjdfl asfdasdf',
			serviceID: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b',
		});


		let expectedDataToBeSentInTheRequest = {
			artifactGroupType: 'INFORMATIONAL',
			artifactLabel: 'zizizi',
			artifactName: 'zizizi',
			artifactType: 'WORKFLOW',
			description: 'aslkjdfl asfdasdf',
			payloadData: 'eyJWRVJTSU9OIjp7Im1ham9yIjoxLCJtaW5vciI6MH0sImRlc2NyaXB0aW9uIjoiYXNsa2pkZmwgYXNmZGFzZGYifQ=='
		};
		mockRest.addHandler('create', ({data, baseUrl, options}) => {
			expect(baseUrl).toBe(`/sdc1/feProxy/rest/v1/catalog/services/${flowCreateData.serviceID}/artifacts/`);
			expect(data.artifactLabel).toBe(expectedDataToBeSentInTheRequest.artifactLabel);
			expect(data.artifactName).toBe(expectedDataToBeSentInTheRequest.artifactName);
			expect(data.artifactType).toBe(expectedDataToBeSentInTheRequest.artifactType);
			expect(data.description).toBe(expectedDataToBeSentInTheRequest.description);
			expect(data.payloadData).toBe(expectedDataToBeSentInTheRequest.payloadData);
			expect(options.md5).toBe(true);
			return {
				artifactChecksum: 'NjBmYjc4NGM5MWIwNmNkMDhmMThhMDAwYmQxYjBiZTU=',
				artifactCreator: 'cs0008',
				artifactDisplayName: 'zizizi',
				artifactGroupType: 'INFORMATIONAL',
				artifactLabel: 'zizizi',
				artifactName: 'zizizi',
				artifactType: 'WORKFLOW',
				artifactUUID: '0295a7cc-8c02-4105-9d7e-c30ce67ecd07',
				artifactVersion: '1',
				attUidLastUpdater: 'cs0008',
				creationDate: 1470144601623,
				description: 'aslkjdfl asfdasdf',
				esId: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
				lastUpdateDate: 1470144601623,
				mandatory: false,
				payloadUpdateDate: 1470144602131,
				serviceApi: false,
				timeout: 0,
				uniqueId: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
				updaterFullName: 'Carlos Santana',
			};
		});

		FlowsActions.createOrUpdateFlow(store.dispatch, {flow: flowCreateData}, NEW_FLOW);

		setTimeout(() => {
			assertFlowDataAfterCreateFetchAndUpdate(flowCreateData);
			done();
		}, 50);
	});

	it('Fetch Flow', done => {

		deepFreeze(store.getState());

		const flowFetchData = {
			artifactName: 'zizizi',
			artifactType: 'WORKFLOW',
			description: 'aslkjdfl asfdasdf',
			serviceID: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b',
			uniqueId: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
			participants: []
		};

		mockRest.addHandler('fetch', ({baseUrl}) => {
			//sdc1/feProxy/rest/v1/catalog/services/338d75f0-aec8-4eb4-89c9-8733fcd9bf3b/artifacts/338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi
			expect(baseUrl).toBe(`/sdc1/feProxy/rest/v1/catalog/services/${flowFetchData.serviceID}/artifacts/${flowFetchData.uniqueId}`);
			return {
				artifactName: 'zizizi',
				base64Contents: 'eyJWRVJTSU9OIjp7Im1ham9yIjoxLCJtaW5vciI6MH0sImRlc2NyaXB0aW9uIjoiYXNsa2pkZmwgYXNmZGFzZGYifQ=='
			};
		});

		FlowsActions.fetchArtifact(store.dispatch, {flow: flowFetchData});

		setTimeout(() => {
			assertFlowDataAfterCreateFetchAndUpdate(flowFetchData);
			done();
		}, 50);
	});

	it('Update Existing Flow', done => {

		deepFreeze(store.getState());

		const flowUpdateData = {
			'artifactType': 'WORKFLOW',
			'participants': [
				{
					'id': '1',
					'name': 'Customer'
				},
				{
					'id': '2',
					'name': 'CCD'
				},
				{
					'id': '3',
					'name': 'Infrastructure'
				},
				{
					'id': '4',
					'name': 'MSO'
				},
				{
					'id': '5',
					'name': 'SDN-C'
				},
				{
					'id': '6',
					'name': 'A&AI'
				},
				{
					'id': '7',
					'name': 'APP-C'
				},
				{
					'id': '8',
					'name': 'Cloud'
				},
				{
					'id': '9',
					'name': 'DCAE'
				},
				{
					'id': '10',
					'name': 'ALTS'
				},
				{
					'id': '11',
					'name': 'VF'
				}
			],
			'serviceID': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b',
			'artifactDisplayName': 'zizizi',
			'artifactGroupType': 'INFORMATIONAL',
			'uniqueId': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
			'artifactName': 'zizizi',
			'artifactLabel': 'zizizi',
			'artifactUUID': '0295a7cc-8c02-4105-9d7e-c30ce67ecd07',
			'artifactVersion': '1',
			'creationDate': 1470144601623,
			'lastUpdateDate': 1470144601623,
			'description': 'aslkjdfl asfdasdf',
			'mandatory': false,
			'timeout': 0,
			'esId': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
			'artifactChecksum': 'NjBmYjc4NGM5MWIwNmNkMDhmMThhMDAwYmQxYjBiZTU=',
			'heatParameters': [],
			'sequenceDiagramModel': {
				'diagram': {
					'metadata': {
						'id': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
						'name': 'zizizi',
						'ref': 'BLANK'
					},
					'lifelines': [
						{
							'id': '1',
							'name': 'Customer',
							'index': 1,
							'x': 175
						},
						{
							'id': '2',
							'name': 'CCD',
							'index': 2,
							'x': 575
						},
						{
							'id': '3',
							'name': 'Infrastructure',
							'index': 3,
							'x': 975
						},
						{
							'id': '4',
							'name': 'MSO',
							'index': 4,
							'x': 1375
						},
						{
							'id': '5',
							'name': 'SDN-C',
							'index': 5,
							'x': 1775
						},
						{
							'id': '6',
							'name': 'A&AI',
							'index': 6,
							'x': 2175
						},
						{
							'id': '7',
							'name': 'APP-C',
							'index': 7,
							'x': 2575
						},
						{
							'id': '8',
							'name': 'Cloud',
							'index': 8,
							'x': 2975
						},
						{
							'id': '9',
							'name': 'DCAE',
							'index': 9,
							'x': 3375
						},
						{
							'id': '10',
							'name': 'ALTS',
							'index': 10,
							'x': 3775
						},
						{
							'id': '11',
							'name': 'VF',
							'index': 11,
							'x': 4175
						}
					],
					'steps': [
						{
							'message': {
								'id': '9377-5036-c011-cb95-3a8b-82c6-bbb5-bc84',
								'name': '[Unnamed Message]',
								'type': 'request',
								'from': '1',
								'to': '2',
								'index': 1
							}
						},
						{
							'message': {
								'id': '64c4-4fd1-b1da-4355-a060-6e48-ee47-c85c',
								'name': '[Unnamed Message]',
								'type': 'request',
								'from': '1',
								'to': '2',
								'index': 2
							}
						}
					]
				}
			}
		};

		mockRest.addHandler('create', ({baseUrl}) => {
			expect(baseUrl).toBe(`/sdc1/feProxy/rest/v1/catalog/services/${flowUpdateData.serviceID}/artifacts/${flowUpdateData.uniqueId}`);

			return {
				artifactChecksum: 'MmE5MWJmN2ZlN2FhM2JhMzA0NGQ1ODMyOWFhNWI0NDA=',
				artifactCreator: 'cs0008',
				artifactDisplayName: 'zizizi',
				artifactGroupType: 'INFORMATIONAL',
				artifactLabel: 'zizizi',
				artifactName: 'zizizi',
				artifactType: 'WORKFLOW',
				artifactUUID: '3319335b-969e-4d72-b5a2-409645de6d64',
				artifactVersion: '3',
				attUidLastUpdater: 'cs0008',
				creationDate: 1470144601623,
				description: 'aslkjdfl asfdasdf',
				esId: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
				lastUpdateDate: 1470208425904,
				mandatory: false,
				payloadUpdateDate: 1470208426424,
				serviceApi: false,
				timeout: 0,
				uniqueId: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
				updaterFullName: 'Carlos Santana'
			};
		});

		FlowsActions.createOrUpdateFlow(store.dispatch, {flow: flowUpdateData}, !NEW_FLOW);

		setTimeout(() => {
			assertFlowDataAfterCreateFetchAndUpdate(flowUpdateData);
			done();
		}, 50);
	});

	it('Delete Flow', done => {

		deepFreeze(store.getState());

		const flowDeleteData = deepFreeze({
			artifactName: 'zizizi',
			artifactType: 'WORKFLOW',
			description: 'aslkjdfl asfdasdf',
			serviceID: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b',
			uniqueId: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
			participants: []
		});

		mockRest.addHandler('destroy', ({baseUrl}) => {
			expect(baseUrl).toBe(`/sdc1/feProxy/rest/v1/catalog/services/${flowDeleteData.serviceID}/artifacts/${flowDeleteData.uniqueId}`);
			return {};
		});

		FlowsActions.deleteFlow(store.dispatch, {flow: flowDeleteData});

		setTimeout(() => {
			let {flowList} = store.getState().flows;
			let index = flowList.findIndex(flow => flow.uniqueId === flowDeleteData.uniqueId);
			expect(index).toBe(-1);
			done();
		}, 50);
	});

});


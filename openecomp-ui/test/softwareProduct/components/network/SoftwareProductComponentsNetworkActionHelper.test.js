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

import {expect} from 'chai';
import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import {cloneAndSet} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import SoftwareProductComponentsNetworkActionHelper from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNetworkActionHelper.js';

const softwareProductId = '123';
const componentId = '321';
const nicId = '111';

describe('Software Product Components Network Action Helper Tests', function () {

	it('Fetch NICs List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const NICList = [
			{
				name:'oam01_port_0',
				description:'bbbbbbb',
				networkId:'A0E578751B284D518ED764D5378EA97C',
				id:'96D3648338F94DAA9889E9FBB8E59895',
				networkName:'csb_net'
			},
			{
				name:'oam01_port_1',
				description:'bbbbbbb',
				networkId:'378EA97CA0E578751B284D518ED764D5',
				id:'8E5989596D3648338F94DAA9889E9FBB',
				networkName:'csb_net_2'
			}

		];

		deepFreeze(NICList);

		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network.nicList', NICList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/nics`);
			expect(data).to.deep.equal(undefined);
			expect(options).to.equal(undefined);
			return {results: NICList};
		});

		return SoftwareProductComponentsNetworkActionHelper.fetchNICsList(store.dispatch, {softwareProductId, componentId}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});

	});

	it('open NICE editor', () => {

		const store = storeCreator();
		deepFreeze(store.getState());
		const data = {
			name: 'oam01_port_0',
			description: 'bbbbbbb',
			networkId: 'A0E578751B284D518ED764D5378EA97C',
			networkName: 'csb_net'
		};

		const nic = {id: '444'};
		deepFreeze(data);
		deepFreeze(nic);

		const expectedData = {...data, id: nic.id};

		deepFreeze(expectedData);

		const network = {
			nicEditor: {
				data: expectedData
			},
			nicList: []
		};

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);

		SoftwareProductComponentsNetworkActionHelper.openNICEditor(store.dispatch, {nic, data});

		return setTimeout(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		}, 100);
	});

	it('close NICE editor', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const network = {
			nicEditor: {},
			nicList: []
		};
		deepFreeze(network);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);

		SoftwareProductComponentsNetworkActionHelper.closeNICEditor(store.dispatch);

		return setTimeout(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		}, 100);
	});

	it('Load NIC data', () => {

		const expectedData = {
			description: 'bbbbbbb',
			name: 'oam01_port_0',
			networkId: 'A0E578751B284D518ED764D5378EA97C',
			networkName: 'csb_net'
		};

		deepFreeze(expectedData);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/nics/${nicId}`);
			expect(data).to.deep.equal(undefined);
			expect(options).to.equal(undefined);
			return (expectedData);
		});

		return SoftwareProductComponentsNetworkActionHelper.loadNICData({softwareProductId, componentId, nicId}).then((data) => {
			expect(data).to.deep.equal(expectedData);
		});
	});


	it('load NIC Questionnaire', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const qdata = {
			protocols: {
				protocolWithHighestTrafficProfile: 'UDP',
				protocols: ['UDP']
			},
			ipConfiguration: {
				ipv4Required: true
			}
		};

		const qschema = {
			$schema: 'http://json-schema.org/draft-04/schema#',
			type: 'object',
			properties: {
				'protocols': {
					type: 'object',
					properties: {}
				}
			}
		};

		deepFreeze(qdata);
		deepFreeze(qschema);


		const network = {
			nicEditor: {
				qdata,
				qschema
			},
			nicList: []
		};
		deepFreeze(network);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/nics/${nicId}/questionnaire`);
			expect(data).to.deep.equal(undefined);
			expect(options).to.equal(undefined);
			return ({data: JSON.stringify(qdata), schema: JSON.stringify(qschema)});
		});

		return SoftwareProductComponentsNetworkActionHelper.loadNICQuestionnaire(store.dispatch, {softwareProductId, componentId, nicId}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('update NIC Data', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const data = {test: '123'};
		deepFreeze(data);

		const network = {
			nicEditor: {
				data
			},
			nicList: []
		};

		deepFreeze(network);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);

		SoftwareProductComponentsNetworkActionHelper.updateNICData(store.dispatch, {deltaData:data});

		return setTimeout(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		}, 100);

	});

	it('update NIC Questionnaire', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const qdata = {
			test: '123'
		};
		const network = {
			nicEditor: {
				qdata,
				qschema: undefined
			},
			nicList: []
		};
		deepFreeze(network);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);

		SoftwareProductComponentsNetworkActionHelper.updateNICQuestionnaire(store.dispatch, {data:qdata});

		return setTimeout(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		}, 100);

	});

	it('save NIC Data And Questionnaire', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const qdata = {
			qtest: '111'
		};
		const data = {
			name: '2222',
			description: 'blabla',
			networkId: '123445'
		};

		const expectedData = {...data, id: nicId};

		const network = {
			nicEditor: {},
			nicList: [
				expectedData
			]
		};

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);
		deepFreeze(expectedStore);

		mockRest.addHandler('save', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/nics/${nicId}/questionnaire`);
			expect(data).to.deep.equal(qdata);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('save', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/nics/${nicId}`);
			expect(data).to.deep.equal(data);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductComponentsNetworkActionHelper.saveNICDataAndQuestionnaire(store.dispatch, {softwareProductId, componentId, qdata, data: expectedData}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});


});

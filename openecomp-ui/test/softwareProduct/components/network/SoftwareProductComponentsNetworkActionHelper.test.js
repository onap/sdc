/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import {cloneAndSet} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import SoftwareProductComponentsNetworkActionHelper from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNetworkActionHelper.js';

import {VSPComponentsNicFactory,
    VSPComponentsNicPostFactory,
	VSPComponentsNetworkFactory,
	VSPComponentsNetworkQDataFactory,
	VSPComponentsNetworkDataMapFactory,
	VSPComponentsNicFactoryGenericFieldInfo} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsNetworkFactories.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import VSPQSchemaFactory from 'test-utils/factories/softwareProduct/SoftwareProductQSchemaFactory.js';
import {forms} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';

const softwareProductId = '123';
const componentId = '321';
const nicId = '111';
const version = VersionFactory.build();

describe('Software Product Components Network Action Helper Tests', function () {

	it('Fetch NICs List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const NICList = VSPComponentsNicFactory.buildList(2);

		deepFreeze(NICList);

		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network.nicList', NICList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/nics`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: NICList};
		});

		return SoftwareProductComponentsNetworkActionHelper.fetchNICsList(store.dispatch, {softwareProductId, version, componentId}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});

	});

	it('Add NIC', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const NICPostRequest = VSPComponentsNicPostFactory.build();

		const expectedNIC = VSPComponentsNicFactory.build({...NICPostRequest, id: nicId});

		mockRest.addHandler('post', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/nics`);
			expect(data).toEqual(NICPostRequest);
			expect(options).toEqual(undefined);
			return {
				nicId
			};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/nics`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: [expectedNIC]};
		});

		mockRest.addHandler('destroy', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/nics/${nicId}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/nics`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: []};
		});

		const network = VSPComponentsNetworkFactory.build({
			nicList: [expectedNIC]
		});

		const networkAfterDelete = VSPComponentsNetworkFactory.build();

		let expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);

		return SoftwareProductComponentsNetworkActionHelper.createNIC(store.dispatch, {nic: NICPostRequest, softwareProductId, componentId, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
			return SoftwareProductComponentsNetworkActionHelper.deleteNIC(store.dispatch, {softwareProductId, componentId, nicId, version});
		}).then(() => {
			let expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', networkAfterDelete);
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('open NICE editor', () => {

		const store = storeCreator();
		deepFreeze(store.getState());
		const data = VSPComponentsNicFactory.build();
		const genericFieldInfo = VSPComponentsNicFactoryGenericFieldInfo.build();

		const nic = {id: '444'};
		deepFreeze(data);
		deepFreeze(nic);

		const expectedData = {...data, id: nic.id};

		deepFreeze(expectedData);

		const network = VSPComponentsNetworkFactory.build({
			nicEditor: {
				data: expectedData,
				formName: forms.NIC_EDIT_FORM,
				genericFieldInfo
			}
		});

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);

		SoftwareProductComponentsNetworkActionHelper.openNICEditor(store.dispatch, {nic, data});

		expect(store.getState().softwareProduct.softwareProductComponents.network).toEqual(expectedStore.softwareProduct.softwareProductComponents.network);
	});

	it('close NICE editor', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const network = VSPComponentsNetworkFactory.build();
		deepFreeze(network);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);

		SoftwareProductComponentsNetworkActionHelper.closeNICEditor(store.dispatch);

		expect(store.getState()).toEqual(expectedStore);
	});

	it('Load NIC data', () => {
		mockRest.resetQueue();
		const expectedData = VSPComponentsNicFactory.build();

		deepFreeze(expectedData);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/nics/${nicId}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return (expectedData);
		});

		return SoftwareProductComponentsNetworkActionHelper.loadNICData({softwareProductId, version, componentId, nicId}).then((data) => {
			expect(data).toEqual(expectedData);
		});
	});

	it('load NIC Questionnaire', () => {
		mockRest.resetQueue();
		const store = storeCreator();
		deepFreeze(store.getState());

		const qdata = VSPComponentsNetworkQDataFactory.build();
		const dataMap = VSPComponentsNetworkDataMapFactory.build();
		const qgenericFieldInfo = {};
		const qschema = VSPQSchemaFactory.build();

		deepFreeze(qdata);
		deepFreeze(dataMap);
		deepFreeze(qschema);


		const network = VSPComponentsNetworkFactory.build({
			nicEditor: {
				qdata,
				dataMap,
				qgenericFieldInfo
			}
		});
		deepFreeze(network);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/nics/${nicId}/questionnaire`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return ({data: JSON.stringify(qdata), schema: JSON.stringify(qschema)});
		});

		return SoftwareProductComponentsNetworkActionHelper.loadNICQuestionnaire(store.dispatch, {softwareProductId, version, componentId, nicId}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('save NIC Data And Questionnaire', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const qdata = VSPComponentsNetworkQDataFactory.build();
		const data = VSPComponentsNicFactory.build();

		const expectedData = {...data, id: nicId};

		const network = {
			nicEditor: {},
			nicCreation: {},
			nicList: [
				expectedData
			]
		};

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.network', network);
		deepFreeze(expectedStore);

		mockRest.addHandler('put', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/nics/${nicId}/questionnaire`);
			expect(data).toEqual(qdata);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('put', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/nics/${nicId}`);
			expect(data).toEqual(data);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductComponentsNetworkActionHelper.saveNICDataAndQuestionnaire(store.dispatch, {softwareProductId, version, componentId, qdata, data: expectedData}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});


});

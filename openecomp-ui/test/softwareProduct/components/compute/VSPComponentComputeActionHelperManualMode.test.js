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
import Configuration from 'sdc-app/config/Configuration.js';
import ComputeFlavorActionHelper from 'sdc-app/onboarding/softwareProduct/components/compute/ComputeFlavorActionHelper.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';

import {ComputeFlavorQData, ComputeFlavorBaseData, ComponentComputeFactory, VSPComponentsComputeDataMapFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsComputeFactory.js';

const softwareProductId = '123';
const vspComponentId = '111';
const computeId = '111';
const version = VersionFactory.build();


describe('Software Product Components Compute Module Tests - Manual mode', function () {

	let restPrefix = '';

	beforeAll(function() {
		restPrefix = Configuration.get('restPrefix');
		deepFreeze(restPrefix);
	});


	it('Close Compute Flavor editor', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const compute = ComponentComputeFactory.build();
		deepFreeze(compute);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.computeFlavor', compute);

		ComputeFlavorActionHelper.closeComputeEditor(store.dispatch);
		expect(store.getState()).toEqual(expectedStore);
	});

	it('Get Computes List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const computesList = ComputeFlavorBaseData.buildList(2);
		deepFreeze(computesList);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.computeFlavor.computesList', computesList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${vspComponentId}/compute-flavors`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: computesList};
		});

		return ComputeFlavorActionHelper.fetchComputesList(store.dispatch, {softwareProductId, componentId: vspComponentId, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Load Compute data & Questionnaire', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const computeData = {
			...ComputeFlavorBaseData.build(),
			id: computeId
		};
		deepFreeze(computeData);
		const qdata = ComputeFlavorQData.build();
		const dataMap = VSPComponentsComputeDataMapFactory.build();

		const softwareProductComponentCompute = {
			data: JSON.stringify(qdata),
			schema: JSON.stringify(qdata)
		};
		deepFreeze(softwareProductComponentCompute);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${vspComponentId}/compute-flavors/${computeId}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {data: computeData};
		});
		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(
				`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${vspComponentId}/compute-flavors/${computeId}/questionnaire`
			);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return softwareProductComponentCompute;
		});

		return ComputeFlavorActionHelper.loadCompute(store.dispatch, {softwareProductId, componentId: vspComponentId, version, computeId}).then(() => {
			expect(store.getState().softwareProduct.softwareProductComponents.computeFlavor.computeEditor.data).toEqual(computeData);
			expect(store.getState().softwareProduct.softwareProductComponents.computeFlavor.computeEditor.qdata).toEqual(qdata);
			expect(store.getState().softwareProduct.softwareProductComponents.computeFlavor.computeEditor.dataMap).toEqual(dataMap);
		});
	});

	it('Save Compute Flavor data and questionnaire', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const qdata = ComputeFlavorQData.build();
		const data = ComputeFlavorBaseData.build();

		const compute = {...data, id: computeId};

		const computeObj = {
			computeEditor: {},
			computesList: [
				compute
			]
		};

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.computeFlavor', computeObj);
		deepFreeze(expectedStore);

		mockRest.addHandler('put', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(
				`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${vspComponentId}/compute-flavors/${computeId}/questionnaire`
			);
			expect(data).toEqual(qdata);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('put', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${vspComponentId}/compute-flavors/${computeId}`);
			expect(data).toEqual(data);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		return ComputeFlavorActionHelper.saveComputeDataAndQuestionnaire(store.dispatch, {softwareProductId, componentId: vspComponentId, qdata, data: compute, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Delete Compute Flavor', () => {
		const compute = ComponentComputeFactory.build();
		const computesList = [compute];
		deepFreeze(computesList);

		const store = storeCreator({
			softwareProduct: {
				softwareProductComponents: {
					computeFlavor: {
						computesList: computesList
					}
				}
			}
		});
		deepFreeze(store.getState());

		const computeId = compute.id;

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.computeFlavor.computesList', []);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${vspComponentId}/compute-flavors/${computeId}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {
				results: {
					returnCode: 'OK'
				}
			};
		});

		return ComputeFlavorActionHelper.deleteCompute(store.dispatch, {softwareProductId, componentId: vspComponentId, computeId, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});
});

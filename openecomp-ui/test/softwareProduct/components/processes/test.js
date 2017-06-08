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
import {buildFromExistingObject} from 'test-utils/Util.js';
import {cloneAndSet} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import SoftwareProductComponentProcessesActionHelper from 'sdc-app/onboarding/softwareProduct/components/processes/SoftwareProductComponentProcessesActionHelper.js';

import {
	VSPProcessStoreWithArtifactNameFactory,
	VSPProcessPostFactory,
	VSPProcessStoreFactory,
	VSPProcessPostFactoryWithType,
	VSPProcessStoreFactoryWithType} from 'test-utils/factories/softwareProduct/SoftwareProductProcessFactories.js';

const softwareProductId = '123';
const componentId = '222';
const versionId = '1';
const version = {id: versionId, label: versionId};

describe('Software Product Component Processes Module Tests', function () {
	it('Get Software Products Processes List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductProcessesList = VSPProcessStoreWithArtifactNameFactory.buildList(2);

		deepFreeze(softwareProductProcessesList);

		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentProcesses.processesList', softwareProductProcessesList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/components/${componentId}/processes`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: softwareProductProcessesList};
		});
		

		return SoftwareProductComponentProcessesActionHelper.fetchProcessesList(store.dispatch, {softwareProductId, componentId, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});
	it('Delete Software Products Processes', () => {
		let process = VSPProcessStoreWithArtifactNameFactory.build();
		const softwareProductProcessesList = [process];

		deepFreeze(softwareProductProcessesList);
		const store = storeCreator({
			softwareProduct: {
				softwareProductProcesses: {
					processesList: softwareProductProcessesList
				}
			}
		});

		const processId = process.id;
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentProcesses.processesList', []);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/components/${componentId}/processes/${processId}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {
				results: {
					returnCode: 'OK'
				}
			};
		});

		return SoftwareProductComponentProcessesActionHelper.deleteProcess(store.dispatch, {
			process: softwareProductProcessesList[0],
			softwareProductId, componentId,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Add Software Products Processes', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductProcessFromResponse = 'ADDED_ID';

		const softwareProductProcessAfterAdd = VSPProcessStoreFactory.build({id: softwareProductProcessFromResponse});
		const softwareProductPostRequest = buildFromExistingObject(VSPProcessPostFactory, softwareProductProcessAfterAdd);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentProcesses.processesList', [softwareProductProcessAfterAdd]);

		mockRest.addHandler('post', ({data, options, baseUrl}) => {

			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/components/${componentId}/processes`);
			expect(data).toEqual(softwareProductPostRequest);
			expect(options).toEqual(undefined);
			return {
				returnCode: 'OK',
				value: softwareProductProcessFromResponse
			};
		});

		return SoftwareProductComponentProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId,
				previousProcess: null,
				process: softwareProductPostRequest,
				componentId,
				version
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Add Software Products Processes with type', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductPostRequest = VSPProcessPostFactoryWithType.build();

		const softwareProductProcessAfterAdd = VSPProcessStoreFactoryWithType.build(softwareProductPostRequest);
		const softwareProductProcessFromResponse = softwareProductProcessAfterAdd.id;

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentProcesses.processesList', [softwareProductProcessAfterAdd]);

		mockRest.addHandler('post', ({data, options, baseUrl}) => {

			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/components/${componentId}/processes`);
			expect(data).toEqual(softwareProductPostRequest);
			expect(options).toEqual(undefined);
			return {
				returnCode: 'OK',
				value: softwareProductProcessFromResponse
			};
		});

		return SoftwareProductComponentProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId,
				previousProcess: null,
				process: softwareProductPostRequest,
				componentId,
				version
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Update Software Products Processes', () => {
		let process = VSPProcessStoreWithArtifactNameFactory.build();

		const softwareProductProcessesList = [process];
		deepFreeze(softwareProductProcessesList);

		const store = storeCreator({
			softwareProduct: {
				softwareProductComponents: {
					componentProcesses: {
						processesList: softwareProductProcessesList
					}
				}
			}
		});
		deepFreeze(store.getState());

		const toBeUpdatedProcessId = process.id;
		const previousProcessData = process;
		const processUpdateData = VSPProcessStoreWithArtifactNameFactory.build({
			...process,
			name: 'Pr1_UPDATED',
			description: 'string_UPDATED',
			type: 'Other'
		});
		deepFreeze(processUpdateData);

		const processPutRequest = VSPProcessPostFactory.build({
			name: 'Pr1_UPDATED',
			description: 'string_UPDATED',
			type: 'Other'
		});
		deepFreeze(processPutRequest);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentProcesses.processesList', [processUpdateData]);


		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/components/${componentId}/processes/${toBeUpdatedProcessId}`);
			expect(data).toEqual(processPutRequest);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductComponentProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId: softwareProductId,
				componentId,
				previousProcess: previousProcessData,
				process: processUpdateData,
				version
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

});

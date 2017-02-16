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
import SoftwareProductComponentProcessesActionHelper from 'sdc-app/onboarding/softwareProduct/components/processes/SoftwareProductComponentProcessesActionHelper.js';

const softwareProductId = '123';
const componentId = '222';
describe('Software Product Component Processes Module Tests', function () {
	it('Get Software Products Processes List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductProcessesList = [
			{
				name: 'Pr1',
				description: 'hjhj',
				id: 'EBADF561B7FA4A788075E1840D0B5971',
				artifactName: 'artifact'
			},
			{
				name: 'Pr1',
				description: 'hjhj',
				id: '2F47447D22DB4C53B020CA1E66201EF2',
				artifactName: 'artifact'
			}
		];

		deepFreeze(softwareProductProcessesList);

		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentProcesses.processesList', softwareProductProcessesList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/processes`);
			expect(data).to.deep.equal(undefined);
			expect(options).to.equal(undefined);
			return {results: softwareProductProcessesList};
		});

		return SoftwareProductComponentProcessesActionHelper.fetchProcessesList(store.dispatch, {softwareProductId, componentId}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});
	it('Delete Software Products Processes', () => {
		const softwareProductProcessesList = [
			{
				name: 'Pr1',
				description: 'hjhj',
				id: 'EBADF561B7FA4A788075E1840D0B5971',
				artifactName: 'artifact'
			}
		];

		deepFreeze(softwareProductProcessesList);
		const store = storeCreator({
			softwareProduct: {
				softwareProductProcesses: {
					processesList: softwareProductProcessesList
				}
			}
		});

		const processId = 'EBADF561B7FA4A788075E1840D0B5971';
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentProcesses.processesList', []);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/processes/${processId}`);
			expect(data).to.equal(undefined);
			expect(options).to.equal(undefined);
			return {
				results: {
					returnCode: 'OK'
				}
			};
		});

		return SoftwareProductComponentProcessesActionHelper.deleteProcess(store.dispatch, {
			process: softwareProductProcessesList[0],
			softwareProductId, componentId
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Add Software Products Processes', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductPostRequest = {
			name: 'Pr1',
			description: 'string'
		};
		const softwareProductProcessToAdd = {
			name: 'Pr1',
			description: 'string'
		};
		const softwareProductProcessFromResponse = 'ADDED_ID';
		const softwareProductProcessAfterAdd = {
			...softwareProductProcessToAdd,
			id: softwareProductProcessFromResponse
		};

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentProcesses.processesList', [softwareProductProcessAfterAdd]);

		mockRest.addHandler('create', ({data, options, baseUrl}) => {

			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/processes`);
			expect(data).to.deep.equal(softwareProductPostRequest);
			expect(options).to.equal(undefined);
			return {
				returnCode: 'OK',
				value: softwareProductProcessFromResponse
			};
		});

		return SoftwareProductComponentProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId,
				previousProcess: null,
				process: softwareProductProcessToAdd,
				componentId
			}
		).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Update Software Products Processes', () => {
		const softwareProductProcessesList = [
			{
				name: 'Pr1',
				description: 'string',
				id: 'EBADF561B7FA4A788075E1840D0B5971',
				artifactName: 'artifact'
			}
		];
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

		const toBeUpdatedProcessId = softwareProductProcessesList[0].id;
		const previousProcessData = softwareProductProcessesList[0];
		const processUpdateData = {
			...softwareProductProcessesList[0],
			name: 'Pr1_UPDATED',
			description: 'string_UPDATED'
		};
		deepFreeze(processUpdateData);

		const processPutRequest = {
			name: 'Pr1_UPDATED',
			description: 'string_UPDATED'
		};
		deepFreeze(processPutRequest);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentProcesses.processesList', [processUpdateData]);


		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/processes/${toBeUpdatedProcessId}`);
			expect(data).to.deep.equal(processPutRequest);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductComponentProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId: softwareProductId,
				componentId,
				previousProcess: previousProcessData,
				process: processUpdateData
			}
		).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

});


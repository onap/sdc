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
import SoftwareProductProcessesActionHelper from 'sdc-app/onboarding/softwareProduct/processes/SoftwareProductProcessesActionHelper.js';
import {
	VSPProcessPostFactory,
	VSPProcessStoreFactory,
	VSPProcessPostFactoryWithType,
	VSPProcessStoreFactoryWithType,
	VSPProcessStoreWithFormDataFactory,
	VSPProcessPostWithFormDataFactory,
	VSPProcessStoreWithArtifactNameFactory } from 'test-utils/factories/softwareProduct/SoftwareProductProcessFactories.js';
import {buildFromExistingObject} from 'test-utils/Util.js';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';

const softwareProductId = '123';
const version = VersionFactory.build();

describe('Software Product Processes Module Tests', function () {

	let restPrefix = '';

	beforeAll(function() {
		restPrefix = Configuration.get('restPrefix');
		deepFreeze(restPrefix);
	});

	//**
	//** ADD
	//**
	it('Add Software Products Processes', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductProcessFromResponse = 'ADDED_ID';

		const softwareProductProcessAfterAdd = VSPProcessStoreFactory.build({id: softwareProductProcessFromResponse});
		const softwareProductPostRequest = buildFromExistingObject(VSPProcessPostFactory, softwareProductProcessAfterAdd);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', [softwareProductProcessAfterAdd]);

		mockRest.addHandler('post', ({data, options, baseUrl}) => {

			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/processes`);
			expect(data).toEqual(softwareProductPostRequest);
			expect(options).toEqual(undefined);
			return {
				returnCode: 'OK',
				value: softwareProductProcessFromResponse
			};
		});

		return SoftwareProductProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId,
				version,
				previousProcess: null,
				process: softwareProductPostRequest
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Add Software Products Processes with type', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductProcessFromResponse = 'ADDED_ID';

		const softwareProductProcessAfterAdd = VSPProcessStoreFactoryWithType.build({id: softwareProductProcessFromResponse});
		const softwareProductPostRequest = buildFromExistingObject(VSPProcessPostFactoryWithType, softwareProductProcessAfterAdd);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', [softwareProductProcessAfterAdd]);

		mockRest.addHandler('post', ({data, options, baseUrl}) => {

			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/processes`);
			expect(data).toEqual(softwareProductPostRequest);
			expect(options).toEqual(undefined);
			return {
				returnCode: 'OK',
				value: softwareProductProcessFromResponse
			};
		});

		return SoftwareProductProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId,
				version,
				previousProcess: null,
				process: softwareProductPostRequest
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Add Software Products Processes with uploaded file', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductPostRequest = VSPProcessPostFactoryWithType.build();
		const softwareProductProcessToAdd = VSPProcessPostWithFormDataFactory.build(softwareProductPostRequest);
		const softwareProductProcessAfterAdd = VSPProcessStoreWithFormDataFactory.build();

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', [softwareProductProcessAfterAdd]);

		mockRest.addHandler('post', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/processes`);
			expect(data).toEqual(softwareProductPostRequest);
			expect(options).toEqual(undefined);
			return {
				returnCode: 'OK',
				value: softwareProductProcessAfterAdd.id
			};
		});

		mockRest.addHandler('post', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/processes/${softwareProductProcessAfterAdd.id}/upload`);
			expect(data).toEqual(softwareProductProcessToAdd.formData);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId,
				version,
				previousProcess: null,
				process: softwareProductProcessToAdd
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	//**
	//** UPDATE
	//**
	it('Update Software Products Processes', () => {
		const softwareProductProcessesList = VSPProcessStoreWithArtifactNameFactory.buildList(1);
		deepFreeze(softwareProductProcessesList);

		const store = storeCreator({
			softwareProduct: {
				softwareProductProcesses: {
					processesList: softwareProductProcessesList
				}
			}
		});
		deepFreeze(store.getState());

		const toBeUpdatedProcessId = softwareProductProcessesList[0].id;
		const previousProcessData = softwareProductProcessesList[0];
		const processUpdateData = VSPProcessStoreWithArtifactNameFactory.build(
			{...previousProcessData,
				name: 'Pr1_UPDATED',
				description: 'string_UPDATED',
				type: 'Other'
			}
		);

		deepFreeze(processUpdateData);

		const processPutRequest = VSPProcessPostFactory.build({
			name: 'Pr1_UPDATED',
			description: 'string_UPDATED',
			type: 'Other'
		});
		deepFreeze(processPutRequest);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', [processUpdateData]);


		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/processes/${toBeUpdatedProcessId}`);
			expect(data).toEqual(processPutRequest);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId,
				version,
				previousProcess: previousProcessData,
				process: processUpdateData
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Update Software Products Processes and uploaded file', () => {
		const previousProcessData = VSPProcessStoreWithArtifactNameFactory.build();
		deepFreeze(previousProcessData);

		const store = storeCreator({
			softwareProduct: {
				softwareProductProcesses: {
					processesList: [previousProcessData]
				}
			}
		});
		deepFreeze(store.getState());

		const newProcessToUpdate = VSPProcessStoreWithFormDataFactory.build({
			...previousProcessData,
			name: 'new name',
			formData: {
				name: 'new artifact name'
			}
		});
		deepFreeze(newProcessToUpdate);

		const newProcessToPutRequest = VSPProcessPostFactory.build({
			name: newProcessToUpdate.name,
			description: previousProcessData.description,
			type: previousProcessData.type
		});
		deepFreeze(newProcessToPutRequest);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', [newProcessToUpdate]);

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/processes/${newProcessToUpdate.id}`);
			expect(data).toEqual(newProcessToPutRequest);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('post', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/processes/${newProcessToUpdate.id}/upload`);
			expect(data).toEqual(newProcessToUpdate.formData);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId,
				version,
				previousProcess: previousProcessData,
				process: newProcessToUpdate
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	//**
	//** GET
	//**
	it('Get Software Products Processes List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductProcessesList = VSPProcessStoreFactory.buildList(2);

		deepFreeze(softwareProductProcessesList);

		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', softwareProductProcessesList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/processes`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: softwareProductProcessesList};
		});

		return SoftwareProductProcessesActionHelper.fetchProcessesList(store.dispatch, {softwareProductId, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	//**
	//** DELETE
	//**
	it('Delete Software Products Processes', () => {
		const softwareProductProcessesList = VSPProcessStoreWithArtifactNameFactory.buildList(1);
		const currentSoftwareProduct = VSPEditorFactory.build();

		deepFreeze(softwareProductProcessesList);
		const store = storeCreator({
			softwareProduct: {
				softwareProductProcesses: {
					processesList: softwareProductProcessesList
				},
				softwareProductEditor: {
					data: currentSoftwareProduct
				}
			}
		});

		const processId = softwareProductProcessesList[0].id;
		const versionId = version.id;
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', []);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/processes/${processId}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {
				results: {
					returnCode: 'OK'
				}
			};
		});

		return SoftwareProductProcessesActionHelper.deleteProcess(store.dispatch, {
			process: softwareProductProcessesList[0],
			softwareProductId,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Validating Software Products Processes Delete confirmation', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		let process = VSPProcessStoreFactory.build();
		deepFreeze(process);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processToDelete', process);

		return SoftwareProductProcessesActionHelper.openDeleteProcessesConfirm(store.dispatch, {process});

		expect(store.getState()).toEqual(expectedStore);
	});

	it('Validating Software Products Processes Cancel Delete', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processToDelete', false);

		SoftwareProductProcessesActionHelper.hideDeleteConfirm(store.dispatch);

		expect(store.getState()).toEqual(expectedStore);
	});

	//**
	//** CREATE/EDIT
	//**
	it('Validating open Software Products Processes for create', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		let process = {};
		deepFreeze(process);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesEditor.data', process);

		SoftwareProductProcessesActionHelper.openEditor(store.dispatch);
		expect(store.getState().softwareProduct.softwareProductProcesses.processesEditor.data).toEqual(expectedStore.softwareProduct.softwareProductProcesses.processesEditor.data);
	});

	it('Validating close Software Products Processes from editing mode', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesEditor', {});

		SoftwareProductProcessesActionHelper.closeEditor(store.dispatch);
		expect(store.getState()).toEqual(expectedStore);
	});

	it('Validating open Software Products Processes for editing', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		let process = {name: 'aa', description: 'xx'};
		deepFreeze(process);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesEditor.data', process);

		SoftwareProductProcessesActionHelper.openEditor(store.dispatch, process);
		expect(store.getState().softwareProduct.softwareProductProcesses.processesEditor.data).toEqual(expectedStore.softwareProduct.softwareProductProcesses.processesEditor.data);

	});

});

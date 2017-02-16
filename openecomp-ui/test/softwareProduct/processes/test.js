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
import Configuration from 'sdc-app/config/Configuration.js';
import SoftwareProductProcessesActionHelper from 'sdc-app/onboarding/softwareProduct/processes/SoftwareProductProcessesActionHelper.js';

const softwareProductId = '123';

describe('Software Product Processes Module Tests', function () {

	let restPrefix = '';

	before(function() {
		restPrefix = Configuration.get('restPrefix');
		deepFreeze(restPrefix);
	});

	//**
	//** ADD
	//**
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

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', [softwareProductProcessAfterAdd]);

		mockRest.addHandler('create', ({data, options, baseUrl}) => {

			expect(baseUrl).to.equal(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/processes`);
			expect(data).to.deep.equal(softwareProductPostRequest);
			expect(options).to.equal(undefined);
			return {
				returnCode: 'OK',
				value: softwareProductProcessFromResponse
			};
		});

		return SoftwareProductProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId: softwareProductId,
				previousProcess: null,
				process: softwareProductProcessToAdd
			}
		).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Add Software Products Processes with uploaded file', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductPostRequest = {
			name: 'Pr1',
			description: 'string'
		};
		const softwareProductProcessToAdd = {
			name: 'Pr1',
			description: 'string',
			formData: {
				name: 'new artifact name'
			}
		};
		const softwareProductProcessFromResponse = 'ADDED_ID';
		const softwareProductProcessAfterAdd = {
			...softwareProductProcessToAdd,
			id: softwareProductProcessFromResponse
		};

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', [softwareProductProcessAfterAdd]);

		mockRest.addHandler('create', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/processes`);
			expect(data).to.deep.equal(softwareProductPostRequest);
			expect(options).to.equal(undefined);
			return {
				returnCode: 'OK',
				value: softwareProductProcessFromResponse
			};
		});

		mockRest.addHandler('create', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/processes/${softwareProductProcessAfterAdd.id}/upload`);
			expect(data).to.deep.equal(softwareProductProcessToAdd.formData);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId: softwareProductId,
				previousProcess: null,
				process: softwareProductProcessToAdd
			}
		).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	//**
	//** UPDATE
	//**
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
				softwareProductProcesses: {
					processesList: softwareProductProcessesList
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

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', [processUpdateData]);


		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/processes/${toBeUpdatedProcessId}`);
			expect(data).to.deep.equal(processPutRequest);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId: softwareProductId,
				previousProcess: previousProcessData,
				process: processUpdateData
			}
		).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Update Software Products Processes and uploaded file', () => {
		const previousProcessData = {
			id: 'EBADF561B7FA4A788075E1840D0B5971',
			name: 'p1',
			description: 'string',
			artifactName: 'artifact'
		};
		deepFreeze(previousProcessData);

		const store = storeCreator({
			softwareProduct: {
				softwareProductProcesses: {
					processesList: [previousProcessData]
				}
			}
		});
		deepFreeze(store.getState());

		const newProcessToUpdate = {
			...previousProcessData,
			name: 'new name',
			formData: {
				name: 'new artifact name'
			}
		};
		deepFreeze(newProcessToUpdate);

		const newProcessToPutRequest = {
			name: newProcessToUpdate.name,
			description: previousProcessData.description
		};
		deepFreeze(newProcessToPutRequest);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', [newProcessToUpdate]);

		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/processes/${previousProcessData.id}`);
			expect(data).to.deep.equal(newProcessToPutRequest);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('create', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/processes/${previousProcessData.id}/upload`);
			expect(data).to.deep.equal(newProcessToUpdate.formData);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductProcessesActionHelper.saveProcess(store.dispatch,
			{
				softwareProductId: softwareProductId,
				previousProcess: previousProcessData,
				process: newProcessToUpdate
			}
		).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	//**
	//** GET
	//**
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

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', softwareProductProcessesList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/processes`);
			expect(data).to.deep.equal(undefined);
			expect(options).to.equal(undefined);
			return {results: softwareProductProcessesList};
		});

		return SoftwareProductProcessesActionHelper.fetchProcessesList(store.dispatch, {softwareProductId}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	//**
	//** DELETE
	//**
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

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesList', []);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/processes/${processId}`);
			expect(data).to.equal(undefined);
			expect(options).to.equal(undefined);
			return {
				results: {
					returnCode: 'OK'
				}
			};
		});

		return SoftwareProductProcessesActionHelper.deleteProcess(store.dispatch, {
			process: softwareProductProcessesList[0],
			softwareProductId
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Validating Software Products Processes Delete confirmation', done => {
		const store = storeCreator();
		deepFreeze(store.getState());

		let process = {
			id: 'p_id',
			name: 'p_name'
		};
		deepFreeze(process);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processToDelete', process);

		SoftwareProductProcessesActionHelper.openDeleteProcessesConfirm(store.dispatch, {process});

		setTimeout(function(){
			expect(store.getState()).to.deep.equal(expectedStore);
			done();
		}, 100);
	});

	it('Validating Software Products Processes Cancel Delete', done => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processToDelete', false);

		SoftwareProductProcessesActionHelper.hideDeleteConfirm(store.dispatch);

		setTimeout(function(){
			expect(store.getState()).to.deep.equal(expectedStore);
			done();
		}, 100);
	});

	//**
	//** CREATE/EDIT
	//**
	it('Validating open Software Products Processes for create', done => {
		const store = storeCreator();
		deepFreeze(store.getState());

		let process = {};
		deepFreeze(process);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesEditor.data', process);

		SoftwareProductProcessesActionHelper.openEditor(store.dispatch);

		setTimeout(function(){
			expect(store.getState()).to.deep.equal(expectedStore);
			done();
		}, 100);
	});

	it('Validating close Software Products Processes from editing mode', done => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesEditor', {});

		SoftwareProductProcessesActionHelper.closeEditor(store.dispatch);

		setTimeout(function(){
			expect(store.getState()).to.deep.equal(expectedStore);
			done();
		}, 100);
	});

	it('Validating open Software Products Processes for editing', done => {
		const store = storeCreator();
		deepFreeze(store.getState());

		let process = {name: 'aa', description: 'xx'};
		deepFreeze(process);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesEditor.data', process);

		SoftwareProductProcessesActionHelper.openEditor(store.dispatch, process);

		setTimeout(function(){
			expect(store.getState()).to.deep.equal(expectedStore);
			done();
		}, 100);
	});

	it('Validating Software Products Processes dataChanged event', done => {
		let process = {name: 'aa', description: 'xx'};
		deepFreeze(process);

		const store = storeCreator({
			softwareProduct: {
				softwareProductProcesses: {
					processesEditor: {
						data: process
					}
				}
			}
		});
		deepFreeze(store.getState());

		let deltaData = {name: 'bb'};
		deepFreeze(deltaData);

		let expectedProcess = {name: 'bb', description: 'xx'};
		deepFreeze(expectedProcess);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductProcesses.processesEditor.data', expectedProcess);

		SoftwareProductProcessesActionHelper.processEditorDataChanged(store.dispatch, {deltaData});

		setTimeout(function(){
			expect(store.getState()).to.deep.equal(expectedStore);
			done();
		}, 100);
	});
});


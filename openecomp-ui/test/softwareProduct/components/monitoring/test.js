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

import mockRest from 'test-utils/MockRest.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import SoftwareProductComponentsMonitoringActionHelper from 'sdc-app/onboarding/softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringActionHelper.js';
import {fileTypes} from 'sdc-app/onboarding/softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringConstants.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import {VSPComponentsMonitoringRestFactory, trap, poll, ves} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsMonitoringFactories.js';

const softwareProductId = '123';
const componentId = '123';
const version = VersionFactory.build();

describe('Software Product Components Monitoring Module Tests', function () {

	let store;

	beforeEach(()=> {
		store = storeCreator();
	});


	it('Fetch for existing files - no files', done => {

		let emptyResult = VSPComponentsMonitoringRestFactory.build();

		mockRest.addHandler('fetch', ({ baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/uploads`);
			return emptyResult;
		});

		return SoftwareProductComponentsMonitoringActionHelper.fetchExistingFiles(store.dispatch, {softwareProductId, version, componentId}).then(() => {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring[trap]).toEqual(emptyResult[trap]);
			expect(monitoring[poll]).toEqual(emptyResult[poll]);
			expect(monitoring[ves]).toEqual(emptyResult[ves]);
			done();
		});


	});

	it('Fetch for existing files - only snmp trap file exists', done => {
		let response = VSPComponentsMonitoringRestFactory.build({}, {createTrap: true});

		mockRest.addHandler('fetch', ({ baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/uploads`);
			return response;
		});

		return SoftwareProductComponentsMonitoringActionHelper.fetchExistingFiles(store.dispatch, {softwareProductId, version, componentId}).then(() => {

			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring[poll]).toEqual(undefined);
			expect(monitoring[trap]).toEqual(response[trap]);
			expect(monitoring[ves]).toEqual(undefined);
			done();
		});
	});


	it('Fetch for existing files - all files exist', done => {
		let response = VSPComponentsMonitoringRestFactory.build({}, {createSnmp: true, createPoll: true, createVes: true});

		mockRest.addHandler('fetch', ({baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/uploads`);
			return response;
		});

		return SoftwareProductComponentsMonitoringActionHelper.fetchExistingFiles(store.dispatch, {softwareProductId, version, componentId}).then(() => {

			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring[trap]).toEqual(response[trap]);
			expect(monitoring[poll]).toEqual(response[poll]);
			expect(monitoring[ves]).toEqual(response[ves]);
			done();
		});
	});

	it('Upload file', done => {

		mockRest.addHandler('post', ({baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/uploads/types/${fileTypes.SNMP_TRAP}`);
			return {};
		});
		var debug = {hello: 'world'};
		let file = new Blob([JSON.stringify(debug, null, 2)], {type: 'application/json'});;
		let formData = new FormData();
		formData.append('upload', file);
		return SoftwareProductComponentsMonitoringActionHelper.uploadFile(store.dispatch, {
			softwareProductId,
			version,
			componentId,
			formData,
			fileSize: file.size,
			type: fileTypes.SNMP_TRAP
		}).then(() => {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring[poll]).toEqual(undefined);
			expect(monitoring[ves]).toEqual(undefined);
			expect(monitoring[trap]).toEqual('blob');
			done();

		});
	});

	it('Delete snmp trap file', done => {
		mockRest.addHandler('destroy', ({baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/uploads/types/${fileTypes.SNMP_TRAP}`);
			return {};
		});


		return SoftwareProductComponentsMonitoringActionHelper.deleteFile(store.dispatch, {
			softwareProductId,
			version,
			componentId,
			type: fileTypes.SNMP_TRAP
		}).then(() => {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring[trap]).toEqual(undefined);
			done();
		});
	});


});

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
import SoftwareProductComponentsMonitoringConstants from 'sdc-app/onboarding/softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringConstants.js';
import SoftwareProductComponentsMonitoringActionHelper from 'sdc-app/onboarding/softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringActionHelper.js';

import {VSPComponentsMonitoringRestFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsMonitoringFactories.js';
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';

const softwareProductId = '123';
const componentId = '123';
const version = VersionControllerUtilsFactory.build().version;

describe('Software Product Components Monitoring Module Tests', function () {

	let store;

	beforeEach(()=> {
		store = storeCreator();
	});


	it('Fetch for existing files - no files', done => {

		let emptyResult = VSPComponentsMonitoringRestFactory.build();

		mockRest.addHandler('fetch', ({ baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/monitors/snmp`);
			return emptyResult;
		});

		SoftwareProductComponentsMonitoringActionHelper.fetchExistingFiles(store.dispatch, {
			softwareProductId,
			version,
			componentId
		});
		setTimeout(()=> {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring.pollFilename).toEqual(emptyResult.snmpPoll);
			expect(monitoring.trapFilename).toEqual(emptyResult.snmpTrap);
			done();
		}, 0);

	});

	it('Fetch for existing files - only snmp trap file exists', done => {
		let response = VSPComponentsMonitoringRestFactory.build({}, {snmpTrapFlag: true});

		mockRest.addHandler('fetch', ({ baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/monitors/snmp`);
			return response;
		});

		SoftwareProductComponentsMonitoringActionHelper.fetchExistingFiles(store.dispatch, {
			softwareProductId,
			version,
			componentId
		});
		setTimeout(()=> {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring.pollFilename).toEqual(response.snmpPoll);
			expect(monitoring.trapFilename).toEqual(response.snmpTrap);
			done();
		}, 0);
	});

	it('Fetch for existing files - only snmp poll file exists', done => {
		let response  = VSPComponentsMonitoringRestFactory.build({}, {snmpPollFlag: true});

		mockRest.addHandler('fetch', ({baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/monitors/snmp`);
			return response;
		});

		SoftwareProductComponentsMonitoringActionHelper.fetchExistingFiles(store.dispatch, {
			softwareProductId,
			version,
			componentId
		});
		setTimeout(()=> {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring.pollFilename).toEqual(response.snmpPoll);
			expect(monitoring.trapFilename).toEqual(response.snmpTrap);
			done();
		}, 0);
	});

	it('Fetch for existing files - both files exist', done => {
		let response = VSPComponentsMonitoringRestFactory.build({}, {snmpTrapFlag: true, snmpPollFlag: true});

		mockRest.addHandler('fetch', ({baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/monitors/snmp`);
			return response;
		});

		SoftwareProductComponentsMonitoringActionHelper.fetchExistingFiles(store.dispatch, {
			softwareProductId,
			version,
			componentId
		});
		setTimeout(()=> {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring.pollFilename).toEqual(response.snmpPoll);
			expect(monitoring.trapFilename).toEqual(response.snmpTrap);
			done();
		}, 0);
	});

	it('Upload snmp trap file', done => {

		mockRest.addHandler('post', ({baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/monitors/snmp-trap/upload`);
			return {};
		});
		var debug = {hello: 'world'};
		let file = new Blob([JSON.stringify(debug, null, 2)], {type: 'application/json'});;
		let formData = new FormData();
		formData.append('upload', file);
		SoftwareProductComponentsMonitoringActionHelper.uploadSnmpFile(store.dispatch, {
			softwareProductId,
			version,
			componentId,
			formData,
			fileSize: file.size,
			type: SoftwareProductComponentsMonitoringConstants.SNMP_TRAP
		});
		setTimeout(()=> {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring.pollFilename).toEqual(undefined);
			expect(monitoring.trapFilename).toEqual('blob');
			done();
		}, 0);
	});

	it('Upload snmp poll file', done => {
		mockRest.addHandler('post', ({baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/monitors/snmp/upload`);
			return {};
		});
		var debug = {hello: 'world'};
		let file = new Blob([JSON.stringify(debug, null, 2)], {type: 'application/json'});;
		let formData = new FormData();
		formData.append('upload', file);
		SoftwareProductComponentsMonitoringActionHelper.uploadSnmpFile(store.dispatch, {
			softwareProductId,
			version,
			componentId,
			formData,
			fileSize: file.size,
			type: SoftwareProductComponentsMonitoringConstants.SNMP_POLL
		});
		setTimeout(()=> {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring.pollFilename).toEqual('blob');
			expect(monitoring.trapFilename).toEqual(undefined);
			done();
		}, 0);
	});

	it('Delete snmp trap file', done => {
		mockRest.addHandler('destroy', ({baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/monitors/snmp-trap`);
			return {};
		});
		SoftwareProductComponentsMonitoringActionHelper.deleteSnmpFile(store.dispatch, {
			softwareProductId,
			version,
			componentId,
			type: SoftwareProductComponentsMonitoringConstants.SNMP_TRAP
		});
		setTimeout(()=> {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring.trapFilename).toEqual(undefined);
			done();
		}, 0);
	});

	it('Delete snmp poll file', done => {
		mockRest.addHandler('destroy', ({baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/monitors/snmp`);
			return {};
		});
		SoftwareProductComponentsMonitoringActionHelper.deleteSnmpFile(store.dispatch, {
			softwareProductId,
			version,
			componentId,
			type: SoftwareProductComponentsMonitoringConstants.SNMP_POLL
		});
		setTimeout(()=> {
			var {softwareProduct: {softwareProductComponents: {monitoring}}} = store.getState();
			expect(monitoring.pollFilename).toEqual(undefined);
			done();
		}, 0);
	});
});

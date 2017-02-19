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

import LicenseKeyGroupsActionHelper from 'sdc-app/onboarding/licenseModel/licenseKeyGroups/LicenseKeyGroupsActionHelper.js';

describe('License Key Groups Module Tests', function () {

	const LICENSE_MODEL_ID = '555';
	it('Load License Key Group', () => {
		const licenseKeyGroupsList = [
			{
				name: 'lsk1',
				description: 'string',
				type: 'Unique',
				operationalScope: {'choices': ['Data_Center'], 'other': ''},
				id: '0'
			}
		];
		deepFreeze(licenseKeyGroupsList);
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', licenseKeyGroupsList);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/license-key-groups`);
			expect(data).to.equal(undefined);
			expect(options).to.equal(undefined);
			return {results: licenseKeyGroupsList};
		});

		return LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(store.dispatch, {licenseModelId: LICENSE_MODEL_ID}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Delete License Key Group', () => {
		const licenseKeyGroupsList = [
			{
				name: 'lsk1',
				description: 'string',
				type: 'Unique',
				operationalScope: {'choices': ['Data_Center'], 'other': ''},
				id: '0'
			}
		];
		deepFreeze(licenseKeyGroupsList);
		const store = storeCreator({
			licenseModel: {
				licenseKeyGroup: {
					licenseKeyGroupsList
				}
			}
		});
		deepFreeze(store.getState());
		const toBeDeletedLicenseKeyGroupId = '0';
		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', []);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/license-key-groups/${toBeDeletedLicenseKeyGroupId}`);
			expect(data).to.equal(undefined);
			expect(options).to.equal(undefined);
		});

		return LicenseKeyGroupsActionHelper.deleteLicenseKeyGroup(store.dispatch, {
			licenseKeyGroupId: toBeDeletedLicenseKeyGroupId,
			licenseModelId: LICENSE_MODEL_ID
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Add License Key Group', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const licenseKeyGroupPostRequest = {
			name: 'lsk1_ADDED',
			description: 'string_ADDED',
			type: 'Unique_ADDED',
			operationalScope: {'choices': ['Data_Center'], 'other': ''}
		};
		deepFreeze(licenseKeyGroupPostRequest);

		const licenseKeyGroupToAdd = {
			...licenseKeyGroupPostRequest
		};

		deepFreeze(licenseKeyGroupToAdd);

		const licenseKeyGroupIdFromResponse = 'ADDED_ID';
		const licenseKeyGroupAfterAdd = {
			...licenseKeyGroupToAdd,
			id: licenseKeyGroupIdFromResponse
		};
		deepFreeze(licenseKeyGroupAfterAdd);

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', [licenseKeyGroupAfterAdd]);

		mockRest.addHandler('create', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/license-key-groups`);
			expect(data).to.deep.equal(licenseKeyGroupPostRequest);
			expect(options).to.equal(undefined);
			return {
				value: licenseKeyGroupIdFromResponse
			};
		});

		return LicenseKeyGroupsActionHelper.saveLicenseKeyGroup(store.dispatch, {
			licenseKeyGroup: licenseKeyGroupToAdd,
			licenseModelId: LICENSE_MODEL_ID
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Update License Key Group', () => {
		const licenseKeyGroupsList = [
			{
				name: 'lsk1',
				description: 'string',
				type: 'Unique',
				operationalScope: {'choices': ['Data_Center'], 'other': ''},
				id: '0'
			}
		];
		deepFreeze(licenseKeyGroupsList);
		const store = storeCreator({
			licenseModel: {
				licenseKeyGroup: {
					licenseKeyGroupsList
				}
			}
		});

		const toBeUpdatedLicenseKeyGroupId = licenseKeyGroupsList[0].id;
		const previousLicenseKeyGroupData = licenseKeyGroupsList[0];

		const licenseKeyGroupUpdateData = {
			...licenseKeyGroupsList[0],
			name: 'lsk1_UPDATE',
			description: 'string_UPDATE',
			type: 'Unique',
			operationalScope: {'choices': ['Data_Center'], 'other': ''}
		};
		deepFreeze(licenseKeyGroupUpdateData);

		const licenseKeyGroupPutRequest = {
			name: 'lsk1_UPDATE',
			description: 'string_UPDATE',
			type: 'Unique',
			operationalScope: {'choices': ['Data_Center'], 'other': ''}
		};
		deepFreeze(licenseKeyGroupPutRequest);

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', [licenseKeyGroupUpdateData]);

		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/license-key-groups/${toBeUpdatedLicenseKeyGroupId}`);
			expect(data).to.deep.equal(licenseKeyGroupPutRequest);
			expect(options).to.equal(undefined);
		});

		return LicenseKeyGroupsActionHelper.saveLicenseKeyGroup(store.dispatch, {
			previousLicenseKeyGroup: previousLicenseKeyGroupData,
			licenseKeyGroup: licenseKeyGroupUpdateData,
			licenseModelId: LICENSE_MODEL_ID
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

});

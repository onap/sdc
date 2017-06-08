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
import {cloneAndSet, buildListFromFactory} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import {LicenseKeyGroupStoreFactory, LicenseKeyGroupPostFactory} from 'test-utils/factories/licenseModel/LicenseKeyGroupFactories.js';

import LicenseKeyGroupsActionHelper from 'sdc-app/onboarding/licenseModel/licenseKeyGroups/LicenseKeyGroupsActionHelper.js';
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';

describe('License Key Groups Module Tests', function () {

	const LICENSE_MODEL_ID = '555';
	const version = VersionControllerUtilsFactory.build().version;

	it('Load License Key Group', () => {

		const licenseKeyGroupsList = buildListFromFactory(LicenseKeyGroupStoreFactory);

		deepFreeze(licenseKeyGroupsList);
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', licenseKeyGroupsList);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: licenseKeyGroupsList};
		});

		return LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(store.dispatch, {licenseModelId: LICENSE_MODEL_ID, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Delete License Key Group', () => {

		const licenseKeyGroupsList = buildListFromFactory(LicenseKeyGroupStoreFactory, 1);

		deepFreeze(licenseKeyGroupsList);
		const store = storeCreator({
			licenseModel: {
				licenseKeyGroup: {
					licenseKeyGroupsList
				}
			}
		});
		deepFreeze(store.getState());
		const toBeDeletedLicenseKeyGroupId = licenseKeyGroupsList[0].id;
		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', []);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${toBeDeletedLicenseKeyGroupId}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
		});

		return LicenseKeyGroupsActionHelper.deleteLicenseKeyGroup(store.dispatch, {
			licenseKeyGroupId: toBeDeletedLicenseKeyGroupId,
			licenseModelId: LICENSE_MODEL_ID,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Add License Key Group', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const LicenseKeyGroupPost = LicenseKeyGroupPostFactory.build();
		deepFreeze(LicenseKeyGroupPost);

		const LicenseKeyGroupStore = LicenseKeyGroupStoreFactory.build();
		deepFreeze(LicenseKeyGroupStore);

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', [LicenseKeyGroupStore]);

		mockRest.addHandler('post', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups`);
			expect(data).toEqual(LicenseKeyGroupPost);
			expect(options).toEqual(undefined);
			return {
				value: LicenseKeyGroupStore.id
			};
		});

		return LicenseKeyGroupsActionHelper.saveLicenseKeyGroup(store.dispatch, {
			licenseKeyGroup: LicenseKeyGroupPost,
			licenseModelId: LICENSE_MODEL_ID,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Update License Key Group', () => {
		const licenseKeyGroupsList = buildListFromFactory(LicenseKeyGroupStoreFactory, 1);
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

		const licenseKeyGroupUpdatedData = LicenseKeyGroupPostFactory.build({
			name: 'lsk1_UPDATE',
			description: 'string_UPDATE',
			id: toBeUpdatedLicenseKeyGroupId
		});
		deepFreeze(licenseKeyGroupUpdatedData);

		const licenseKeyGroupPutRequest = LicenseKeyGroupPostFactory.build({
			name: 'lsk1_UPDATE',
			description: 'string_UPDATE'
		});

		deepFreeze(licenseKeyGroupPutRequest);

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', [licenseKeyGroupUpdatedData]);

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${toBeUpdatedLicenseKeyGroupId}`);
			expect(data).toEqual(licenseKeyGroupPutRequest);
			expect(options).toEqual(undefined);
		});

		return LicenseKeyGroupsActionHelper.saveLicenseKeyGroup(store.dispatch, {
			previousLicenseKeyGroup: previousLicenseKeyGroupData,
			licenseKeyGroup: licenseKeyGroupUpdatedData,
			licenseModelId: LICENSE_MODEL_ID,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

});

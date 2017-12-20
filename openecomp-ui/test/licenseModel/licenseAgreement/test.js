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
import pickBy from 'lodash/pickBy';
import mockRest from 'test-utils/MockRest.js';
import {cloneAndSet, buildListFromFactory} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import LicenseAgreementActionHelper from 'sdc-app/onboarding/licenseModel/licenseAgreement/LicenseAgreementActionHelper.js';

import { LicenseAgreementStoreFactory, LicenseAgreementDispatchFactory, LicenseAgreementPostFactory, LicenseAgreementPutFactory } from 'test-utils/factories/licenseModel/LicenseAgreementFactories.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import {SyncStates} from 'sdc-app/common/merge/MergeEditorConstants.js';
import CurrentScreenFactory from 'test-utils/factories/common/CurrentScreenFactory.js';

describe('License Agreement Module Tests', () => {

	const LICENSE_MODEL_ID = '777';
	const version = VersionFactory.build();
	const itemPermissionAndProps = CurrentScreenFactory.build({}, {version});
	const returnedVersionFields = {baseId: version.baseId, description: version.description, id: version.id, name: version.name, status: version.status};

	it('Load License Agreement List', () => {
		const licenseAgreementList = buildListFromFactory(LicenseAgreementStoreFactory);

		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseAgreement.licenseAgreementList', licenseAgreementList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-agreements`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: licenseAgreementList};
		});
		return LicenseAgreementActionHelper.fetchLicenseAgreementList(store.dispatch, {licenseModelId: LICENSE_MODEL_ID, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Delete License Agreement', () => {
		const licenseAgreementList = buildListFromFactory(LicenseAgreementStoreFactory, 1);
		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				licenseAgreement: {
					licenseAgreementList
				}
			}
		});
		deepFreeze(store.getState());
		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};
		const toBeDeletedLicenseAgreementId = licenseAgreementList[0].id;
		let  expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseAgreement.licenseAgreementList', []);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-agreements/${toBeDeletedLicenseAgreementId}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
		});
		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
		});

		return LicenseAgreementActionHelper.deleteLicenseAgreement(store.dispatch, {
			licenseAgreementId: toBeDeletedLicenseAgreementId,
			licenseModelId: LICENSE_MODEL_ID,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Add License Agreement', () => {
		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps}
		});
		deepFreeze(store.getState());

		const licenseAgreementToAdd = LicenseAgreementDispatchFactory.build();

		const LicenseAgreementPostRequest = LicenseAgreementPostFactory.build(
			pickBy(licenseAgreementToAdd, (val, key) => key !== 'featureGroupsIds')
		);

		deepFreeze(LicenseAgreementPostRequest);

		const licenseAgreementIdFromResponse = 'ADDED_ID';
		const licenseAgreementAfterAdd = LicenseAgreementStoreFactory.build({
			...licenseAgreementToAdd,
			id: licenseAgreementIdFromResponse
		});
		deepFreeze(licenseAgreementAfterAdd);
		const licenseAgreementList = [licenseAgreementAfterAdd];
		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};
		const featureGroupsList = licenseAgreementList.featureGroupsIds;
		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseAgreement.licenseAgreementList', [licenseAgreementAfterAdd]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('post', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-agreements`);
			expect(data).toEqual(LicenseAgreementPostRequest);
			expect(options).toEqual(undefined);
			return {
				value: licenseAgreementIdFromResponse
			};
		});
		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-agreements`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: licenseAgreementList};
		});
		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/feature-groups`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: featureGroupsList};
		});
		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};

		});
		return LicenseAgreementActionHelper.saveLicenseAgreement(store.dispatch, {
			licenseAgreement: licenseAgreementToAdd,
			licenseModelId: LICENSE_MODEL_ID,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Update License Agreement', () => {
		const licenseAgreementList = buildListFromFactory(LicenseAgreementStoreFactory, 1, {featureGroupsIds: ['77']});
		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				licenseAgreement: {
					licenseAgreementList
				}
			}
		});
		deepFreeze(store.getState());

		const previousLicenseAgreementData = licenseAgreementList[0];
		const toBeUpdatedLicenseAgreementId = previousLicenseAgreementData.id;
		const oldFeatureGroupIds = previousLicenseAgreementData.featureGroupsIds;

		const newFeatureGroupsIds = ['update_id_1', 'update_id_2'];

		const licenseAgreementUpdateData = LicenseAgreementStoreFactory.build({
			id: toBeUpdatedLicenseAgreementId,
			featureGroupsIds: newFeatureGroupsIds
		});
		deepFreeze(licenseAgreementUpdateData);

		const LicenseAgreementPutFactoryRequest = LicenseAgreementPutFactory.build({
			addedFeatureGroupsIds: newFeatureGroupsIds,
			removedFeatureGroupsIds: oldFeatureGroupIds
		});

		deepFreeze(LicenseAgreementPutFactoryRequest);

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};
		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseAgreement.licenseAgreementList', [licenseAgreementUpdateData]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-agreements/${toBeUpdatedLicenseAgreementId}`);
			expect(data).toEqual(LicenseAgreementPutFactoryRequest);
			expect(options).toEqual(undefined);
		});
		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-agreements`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: [licenseAgreementUpdateData]};
		});
		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
		});
		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/feature-groups`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: newFeatureGroupsIds};
		});

		return LicenseAgreementActionHelper.saveLicenseAgreement(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			version,
			previousLicenseAgreement: previousLicenseAgreementData,
			licenseAgreement: licenseAgreementUpdateData
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

});

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
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import CurrentScreenFactory from 'test-utils/factories/common/CurrentScreenFactory.js';
import {LimitItemFactory, LimitPostFactory} from 'test-utils/factories/licenseModel/LimitFactories.js';
import {getStrValue} from 'nfvo-utils/getValue.js';
import {SyncStates} from 'sdc-app/common/merge/MergeEditorConstants.js';

describe('License Key Groups Module Tests', function () {

	const LICENSE_MODEL_ID = '555';
	const version = VersionFactory.build();
	const itemPermissionAndProps = CurrentScreenFactory.build({}, {version});
	const returnedVersionFields = {baseId: version.baseId, description: version.description, id: version.id, name: version.name, status: version.status};

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
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				licenseKeyGroup: {
					licenseKeyGroupsList
				}
			}
		});
		deepFreeze(store.getState());
		const toBeDeletedLicenseKeyGroupId = licenseKeyGroupsList[0].id;

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', []);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${toBeDeletedLicenseKeyGroupId}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
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

		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps}
		});
		deepFreeze(store.getState());

		const LicenseKeyGroupPost = LicenseKeyGroupPostFactory.build();
		deepFreeze(LicenseKeyGroupPost);

		const LicenseKeyGroupStore = LicenseKeyGroupStoreFactory.build();
		deepFreeze(LicenseKeyGroupStore);

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', [LicenseKeyGroupStore]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('post', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups`);
			expect(data).toEqual(LicenseKeyGroupPost);
			expect(options).toEqual(undefined);
			return {
				value: LicenseKeyGroupStore.id
			};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
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
			currentScreen: {...itemPermissionAndProps},
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

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsList', [licenseKeyGroupUpdatedData]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${toBeUpdatedLicenseKeyGroupId}`);
			expect(data).toEqual(licenseKeyGroupPutRequest);
			expect(options).toEqual(undefined);
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
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

	it('Load Limits List', () => {

		const limitsList = LimitItemFactory.buildList(3);
		deepFreeze(limitsList);
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsEditor.limitsList', limitsList);
		const licenseKeyGroup = LicenseKeyGroupStoreFactory.build();
		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${licenseKeyGroup.id}/limits`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: limitsList};
		 });

		return LicenseKeyGroupsActionHelper.fetchLimits(store.dispatch, {licenseModelId: LICENSE_MODEL_ID, version, licenseKeyGroup}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		 });
	});

	it('Add Limit', () => {

		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps}
		});
		deepFreeze(store.getState());

		const limitToAdd = LimitPostFactory.build();
		let limitFromBE = {...limitToAdd};
		limitFromBE.metric = getStrValue(limitFromBE.metric);
		limitFromBE.unit = getStrValue(limitFromBE.unit);

		deepFreeze(limitToAdd);
		deepFreeze(limitFromBE);

		const LimitIdFromResponse = 'ADDED_ID';
		const limitAddedItem = {...limitToAdd, id: LimitIdFromResponse};
		deepFreeze(limitAddedItem);
		const licenseKeyGroup = LicenseKeyGroupStoreFactory.build();

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsEditor.limitsList', [limitAddedItem]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('post', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${licenseKeyGroup.id}/limits`);
			expect(data).toEqual(limitFromBE);
			expect(options).toEqual(undefined);
			return {
				returnCode: 'OK',
				value: LimitIdFromResponse
			};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${licenseKeyGroup.id}/limits`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: [limitAddedItem]};
		 });

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
		});

		return LicenseKeyGroupsActionHelper.submitLimit(store.dispatch,
			{
				licenseModelId: LICENSE_MODEL_ID,
				version,
				licenseKeyGroup,
				limit: limitToAdd
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Delete Limit', () => {

		const limitsList = LimitItemFactory.buildList(1);
		deepFreeze(limitsList);

		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				entitlementPool: {
					entitlementPoolEditor: {
						limitsList
					}
				}
			}
		});
		deepFreeze(store.getState());

		const licenseKeyGroup = LicenseKeyGroupStoreFactory.build();

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsEditor.limitsList', []);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${licenseKeyGroup.id}/limits/${limitsList[0].id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {
				results: {
					returnCode: 'OK'
				}
			};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${licenseKeyGroup.id}/limits`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: []};
		 });

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
		});

		return LicenseKeyGroupsActionHelper.deleteLimit(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			version,
			licenseKeyGroup,
			limit: limitsList[0]
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Update Limit', () => {

		const limitsList = LimitItemFactory.buildList(1);
		deepFreeze(limitsList);
		const licenseKeyGroup = LicenseKeyGroupStoreFactory.build();
		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				licenseKeyGroup: {
					licenseKeyGroupsEditor: {
						limitsList
					}
				}
			}
		});

		deepFreeze(store.getState());


		const previousData = limitsList[0];
		deepFreeze(previousData);
		const limitId = limitsList[0].id;

		let updatedLimit = {...previousData, name: 'updatedLimit'};
		const updatedLimitForPut = {...updatedLimit, id: undefined};
		updatedLimit.metric = {choice: updatedLimit.metric, other: ''};
		updatedLimit.unit = {choice: updatedLimit.unit, other: ''};
		deepFreeze(updatedLimit);

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseKeyGroup.licenseKeyGroupsEditor.limitsList', [updatedLimitForPut]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);


		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${licenseKeyGroup.id}/limits/${limitId}`);
			expect(data).toEqual(updatedLimitForPut);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups/${licenseKeyGroup.id}/limits`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: [updatedLimitForPut]};
		 });

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
		});

		return LicenseKeyGroupsActionHelper.submitLimit(store.dispatch,
			{
				licenseModelId: LICENSE_MODEL_ID,
				version,
				licenseKeyGroup,
				limit: updatedLimit
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

});

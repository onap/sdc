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
import FeatureGroupsActionHelper from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupsActionHelper.js';
import { FeatureGroupStoreFactory, FeatureGroupPostFactory, FeatureGroupDispatchFactory, FeatureGroupPutFactory } from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import CurrentScreenFactory from 'test-utils/factories/common/CurrentScreenFactory.js';
import {SyncStates} from 'sdc-app/common/merge/MergeEditorConstants.js';


describe('Feature Groups Module Tests', function () {

	const LICENSE_MODEL_ID = '555';
	const version = VersionFactory.build();
	const itemPermissionAndProps = CurrentScreenFactory.build({}, {version});
	const returnedVersionFields = {baseId: version.baseId, description: version.description, id: version.id, name: version.name, status: version.status};

	it('Load Feature Groups List', () => {

		const featureGroupsList = buildListFromFactory(FeatureGroupStoreFactory);
		deepFreeze(featureGroupsList);

		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.featureGroup.featureGroupsList', featureGroupsList);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/feature-groups`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: featureGroupsList};
		});

		return FeatureGroupsActionHelper.fetchFeatureGroupsList(store.dispatch, {licenseModelId: LICENSE_MODEL_ID, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Delete Feature Group', () => {
		const featureGroupsList = buildListFromFactory(FeatureGroupStoreFactory, 1);
		deepFreeze(featureGroupsList);
		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				featureGroup: {
					featureGroupsList
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

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.featureGroup.featureGroupsList', []);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		const idToDelete = featureGroupsList[0].id;

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/feature-groups/${idToDelete}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {
				results: {
					returnCode: 'OK'
				}
			};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
		});

		return FeatureGroupsActionHelper.deleteFeatureGroup(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			version,
			featureGroupId: idToDelete
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Add Feature Group', () => {

		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				featureGroup: {
					featureGroupsList: []
				}
			}
		});
		deepFreeze(store.getState());

		const FeatureGroupPostRequest = FeatureGroupPostFactory.build({
			addedLicenseKeyGroupsIds: [1],
			addedEntitlementPoolsIds: [1]
		});
		const featureGroupToAdd = FeatureGroupDispatchFactory.build({
			licenseKeyGroupsIds: [1],
			entitlementPoolsIds: [1]
		});

		const featureGroupIdFromResponse = 'ADDED_ID';
		const featureGroupAfterAdd = FeatureGroupStoreFactory.build({
			...featureGroupToAdd,
			id: featureGroupIdFromResponse
		});

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.featureGroup.featureGroupsList', [featureGroupAfterAdd]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('post', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/feature-groups`);
			expect(data).toEqual(FeatureGroupPostRequest);
			expect(options).toEqual(undefined);
			return {
				returnCode: 'OK',
				value: featureGroupIdFromResponse
			};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: []};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups`);
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


		return FeatureGroupsActionHelper.saveFeatureGroup(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			version,
			featureGroup: featureGroupToAdd
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Update Feature Group', () => {
		const featureGroupsList = buildListFromFactory(FeatureGroupStoreFactory, 1, {
			licenseKeyGroupsIds: [1],
			entitlementPoolsIds: [1]
		});
		deepFreeze(featureGroupsList);

		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				featureGroup: {
					featureGroupsList
				}
			}
		});
		deepFreeze(store.getState());

		const toBeUpdatedFeatureGroupId = featureGroupsList[0].id;
		const previousFeatureGroupData = featureGroupsList[0];

		const featureGroupUpdateData = FeatureGroupStoreFactory.build({
			...previousFeatureGroupData,
			licenseKeyGroupsIds: [7],
			entitlementPoolsIds: [7]
		});
		deepFreeze(featureGroupUpdateData);

		const FeatureGroupPutFactoryRequest = FeatureGroupPutFactory.build({
			name: featureGroupUpdateData.name,
			description: featureGroupUpdateData.description,
			partNumber: featureGroupUpdateData.partNumber,
			addedLicenseKeyGroupsIds: [7],
			addedEntitlementPoolsIds: [7],
			removedLicenseKeyGroupsIds: [1],
			removedEntitlementPoolsIds: [1]
		});
		deepFreeze(FeatureGroupPutFactoryRequest);

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.featureGroup.featureGroupsList', [featureGroupUpdateData]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);


		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/feature-groups/${toBeUpdatedFeatureGroupId}`);
			expect(data).toEqual(FeatureGroupPutFactoryRequest);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: []};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups`);
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

		return FeatureGroupsActionHelper.saveFeatureGroup(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			version,
			previousFeatureGroup: previousFeatureGroupData,
			featureGroup: featureGroupUpdateData
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});

	});

	it('Open Editor', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const editorData = FeatureGroupStoreFactory.build();
		deepFreeze(editorData);
		const LICENSE_MODEL_ID = '123';

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: []};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/license-key-groups`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: []};
		});


		return FeatureGroupsActionHelper.openFeatureGroupsEditor(store.dispatch, {featureGroup: editorData, licenseModelId: '123', version}).then(() => {
			expect(store.getState().licenseModel.featureGroup.featureGroupEditor.data).toEqual(editorData);
		});
	});

});

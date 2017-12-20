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
import EntitlementPoolsActionHelper from 'sdc-app/onboarding/licenseModel/entitlementPools/EntitlementPoolsActionHelper.js';
import {EntitlementPoolStoreFactory, EntitlementPoolPostFactory} from 'test-utils/factories/licenseModel/EntitlementPoolFactories.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import {LimitItemFactory, LimitPostFactory} from 'test-utils/factories/licenseModel/LimitFactories.js';
import {getStrValue} from 'nfvo-utils/getValue.js';
import {SyncStates} from 'sdc-app/common/merge/MergeEditorConstants.js';
import CurrentScreenFactory from 'test-utils/factories/common/CurrentScreenFactory.js';

describe('Entitlement Pools Module Tests', function () {

	const LICENSE_MODEL_ID = '555';
	const version = VersionFactory.build();
	const itemPermissionAndProps = CurrentScreenFactory.build({}, {version});
	const returnedVersionFields = {baseId: version.baseId, description: version.description, id: version.id, name: version.name, status: version.status};

	it('Load Entitlement Pools List', () => {

		const entitlementPoolsList = buildListFromFactory(EntitlementPoolStoreFactory);
		deepFreeze(entitlementPoolsList);
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolsList', entitlementPoolsList);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: entitlementPoolsList};
		});

		return EntitlementPoolsActionHelper.fetchEntitlementPoolsList(store.dispatch, {licenseModelId: LICENSE_MODEL_ID, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Delete Entitlement Pool', () => {

		const entitlementPoolsList = buildListFromFactory(EntitlementPoolStoreFactory,1);
		deepFreeze(entitlementPoolsList);
		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				entitlementPool: {
					entitlementPoolsList
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

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolsList', []);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools/${entitlementPoolsList[0].id}`);
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

		return EntitlementPoolsActionHelper.deleteEntitlementPool(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			version,
			entitlementPoolId: entitlementPoolsList[0].id
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Add Entitlement Pool', () => {

		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps}
		});
		deepFreeze(store.getState());

		const EntitlementPoolPostRequest = EntitlementPoolPostFactory.build();

		deepFreeze(EntitlementPoolPostRequest);

		const entitlementPoolIdFromResponse = 'ADDED_ID';
		const entitlementPoolAfterAdd = EntitlementPoolStoreFactory.build({id: entitlementPoolIdFromResponse});
		deepFreeze(entitlementPoolAfterAdd);

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolsList', [entitlementPoolAfterAdd]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('post', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools`);
			expect(data).toEqual(EntitlementPoolPostRequest);
			expect(options).toEqual(undefined);
			return {
				returnCode: 'OK',
				value: entitlementPoolIdFromResponse
			};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
		});

		return EntitlementPoolsActionHelper.saveEntitlementPool(store.dispatch,
			{
				licenseModelId: LICENSE_MODEL_ID,
				version,
				previousEntitlementPool: null,
				entitlementPool: EntitlementPoolPostRequest
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Update Entitlement Pool', () => {

		const entitlementPoolsList = buildListFromFactory(EntitlementPoolStoreFactory, 1);
		deepFreeze(entitlementPoolsList);

		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				entitlementPool: {
					entitlementPoolsList
				}
			}
		});

		deepFreeze(store.getState());

		const toBeUpdatedEntitlementPoolId = entitlementPoolsList[0].id;
		const previousEntitlementPoolData = entitlementPoolsList[0];
		const entitlementPoolUpdateData = EntitlementPoolStoreFactory.build({name: 'ep1_UPDATED', description: 'string_UPDATED', id: toBeUpdatedEntitlementPoolId});
		deepFreeze(entitlementPoolUpdateData);

		const entitlementPoolPutRequest = EntitlementPoolPostFactory.build({name: 'ep1_UPDATED', description: 'string_UPDATED'});
		deepFreeze(entitlementPoolPutRequest);

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolsList', [entitlementPoolUpdateData]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);


		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools/${toBeUpdatedEntitlementPoolId}`);
			expect(data).toEqual(entitlementPoolPutRequest);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${LICENSE_MODEL_ID}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...returnedVersionFields, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: true}};
		});

		return EntitlementPoolsActionHelper.saveEntitlementPool(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			version,
			previousEntitlementPool: previousEntitlementPoolData,
			entitlementPool: entitlementPoolUpdateData
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Load Limits List', () => {

		const limitsList = LimitItemFactory.buildList(3);
		deepFreeze(limitsList);
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolEditor.limitsList', limitsList);
		const entitlementPool = EntitlementPoolStoreFactory.build();
		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools/${entitlementPool.id}/limits`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: limitsList};
		 });

		return EntitlementPoolsActionHelper.fetchLimits(store.dispatch, {licenseModelId: LICENSE_MODEL_ID, version, entitlementPool}).then(() => {
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
		const entitlementPool = EntitlementPoolStoreFactory.build();

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolEditor.limitsList', [limitAddedItem]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('post', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools/${entitlementPool.id}/limits`);
			expect(data).toEqual(limitFromBE);
			expect(options).toEqual(undefined);
			return {
				returnCode: 'OK',
				value: LimitIdFromResponse
			};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools/${entitlementPool.id}/limits`);
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

		return EntitlementPoolsActionHelper.submitLimit(store.dispatch,
			{
				licenseModelId: LICENSE_MODEL_ID,
				version,
				entitlementPool,
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

		const entitlementPool = EntitlementPoolStoreFactory.build();

		const expectedCurrentScreenProps = {
			...itemPermissionAndProps,
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isDirty: true
			}
		};

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolEditor.limitsList', []);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools/${entitlementPool.id}/limits/${limitsList[0].id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {
				results: {
					returnCode: 'OK'
				}
			};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools/${entitlementPool.id}/limits`);
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

		return EntitlementPoolsActionHelper.deleteLimit(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			version,
			entitlementPool,
			limit: limitsList[0]
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Update Limit', () => {

		const limitsList = LimitItemFactory.buildList(1);
		deepFreeze(limitsList);
		const entitlementPool = EntitlementPoolStoreFactory.build();
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

		let expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolEditor.limitsList', [updatedLimitForPut]);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);


		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools/${entitlementPool.id}/limits/${limitId}`);
			expect(data).toEqual(updatedLimitForPut);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools/${entitlementPool.id}/limits`);
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

		return EntitlementPoolsActionHelper.submitLimit(store.dispatch,
			{
				licenseModelId: LICENSE_MODEL_ID,
				version,
				entitlementPool,
				limit: updatedLimit
			}
		).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

});

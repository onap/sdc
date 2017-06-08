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
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';

describe('Entitlement Pools Module Tests', function () {

	const LICENSE_MODEL_ID = '555';
	const version = VersionControllerUtilsFactory.build().version;

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
			licenseModel: {
				entitlementPool: {
					entitlementPoolsList
				}
			}
		});
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolsList', []);

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

		return EntitlementPoolsActionHelper.deleteEntitlementPool(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			version,
			entitlementPoolId: entitlementPoolsList[0].id
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Add Entitlement Pool', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const EntitlementPoolPostRequest = EntitlementPoolPostFactory.build();

		deepFreeze(EntitlementPoolPostRequest);

		const entitlementPoolIdFromResponse = 'ADDED_ID';
		const entitlementPoolAfterAdd = EntitlementPoolStoreFactory.build({id: entitlementPoolIdFromResponse});
		deepFreeze(entitlementPoolAfterAdd);

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolsList', [entitlementPoolAfterAdd]);

		mockRest.addHandler('post', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools`);
			expect(data).toEqual(EntitlementPoolPostRequest);
			expect(options).toEqual(undefined);
			return {
				returnCode: 'OK',
				value: entitlementPoolIdFromResponse
			};
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

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolsList', [entitlementPoolUpdateData]);


		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/versions/${version.id}/entitlement-pools/${toBeUpdatedEntitlementPoolId}`);
			expect(data).toEqual(entitlementPoolPutRequest);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
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

});

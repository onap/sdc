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

import deepFreeze from 'deep-freeze';
import {expect} from 'chai';
import mockRest from 'test-utils/MockRest.js';
import {cloneAndSet} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import EntitlementPoolsActionHelper from 'sdc-app/onboarding/licenseModel/entitlementPools/EntitlementPoolsActionHelper.js';

describe('Entitlement Pools Module Tests', function () {

	const LICENSE_MODEL_ID = '555';

	it('Load Entitlement Pools List', () => {
		const entitlementPoolsList = [
			{
				name: 'ep1',
				description: 'string',
				thresholdValue: 75,
				thresholdUnits: '%',
				entitlementMetric: {'choice': 'User', 'other': ''},
				increments: 'string',
				aggregationFunction: {'choice': 'Average', 'other': ''},
				operationalScope: {'choices': ['Other'], 'other': 'blabla'},
				time: {'choice': 'Hour', 'other': ''},
				sku: 'DEF2-385A-4521-AAAA',
				id: '1',
				referencingFeatureGroups: [],
				partNumber: '51529'
			}
		];
		deepFreeze(entitlementPoolsList);
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolsList', entitlementPoolsList);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/entitlement-pools`);
			expect(data).to.equal(undefined);
			expect(options).to.equal(undefined);
			return {results: entitlementPoolsList};
		});

		return EntitlementPoolsActionHelper.fetchEntitlementPoolsList(store.dispatch, {licenseModelId: LICENSE_MODEL_ID}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Delete Entitlement Pool', () => {
		const entitlementPoolsList = [
			{
				name: 'ep1',
				description: 'string',
				thresholdValue: 75,
				thresholdUnits: '%',
				entitlementMetric: {'choice': 'User', 'other': ''},
				increments: 'string',
				aggregationFunction: {'choice': 'Average', 'other': ''},
				operationalScope: {'choices': ['Other'], 'other': 'blabla'},
				time: {'choice': 'Hour', 'other': ''},
				sku: 'DEF2-385A-4521-AAAA',
				id: '1',
				referencingFeatureGroups: [],
				partNumber: '51529'
			}
		];

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
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/entitlement-pools/${entitlementPoolsList[0].id}`);
			expect(data).to.equal(undefined);
			expect(options).to.equal(undefined);
			return {
				results: {
					returnCode: 'OK'
				}
			};
		});

		return EntitlementPoolsActionHelper.deleteEntitlementPool(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			entitlementPoolId: entitlementPoolsList[0].id
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Add Entitlement Pool', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const entitlementPoolPostRequest = {
			name: 'ep1',
			description: 'string',
			thresholdValue: 75,
			thresholdUnits: '%',
			entitlementMetric: {'choice': 'User', 'other': ''},
			increments: 'string',
			aggregationFunction: {'choice': 'Average', 'other': ''},
			operationalScope: {'choices': ['Other'], 'other': 'blabla'},
			time: {'choice': 'Hour', 'other': ''},
			manufacturerReferenceNumber: 'DEF2-385A-4521-AAAA',
		};
		const entitlementPoolToAdd = {
			name: 'ep1',
			description: 'string',
			thresholdValue: 75,
			thresholdUnits: '%',
			entitlementMetric: {'choice': 'User', 'other': ''},
			increments: 'string',
			aggregationFunction: {'choice': 'Average', 'other': ''},
			operationalScope: {'choices': ['Other'], 'other': 'blabla'},
			time: {'choice': 'Hour', 'other': ''},
			manufacturerReferenceNumber: 'DEF2-385A-4521-AAAA',
			referencingFeatureGroups: []
		};
		const entitlementPoolIdFromResponse = 'ADDED_ID';
		const entitlementPoolAfterAdd = {
			...entitlementPoolToAdd,
			id: entitlementPoolIdFromResponse
		};

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolsList', [entitlementPoolAfterAdd]);

		mockRest.addHandler('create', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/entitlement-pools`);
			expect(data).to.deep.equal(entitlementPoolPostRequest);
			expect(options).to.equal(undefined);
			return {
				returnCode: 'OK',
				value: entitlementPoolIdFromResponse
			};
		});

		return EntitlementPoolsActionHelper.saveEntitlementPool(store.dispatch,
			{
				licenseModelId: LICENSE_MODEL_ID,
				previousEntitlementPool: null,
				entitlementPool: entitlementPoolToAdd
			}
		).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Update Entitlement Pool', () => {
		const entitlementPoolsList = [{
			name: 'ep1',
			id: '0',
			description: 'string',
			thresholdValue: 75,
			thresholdUnits: '%',
			entitlementMetric: {'choice': 'User', 'other': ''},
			increments: 'string',
			aggregationFunction: {'choice': 'Average', 'other': ''},
			operationalScope: {'choices': ['Other'], 'other': 'blabla'},
			time: {'choice': 'Hour', 'other': ''},
			manufacturerReferenceNumber: 'DEF2-385A-4521-AAAA'
		}];
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
		const entitlementPoolUpdateData = {
			...entitlementPoolsList[0],
			name: 'ep1_UPDATED',
			description: 'string_UPDATED'
		};
		deepFreeze(entitlementPoolUpdateData);

		const entitlementPoolPutRequest = {
			name: 'ep1_UPDATED',
			description: 'string_UPDATED',
			thresholdValue: 75,
			thresholdUnits: '%',
			entitlementMetric: {'choice': 'User', 'other': ''},
			increments: 'string',
			aggregationFunction: {'choice': 'Average', 'other': ''},
			operationalScope: {'choices': ['Other'], 'other': 'blabla'},
			time: {'choice': 'Hour', 'other': ''},
			manufacturerReferenceNumber: 'DEF2-385A-4521-AAAA'
		};
		deepFreeze(entitlementPoolPutRequest);

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.entitlementPool.entitlementPoolsList', [entitlementPoolUpdateData]);


		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/entitlement-pools/${toBeUpdatedEntitlementPoolId}`);
			expect(data).to.deep.equal(entitlementPoolPutRequest);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return EntitlementPoolsActionHelper.saveEntitlementPool(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			previousEntitlementPool: previousEntitlementPoolData,
			entitlementPool: entitlementPoolUpdateData
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

});

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
import FeatureGroupsActionHelper from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupsActionHelper.js';


describe('Feature Groups Module Tests', function () {

	const LICENSE_MODEL_ID = '555';

	it('Load Feature Groups List', () => {
		const featureGroupsList = [
			{
				name: 'fs1',
				id: 0,
				description: 'fs1-d',
				licenseKeyGroupsIds: [1],
				entitlementPoolsIds: [1],
				refCount: 0
			}
		];
		deepFreeze(featureGroupsList);
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.featureGroup.featureGroupsList', featureGroupsList);

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/feature-groups`);
			expect(data).to.equal(undefined);
			expect(options).to.equal(undefined);
			return {results: featureGroupsList};
		});

		return FeatureGroupsActionHelper.fetchFeatureGroupsList(store.dispatch, {licenseModelId: LICENSE_MODEL_ID}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Delete Feature Group', () => {
		const featureGroupsList = [
			{
				name: 'fs1',
				id: 0,
				description: 'fs1-d',
				licenseKeyGroupsIds: [1],
				entitlementPoolsIds: [1],
				refCount: 0
			}
		];
		deepFreeze(featureGroupsList);
		const store = storeCreator({
			licenseModel: {
				featureGroup: {
					featureGroupsList
				}
			}
		});
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.featureGroup.featureGroupsList', []);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/feature-groups/${featureGroupsList[0].id}`);
			expect(data).to.equal(undefined);
			expect(options).to.equal(undefined);
			return {
				results: {
					returnCode: 'OK'
				}
			};
		});

		return FeatureGroupsActionHelper.deleteFeatureGroup(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			featureGroupId: featureGroupsList[0].id
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Add Feature Group', () => {

		const store = storeCreator();
		deepFreeze(store.getState());

		const featureGroupPostRequest = {
			name: 'fs1',
			description: 'fs1-d',
			partNumber: '123',
			addedLicenseKeyGroupsIds: [1],
			addedEntitlementPoolsIds: [1]
		};
		const featureGroupToAdd = {
			name: 'fs1',
			description: 'fs1-d',
			partNumber: '123',
			licenseKeyGroupsIds: [1],
			entitlementPoolsIds: [1]
		};
		const featureGroupIdFromResponse = 'ADDED_ID';
		const featureGroupAfterAdd = {
			...featureGroupToAdd,
			id: featureGroupIdFromResponse
		};

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.featureGroup.featureGroupsList', [featureGroupAfterAdd]);

		mockRest.addHandler('create', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/feature-groups`);
			expect(data).to.deep.equal(featureGroupPostRequest);
			expect(options).to.equal(undefined);
			return {
				returnCode: 'OK',
				value: featureGroupIdFromResponse
			};
		});

		return FeatureGroupsActionHelper.saveFeatureGroup(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			featureGroup: featureGroupToAdd
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Update Feature Group', () => {
		const featureGroupsList = [{
			name: 'fs1',
			id: 0,
			description: 'fs1-d',
			partNumber: '123',
			licenseKeyGroupsIds: [1],
			entitlementPoolsIds: [1],
			refCount: 0
		}];
		deepFreeze(featureGroupsList);

		const store = storeCreator({
			licenseModel: {
				featureGroup: {
					featureGroupsList
				}
			}
		});
		deepFreeze(store.getState());

		const toBeUpdatedFeatureGroupId = featureGroupsList[0].id;
		const previousFeatureGroupData = featureGroupsList[0];
		const featureGroupUpdateData = {
			...featureGroupsList[0],
			name: 'fs_UPDATED',
			description: 'description_UPDATED',
			partNumber: '123_UPDATED',
			licenseKeyGroupsIds: [7],
			entitlementPoolsIds: [7]
		};
		deepFreeze(featureGroupUpdateData);

		const featureGroupPutRequest = {
			name: 'fs_UPDATED',
			description: 'description_UPDATED',
			partNumber: '123_UPDATED',
			addedLicenseKeyGroupsIds: [7],
			addedEntitlementPoolsIds: [7],
			removedLicenseKeyGroupsIds: [1],
			removedEntitlementPoolsIds: [1]
		};
		deepFreeze(featureGroupPutRequest);

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.featureGroup.featureGroupsList', [featureGroupUpdateData]);


		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/feature-groups/${toBeUpdatedFeatureGroupId}`);
			expect(data).to.deep.equal(featureGroupPutRequest);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return FeatureGroupsActionHelper.saveFeatureGroup(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			previousFeatureGroup: previousFeatureGroupData,
			featureGroup: featureGroupUpdateData
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

});

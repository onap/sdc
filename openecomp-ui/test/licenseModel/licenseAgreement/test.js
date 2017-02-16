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
import LicenseAgreementActionHelper from 'sdc-app/onboarding/licenseModel/licenseAgreement/LicenseAgreementActionHelper.js';


describe('License Agreement Module Tests', () => {

	const LICENSE_MODEL_ID = '777';

	it('Load License Agreement List', () => {
		const licenseAgreementList = [
			{
				id: '0',
				name: 'name0',
				description: 'description0',
				licenseTerm: 'licenseTerm0',
				requirementsAndConstrains: 'req_and_constraints0',
				featureGroupsIds: ['77']
			}
		];
		deepFreeze(licenseAgreementList);
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseAgreement.licenseAgreementList', licenseAgreementList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/license-agreements`);
			expect(data).to.equal(undefined);
			expect(options).to.equal(undefined);
			return {results: licenseAgreementList};
		});
		return LicenseAgreementActionHelper.fetchLicenseAgreementList(store.dispatch, {licenseModelId: LICENSE_MODEL_ID}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Delete License Agreement', () => {
		const licenseAgreementList = [
			{
				id: '0',
				name: 'name0',
				description: 'description0',
				licenseTerm: 'licenseTerm0',
				requirementsAndConstrains: 'req_and_constraints0',
				featureGroupsIds: ['77']
			}
		];
		deepFreeze(licenseAgreementList);
		const store = storeCreator({
			licenseModel: {
				licenseAgreement: {
					licenseAgreementList
				}
			}
		});
		deepFreeze(store.getState());
		const toBeDeletedLicenseAgreementId = '0';
		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseAgreement.licenseAgreementList', []);

		mockRest.addHandler('destroy', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/license-agreements/${toBeDeletedLicenseAgreementId}`);
			expect(data).to.equal(undefined);
			expect(options).to.equal(undefined);
		});

		return LicenseAgreementActionHelper.deleteLicenseAgreement(store.dispatch, {
			licenseAgreementId: toBeDeletedLicenseAgreementId,
			licenseModelId: LICENSE_MODEL_ID
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Add License Agreement', () => {
		const store = storeCreator();
		deepFreeze(store.getState());
		const licenseAgreementPostRequest = {
			name: 'name_ADDED_LA',
			description: 'description_ADDED_LA',
			licenseTerm: 'licenseTerm_ADDED_LA',
			requirementsAndConstrains: 'req_and_constraints_ADDED_LA',
			addedFeatureGroupsIds: []
		};
		deepFreeze(licenseAgreementPostRequest);

		const licenseAgreementToAdd = {
			name: 'name_ADDED_LA',
			description: 'description_ADDED_LA',
			licenseTerm: 'licenseTerm_ADDED_LA',
			requirementsAndConstrains: 'req_and_constraints_ADDED_LA',
			featureGroupsIds: []
		};
		deepFreeze(licenseAgreementToAdd);

		const licenseAgreementIdFromResponse = 'ADDED_ID';
		const licenseAgreementAfterAdd = {
			...licenseAgreementToAdd,
			id: licenseAgreementIdFromResponse
		};
		deepFreeze(licenseAgreementAfterAdd);

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseAgreement.licenseAgreementList', [licenseAgreementAfterAdd]);

		mockRest.addHandler('create', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/license-agreements`);
			expect(data).to.deep.equal(licenseAgreementPostRequest);
			expect(options).to.equal(undefined);
			return {
				value: licenseAgreementIdFromResponse
			};
		});

		return LicenseAgreementActionHelper.saveLicenseAgreement(store.dispatch, {
			licenseAgreement: licenseAgreementToAdd,
			licenseModelId: LICENSE_MODEL_ID
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Update License Agreement', () => {
		const licenseAgreementList = [
			{
				id: '0',
				name: 'name0',
				description: 'description0',
				licenseTerm: 'licenseTerm0',
				requirementsAndConstrains: 'req_and_constraints0',
				featureGroupsIds: ['77']
			}
		];
		const store = storeCreator({
			licenseModel: {
				licenseAgreement: {
					licenseAgreementList
				}
			}
		});
		deepFreeze(store.getState());

		const toBeUpdatedLicenseAgreementId = licenseAgreementList[0].id;
		const previousLicenseAgreementData = licenseAgreementList[0];

		const licenseAgreementUpdateData = {
			...licenseAgreementList[0],
			name: 'name_UPDATED',
			description: 'description_UPDATED',
			licenseTerm: 'licenseTerm_UPDATED_LA',
			requirementsAndConstrains: 'req_and_constraints_UPDATED_LA',
			featureGroupsIds: ['update_id_1', 'update_id_2']
		};
		deepFreeze(licenseAgreementUpdateData);

		const licenseAgreementPutRequest = {
			name: 'name_UPDATED',
			description: 'description_UPDATED',
			licenseTerm: 'licenseTerm_UPDATED_LA',
			requirementsAndConstrains: 'req_and_constraints_UPDATED_LA',
			addedFeatureGroupsIds: ['update_id_1', 'update_id_2'],
			removedFeatureGroupsIds: ['77']
		};
		deepFreeze(licenseAgreementPutRequest);

		const expectedStore = cloneAndSet(store.getState(), 'licenseModel.licenseAgreement.licenseAgreementList', [licenseAgreementUpdateData]);

		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-license-models/${LICENSE_MODEL_ID}/license-agreements/${toBeUpdatedLicenseAgreementId}`);
			expect(data).to.deep.equal(licenseAgreementPutRequest);
			expect(options).to.equal(undefined);
		});

		return LicenseAgreementActionHelper.saveLicenseAgreement(store.dispatch, {
			licenseModelId: LICENSE_MODEL_ID,
			previousLicenseAgreement: previousLicenseAgreementData,
			licenseAgreement: licenseAgreementUpdateData
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

});

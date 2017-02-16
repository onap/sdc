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
import LicenseModelCreationActionHelper from 'sdc-app/onboarding/licenseModel/creation/LicenseModelCreationActionHelper.js';

describe('License Model Module Tests', function () {
	it('Add License Model', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const licenseModelPostRequest = deepFreeze({
			vendorName: 'vlm1',
			description: 'string',
			iconRef: 'icon'
		});

		const licenseModelToAdd = deepFreeze({
			...licenseModelPostRequest
		});

		const licenseModelIdFromResponse = 'ADDED_ID';
		const licenseModelAfterAdd = deepFreeze({
			...licenseModelToAdd,
			id: licenseModelIdFromResponse
		});

		const expectedStore = cloneAndSet(store.getState(), 'licenseModelList', [licenseModelAfterAdd]);

		mockRest.addHandler('create', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal('/onboarding-api/v1.0/vendor-license-models/');
			expect(data).to.deep.equal(licenseModelPostRequest);
			expect(options).to.equal(undefined);
			return {
				value: licenseModelIdFromResponse
			};
		});

		return LicenseModelCreationActionHelper.createLicenseModel(store.dispatch, {
			licenseModel: licenseModelToAdd
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});
});

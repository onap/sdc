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
import SoftwareProductNetworksActionHelper from 'sdc-app/onboarding/softwareProduct/networks/SoftwareProductNetworksActionHelper.js';

const softwareProductId = '123';

describe('Software Product Networks ActionHelper Tests', function () {
	it('Get Software Products Networks List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const networksList = [
			{
				name:'dummy_net_1',
				dhcp:true,
				'id':'7F60CD390458421DA588AF4AD217B93F'
			},
			{
				name:'dummy_net_2',
				dhcp:true,
				'id':'AD217B93F7F60CD390458421DA588AF4'
			}
		];

		deepFreeze(networksList);
		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductNetworks.networksList', networksList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/networks`);
			expect(data).to.deep.equal(undefined);
			expect(options).to.equal(undefined);
			return {results: networksList};
		});

		return SoftwareProductNetworksActionHelper.fetchNetworksList(store.dispatch, {softwareProductId}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});

	});
});

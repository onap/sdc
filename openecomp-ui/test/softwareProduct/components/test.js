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
import SoftwareProductComponentsActionHelper from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsActionHelper.js';

const softwareProductId = '123';
const vspComponentId = '321';

describe('Software Product Components Module Tests', function () {
	it('Get Software Products Components List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductComponentsList = [
			{
				name: 'com.d2.resource.vfc.nodes.heat.sm_server',
				displayName: 'sm_server',
				description: 'hjhj',
				id: 'EBADF561B7FA4A788075E1840D0B5971'
			},
			{
				name: 'com.d2.resource.vfc.nodes.heat.pd_server',
				displayName: 'pd_server',
				description: 'hjhj',
				id: '2F47447D22DB4C53B020CA1E66201EF2'
			}
		];

		deepFreeze(softwareProductComponentsList);

		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentsList', softwareProductComponentsList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components`);
			expect(data).to.deep.equal(undefined);
			expect(options).to.equal(undefined);
			return {results: softwareProductComponentsList};
		});

		return SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(store.dispatch, {softwareProductId}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Update SoftwareProduct Component Questionnaire', () => {
		const store = storeCreator();

		const qdataUpdated = {
			general: {
				hypervisor: {
					containerFeatureDescription: 'aaaUpdated',
					drivers: 'bbbUpdated',
					hypervisor: 'cccUpdated'
				}
			}
		};

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentEditor.qdata', qdataUpdated);
		deepFreeze(expectedStore);


		mockRest.addHandler('save', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/components/${vspComponentId}/questionnaire`);
			expect(data).to.deep.equal(qdataUpdated);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(store.dispatch, {softwareProductId, vspComponentId, qdata: qdataUpdated}).then(() => {
			//TODO think should we add here something or not
		});


	});

});


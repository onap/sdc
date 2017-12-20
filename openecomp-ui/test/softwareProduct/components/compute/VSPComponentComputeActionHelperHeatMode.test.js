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
import {cloneAndSet} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import Configuration from 'sdc-app/config/Configuration.js';
import SoftwareProductComponentsActionHelper from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsActionHelper.js';

import {ComputeFlavorQData, VSPComponentsComputeDataMapFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsComputeFactory.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';

const softwareProductId = '123';
const vspComponentId = '111';
const version = VersionFactory.build();

describe('Software Product Components Compute Module Tests - HEAT mode', function () {

	let restPrefix = '';

	beforeAll(function() {
		restPrefix = Configuration.get('restPrefix');
		deepFreeze(restPrefix);
	});

	it('Get Software Products Components Compute', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const compute = ComputeFlavorQData.build();
		const dataMap = VSPComponentsComputeDataMapFactory.build();

		const softwareProductComponentCompute = {
			data: JSON.stringify(compute),
			schema: JSON.stringify(compute)
		};
		deepFreeze(softwareProductComponentCompute);

		const softwareProductComponentComputeData = {
			qdata: compute,
			dataMap,
			qgenericFieldInfo: {}
		};
		deepFreeze(softwareProductComponentComputeData);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentEditor', softwareProductComponentComputeData);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${vspComponentId}/questionnaire`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return softwareProductComponentCompute;
		});

		return SoftwareProductComponentsActionHelper.fetchSoftwareProductComponentQuestionnaire(store.dispatch, {softwareProductId, version, vspComponentId}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Get Empty Software Products Components Compute', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const compute = ComputeFlavorQData.build();

		const softwareProductComponentQuestionnaire = {
			data: null,
			schema: JSON.stringify(compute)
		};
		deepFreeze(softwareProductComponentQuestionnaire);

		const softwareProductComponentQuestionnaireData = {
			qdata: {},
			dataMap: {},
			qgenericFieldInfo: {}
		};
		deepFreeze(softwareProductComponentQuestionnaireData);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentEditor', softwareProductComponentQuestionnaireData);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${vspComponentId}/questionnaire`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return softwareProductComponentQuestionnaire;
		});

		return SoftwareProductComponentsActionHelper.fetchSoftwareProductComponentQuestionnaire(store.dispatch, {softwareProductId, version, vspComponentId}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

});

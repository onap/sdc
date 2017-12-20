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

import {default as VSPComponentsStorageFactory, VSPComponentsStorageDataMapFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsStorageFactory.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';

const softwareProductId = '123';
const vspComponentId = '111';
const version = VersionFactory.build();

describe('Software Product Components Storage Module Tests', function () {

	let restPrefix = '';

	beforeAll(function() {
		restPrefix = Configuration.get('restPrefix');
		deepFreeze(restPrefix);
	});

	it('Get Software Products Components Storage', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const storage = VSPComponentsStorageFactory.build();
		const dataMap = VSPComponentsStorageDataMapFactory.build();

		const softwareProductComponentStorage = {
			data: JSON.stringify(storage),
			schema: JSON.stringify(storage)
		};
		deepFreeze(softwareProductComponentStorage);

		const softwareProductComponentStorageData = {
			qdata: storage,
			dataMap: dataMap,
			qgenericFieldInfo: {}
		};
		deepFreeze(softwareProductComponentStorageData);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentEditor', softwareProductComponentStorageData);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${vspComponentId}/questionnaire`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return softwareProductComponentStorage;
		});

		return SoftwareProductComponentsActionHelper.fetchSoftwareProductComponentQuestionnaire(store.dispatch, {softwareProductId, version, vspComponentId}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Get Empty Software Products Components Storage', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const storage = VSPComponentsStorageFactory.build();

		const softwareProductComponentQuestionnaire = {
			data: null,
			schema: JSON.stringify(storage)
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

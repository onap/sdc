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
import SoftwareProductComponentsActionHelper from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsActionHelper.js';

import {VSPComponentsFactory, VSPComponentsGeneralFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsFactories.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';

const softwareProductId = '123';
const vspComponentId = '321';
const version = VersionFactory.build();

describe('Software Product Components Module Tests', function () {
	it('Get Software Products Components List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductComponentsList = [
			VSPComponentsFactory.build({}, {componentName: 'sd', componentType: 'server'}),
			VSPComponentsFactory.build({}, {componentName: 'pd', componentType: 'server'})
		];

		deepFreeze(softwareProductComponentsList);

		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentsList', softwareProductComponentsList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: softwareProductComponentsList};
		});

		return SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(store.dispatch, {softwareProductId, version}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Update SoftwareProduct Component Questionnaire', () => {
		const store = storeCreator();

		const qdataUpdated = {
			general: VSPComponentsGeneralFactory.build()
		};

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentEditor.qdata', qdataUpdated);
		deepFreeze(expectedStore);


		mockRest.addHandler('put', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${vspComponentId}/questionnaire`);
			expect(data).toEqual(qdataUpdated);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(store.dispatch, {softwareProductId, version, vspComponentId, qdata: qdataUpdated}).then(() => {
			//TODO think should we add here something or not
		});


	});

});

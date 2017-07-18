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
import {storeCreator} from 'sdc-app/AppStore.js';
import SoftwareProductComponentsActionHelper from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsActionHelper.js';
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';
import {VSPComponentsFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsFactories.js';

const softwareProductId = '123';
const version = VersionControllerUtilsFactory.build().version;

describe('Software Product Components Action Helper Tests', function () {

	it('Load components list', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const expectedComponentData = VSPComponentsFactory.build();
		deepFreeze(expectedComponentData);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return ({results: [expectedComponentData]});
		});

		return SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(store.dispatch, {softwareProductId, version}).then(() => {
			expect(store.getState().softwareProduct.softwareProductComponents.componentsList).toEqual([expectedComponentData]);
		});

	});
});

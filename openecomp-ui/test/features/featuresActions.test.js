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
import {cloneAndSet} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import mockRest from 'test-utils/MockRest.js';
import FeatureFactorie  from 'test-utils/factories/features/FeaturesFactorie.js';
import FeaturesActionHelper from 'sdc-app/features/FeaturesActionHelper.js'; 

describe('Feature Toggle data flow Tests', function () {
	it('Fetch features list test', () => {
		const featuresList = FeatureFactorie.buildList(3);
		deepFreeze(featuresList);
		const store = storeCreator();
		deepFreeze(store.getState());
		const expectedStore = cloneAndSet(store.getState(), 'features', featuresList);


		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual('/onboarding-api/v1.0/togglz');
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {features: featuresList};
		});

		return FeaturesActionHelper.getFeaturesList(store.dispatch).then(() => {			
			expect(store.getState().features).toEqual(expectedStore.features);
		});

	});
});
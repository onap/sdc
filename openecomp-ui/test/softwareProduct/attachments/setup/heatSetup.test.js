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
import React from 'react';

import TestUtils from 'react-addons-test-utils';
import {mapStateToProps} from 'sdc-app/onboarding/softwareProduct/attachments/setup/HeatSetup.js';
import HeatSetupView from 'sdc-app/onboarding/softwareProduct/attachments/setup/HeatSetupView.jsx';
import {storeCreator} from 'sdc-app/AppStore.js';

describe('Heat Setup View test: ', function () {
	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('should mapper return basic data', () => {

		const store = storeCreator();

		var result = mapStateToProps(store.getState());
		expect(result).toBeTruthy();
		expect(result.modules.length).toEqual(0);
		expect(result.unassigned.length).toEqual(0);
		expect(result.artifacts.length).toEqual(0);
		expect(result.nested.length).toEqual(0);
	});

	it('view test', () => {

		const store = storeCreator();

		var params = mapStateToProps(store.getState());

		let heatSetupView = TestUtils.renderIntoDocument(<HeatSetupView {...params}/>);
		expect(heatSetupView).toBeTruthy();
	});
});

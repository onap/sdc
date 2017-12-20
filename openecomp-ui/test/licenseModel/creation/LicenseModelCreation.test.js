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
import {mapStateToProps } from 'sdc-app/onboarding/licenseModel/creation/LicenseModelCreation.js';
import LicenseModelCreationView from 'sdc-app/onboarding/licenseModel/creation/LicenseModelCreationView.jsx';
import {LicenseModelCreationFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';

describe('License Model Creation Module Tests', function() {
	it ('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it ('should return empty data', () => {
		let state = {
			licenseModelList: [],
			licenseModel: {
				licenseModelCreation: {
					data: {}
				}
			},
			users: {
				usersList: []
			}
		};
		let props = mapStateToProps(state);
		expect(props.data).toEqual({});
	});

	it ('should return vlm names list', () => {
		let state = {
			licenseModelList: [{
				name: 'vlm1',
				id: 'vlm1_id'
			}, {
				name: 'vlm2',
				id: 'vlm2_id'
			}],
			licenseModel: {
				licenseModelCreation: {
					data: {}
				}
			},
			users: {
				usersList: []
			}
		};
		let props = mapStateToProps(state);
		expect(props.data).toEqual({});
		expect(props.VLMNames).toEqual({vlm1: 'vlm1_id', vlm2: 'vlm2_id'});
	});

	it('simple jsx test', () => {
		let data = LicenseModelCreationFactory.build();
		var renderer = TestUtils.createRenderer();
		renderer.render(
			<LicenseModelCreationView
				data={data}
				onDataChanged={() => {}}
				onValidateForm={() => {}}
				onSubmit={() => {}}
				onCancel={() => {}}/>
			);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});
});

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
import {mapStateToProps} from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupEditor.js';
import FeatureGroupEditorView from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupEditorView.jsx';
import {LicenseModelOverviewFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';
import {FeatureGroupStoreFactory} from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';

describe('License Model  Feature Groups Editor Module Tests', function () {

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('should return empty data', () => {

		let licenseModel = LicenseModelOverviewFactory.build({
			featureGroup: {
				featureGroupEditor: {
					data: FeatureGroupStoreFactory.build()
				},
				featureGroupsList: []
			}
		});

		var results = mapStateToProps({licenseModel});
		expect(results.entitlementPoolsList).toEqual([]);
		expect(results.licenseKeyGroupsList).toEqual([]);
	});

	it ('should return fg names list', () => {
		let licenseModel = LicenseModelOverviewFactory.build({
			featureGroup: {
				featureGroupEditor: {
					data: FeatureGroupStoreFactory.build()
				},
				featureGroupsList: [{
					name: 'fg1',
					id: 'fg1_id'
				}, {
					name: 'fg2',
					id: 'fg2_id'
				}]
			}
		});
		var results = mapStateToProps({licenseModel});
		expect(results.FGNames).toEqual({fg1: 'fg1_id', fg2: 'fg2_id'});
	});

	it('jsx view test', () => {
		var view = TestUtils.renderIntoDocument(<FeatureGroupEditorView
			isReadOnlyMode={true}
			onTabSelect={() => {}}
			entitlementPoolsList={[{id: '1', name: '1'}]}
			licenseKeyGroupsList={[{id: '1', name: '1'}]}/>);
		expect(view).toBeTruthy();
	});

});

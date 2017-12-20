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

import {mapStateToProps, SummaryCountList} from 'sdc-app/onboarding/licenseModel/overview/summary/SummaryCountList.js';
import LicenseModelDescriptionEdit from 'sdc-app/onboarding/licenseModel/overview/summary/LicenseModelDescriptionEdit.jsx';
import {overviewItems} from 'sdc-app/onboarding/licenseModel/overview/LicenseModelOverviewConstants.js';
import {LicenseModelOverviewFactory, LicenseModelStoreFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';

describe('License Model Overview Summary Count List module test', () => {

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('mapper data return test',() => {

		var obj = {
			licenseModel: LicenseModelOverviewFactory.build({
				entitlementPool: {
					entitlementPoolsList: []
				},
				licenseAgreement: {
					licenseAgreementList: []
				},
				featureGroup: {
					featureGroupsList: []
				},
				licenseKeyGroup: {
					licenseKeyGroupsList: []
				}
			})
		};

		let counts = [
			{name: overviewItems.LICENSE_AGREEMENTS, count: 0},
			{name: overviewItems.FEATURE_GROUPS, count: 0},
			{name: overviewItems.ENTITLEMENT_POOLS, count: 0},
			{name: overviewItems.LICENSE_KEY_GROUPS, count: 0},
		];

		var result = mapStateToProps(obj);
		expect(result.description).toEqual(obj.licenseModel.licenseModelEditor.data.description);
		expect(result.counts).toEqual(counts);
	});

	it('jsx view test', () => {

		var counts = [
			{name: overviewItems.LICENSE_AGREEMENTS, count: 0},
			{name: overviewItems.FEATURE_GROUPS, count: 0},
			{name: overviewItems.ENTITLEMENT_POOLS, count: 0},
			{name: overviewItems.LICENSE_KEY_GROUPS, count: 0},
		];

		var view = TestUtils.renderIntoDocument(<SummaryCountList counts={counts} licenseModelId='1' isReadOnlyMode={false}/>);
		expect(view).toBeTruthy();
	});

	it('description editor jsx view test', () => {
		var data = LicenseModelStoreFactory.build();
		var genericFieldInfo = {
			description: {
				isValid : true
			}
		};
		var view = TestUtils.renderIntoDocument(<LicenseModelDescriptionEdit data={data} genericFieldInfo={genericFieldInfo} description='desc'/>);
		expect(view).toBeTruthy();
	});

});

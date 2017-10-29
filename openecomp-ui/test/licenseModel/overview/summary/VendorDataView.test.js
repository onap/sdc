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
import {mapStateToProps, VendorDataView} from 'sdc-app/onboarding/licenseModel/overview/summary/VendorDataView.js';
import {LicenseModelOverviewFactory, LicenseModelStoreFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';


describe('License Model Overview Summary module test', () => {

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('mapper data return test',() => {

		var state = {
			licenseModel: LicenseModelOverviewFactory.build()
		};

		var props = mapStateToProps(state);
		expect(props.isReadOnlyMode).toEqual(true);
		expect(props.description).toEqual(undefined);
		expect(props.data).toEqual(state.licenseModel.licenseModelEditor.data);

	});

	it('jsx view test', () => {
		var data = LicenseModelStoreFactory.build();
		var view = TestUtils.renderIntoDocument(<VendorDataView isReadOnlyMode={false} description='' data={data} genericFieldInfo={{description: {isValid: true}}}/>);
		expect(view).toBeTruthy();
	});

});

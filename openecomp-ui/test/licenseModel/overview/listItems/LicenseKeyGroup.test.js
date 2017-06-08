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
import LicenseKeyGroup from 'src/sdc-app/onboarding/licenseModel/overview/listItems/LicenseKeyGroup.jsx';
import {LicenseKeyGroupListItemFactory} from 'test-utils/factories/licenseModel/LicenseKeyGroupFactories.js';

describe('LicenseKeyGroup List Item Module Tests', function () {
	it('LicenseKeyGroup List Item should exist', () => {
		expect(LicenseKeyGroup).toBeTruthy();
	});
	it('renders LicenseKeyGroup List Item', () => {
		const lkgData =  LicenseKeyGroupListItemFactory.build();
		const itemView = TestUtils.renderIntoDocument( <LicenseKeyGroup lkgData={lkgData} /> );
		expect(itemView).toBeTruthy();
	});
});

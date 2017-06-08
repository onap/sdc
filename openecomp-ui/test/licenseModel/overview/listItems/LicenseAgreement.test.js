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
import LicenseAgreement from 'src/sdc-app/onboarding/licenseModel/overview/listItems/LicenseAgreement.jsx';
import {scryRenderedDOMComponentsWithTestId} from 'test-utils/Util.js';
import {LicenseAgreementListItemFactory} from 'test-utils/factories/licenseModel/LicenseAgreementFactories.js';
import {FeatureGroupListItemFactory} from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';

describe('License Agreement List Item Module Tests', function () {
	it('License Agreement List Item should exist', () => {
		expect(LicenseAgreement).toBeTruthy();
	});

	it('renders License Agreement List Item Collapsed', () => {
		const laData =  LicenseAgreementListItemFactory.build();
		const itemView = TestUtils.renderIntoDocument( <LicenseAgreement laData={laData} /> );
		expect(itemView).toBeTruthy();
		const elem = TestUtils.scryRenderedDOMComponentsWithClass(itemView,'down');
		expect(elem).toBeTruthy();
	});

	it('renders License Agreement List Item Expanded', () => {
		const laData =  LicenseAgreementListItemFactory.build({isCollpased: false});
		const itemView = TestUtils.renderIntoDocument( <LicenseAgreement laData={laData} /> );
		expect(itemView).toBeTruthy();
		const elem = TestUtils.scryRenderedDOMComponentsWithClass(itemView,'right');
		expect(elem).toBeTruthy();
	});

	it('renders License Agreement List Item with Children Count', () => {
		const fgData =  FeatureGroupListItemFactory.build();
		const laData =  LicenseAgreementListItemFactory.build({children: [fgData]});
		const itemView = TestUtils.renderIntoDocument( <LicenseAgreement laData={laData} /> );
		expect(itemView).toBeTruthy();
		const elem = scryRenderedDOMComponentsWithTestId(itemView,'vlm-list-fg-count-value');
		expect(elem).toBeTruthy();
		expect(elem[0].innerHTML).toBe('1');
	});
});

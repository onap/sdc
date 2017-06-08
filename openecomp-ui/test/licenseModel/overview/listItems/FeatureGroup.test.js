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
import FeatureGroup from 'src/sdc-app/onboarding/licenseModel/overview/listItems/FeatureGroup.jsx';
import {scryRenderedDOMComponentsWithTestId} from 'test-utils/Util.js';
import {FeatureGroupListItemFactory} from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';
import {EntitlementPoolListItemFactory} from 'test-utils/factories/licenseModel/EntitlementPoolFactories.js';
import {LicenseKeyGroupListItemFactory} from 'test-utils/factories/licenseModel/LicenseKeyGroupFactories.js';

describe('Feature Group List Item Module Tests', function () {
	it('Feature Group List Item should exist', () => {
		expect(FeatureGroup).toBeTruthy();
	});

	it('renders Feature Group List Item Collapsed', () => {
		const fgData =  FeatureGroupListItemFactory.build();
		const itemView = TestUtils.renderIntoDocument( <FeatureGroup fgData={fgData} /> );
		expect(itemView).toBeTruthy();
		const elem = TestUtils.scryRenderedDOMComponentsWithClass(itemView,'down');
		expect(elem).toBeTruthy();
	});

	it('renders Feature Group List Item Expanded', () => {
		const fgData =  FeatureGroupListItemFactory.build({isCollpased: false});
		const itemView = TestUtils.renderIntoDocument( <FeatureGroup fgData={fgData} /> );
		expect(itemView).toBeTruthy();
		const elem = TestUtils.scryRenderedDOMComponentsWithClass(itemView,'right');
		expect(elem).toBeTruthy();
	});

	it('renders Feature Group List Item with Children Count', () => {
		const children = [EntitlementPoolListItemFactory.build(), LicenseKeyGroupListItemFactory.build()];
		const fgData =  FeatureGroupListItemFactory.build({children: children, isCollpased: false});
		const itemView = TestUtils.renderIntoDocument( <FeatureGroup fgData={fgData} /> );
		expect(itemView).toBeTruthy();
		let elem = scryRenderedDOMComponentsWithTestId(itemView,'vlm-list-ep-count-value');
		expect(elem).toBeTruthy();
		expect(elem[0].innerHTML).toBe('1');
		elem = scryRenderedDOMComponentsWithTestId(itemView,'vlm-list-lkg-count-value');
		expect(elem).toBeTruthy();
		expect(elem[0].innerHTML).toBe('1');
	});
});

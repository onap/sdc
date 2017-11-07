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
import {scryRenderedDOMComponentsWithTestId} from 'test-utils/Util.js';
import SummaryView from 'sdc-app/onboarding/licenseModel/overview/SummaryView.jsx';
import LicenseModelOverviewView from 'sdc-app/onboarding/licenseModel/overview/LicenseModelOverviewView.jsx';
import VLMListView from 'sdc-app/onboarding/licenseModel/overview/VLMListView.jsx';
import {selectedButton} from 'sdc-app/onboarding/licenseModel/overview/LicenseModelOverviewConstants.js';

import {FeatureGroupListItemFactory} from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';
import {EntitlementPoolListItemFactory} from 'test-utils/factories/licenseModel/EntitlementPoolFactories.js';
import {LicenseKeyGroupListItemFactory} from 'test-utils/factories/licenseModel/LicenseKeyGroupFactories.js';
import {LicenseAgreementListItemFactory} from 'test-utils/factories/licenseModel/LicenseAgreementFactories.js';

describe('License Model Overview - View: ', function () {

	const lkgChild = LicenseKeyGroupListItemFactory.build();

	const epChild = EntitlementPoolListItemFactory.build();

	const baseFGData =  FeatureGroupListItemFactory.build({isCollapsed: false});
	const baseLAData =  LicenseAgreementListItemFactory.build({isCollapse: false});

	it('should render SummaryView', () => {
		var renderer = TestUtils.createRenderer();
		renderer.render(
			<SummaryView />
		);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	it('should render LicenseModelOverviewView', () => {
		let fgData = {...baseFGData};
		fgData.children = Array.of(epChild, lkgChild);
		let laData = {...baseLAData};
		laData.children = [fgData];

		const params = {
			licenseModelId: 'VLM1',
			isDisplayModal: false,
			modalHeader: undefined,
			licensingDataList: [laData],
			orphanDataList: [],
			selectedTab: selectedButton.VLM_LIST_VIEW,
			onTabSelect: () => {}
		};
		var renderer = TestUtils.createRenderer();
		renderer.render(
			<LicenseModelOverviewView {...params}/>
		);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});


	it('should render empty VLMListView', () => {
		const listview = TestUtils.renderIntoDocument( <VLMListView /> );
		expect(listview).toBeTruthy();
		const elem = scryRenderedDOMComponentsWithTestId(listview,'vlm-list');
		expect(elem).toBeTruthy();
		expect(elem[0].children.length).toBe(0);
	});

	it('should render VLMListView with licenseAgreement', () => {
		const listview = TestUtils.renderIntoDocument( <VLMListView licensingDataList={[baseLAData]}/> );
		expect(listview).toBeTruthy();
		let elem = scryRenderedDOMComponentsWithTestId(listview,'vlm-list');
		expect(elem).toBeTruthy();
		expect(elem[0].children.length).toBe(1);
		elem = scryRenderedDOMComponentsWithTestId(listview,'vlm-list-la-item');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('should render VLMListView with Feature Group', () => {
		let laData = {...baseLAData};
		laData.children = [baseFGData];
		const listview = TestUtils.renderIntoDocument( <VLMListView licensingDataList={[laData]}/> );
		expect(listview).toBeTruthy();
		const elem = scryRenderedDOMComponentsWithTestId(listview,'vlm-list-item-fg');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('should render VLMListView with Entitlement Pool', () => {
		let fgData = {...baseFGData};
		fgData.children = [epChild];
		let laData = {...baseLAData};
		laData.children = [fgData];

		const listview = TestUtils.renderIntoDocument( <VLMListView licensingDataList={[laData]} showInUse={true} /> );
		expect(listview).toBeTruthy();
		const elem = scryRenderedDOMComponentsWithTestId(listview,'vlm-list-item-ep');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('should render VLMListView with LicenseKeyGroup', () => {
		let fgData = {...baseFGData};
		fgData.children = [lkgChild];
		let laData = {...baseLAData};
		laData.children = [fgData];

		const listview = TestUtils.renderIntoDocument( <VLMListView licensingDataList={[laData]} showInUse={true} /> );
		expect(listview).toBeTruthy();
		const elem = scryRenderedDOMComponentsWithTestId(listview,'vlm-list-item-lkg');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('should render VLMListView with all items', () => {
		let fgData = {...baseFGData};
		fgData.children = Array.of(epChild, lkgChild);
		let laData = {...baseLAData};
		laData.children = [fgData];

		const listview = TestUtils.renderIntoDocument( <VLMListView licensingDataList={[laData]} showInUse={true} /> );
		expect(listview).toBeTruthy();
		let elem = scryRenderedDOMComponentsWithTestId(listview,'vlm-list-item-fg');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
		elem = scryRenderedDOMComponentsWithTestId(listview,'vlm-list-item-lkg');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
		elem = scryRenderedDOMComponentsWithTestId(listview,'vlm-list-item-ep');
		expect(elem).toBeTruthy();
		expect(elem.length).toBe(1);
	});

	it('should update collapsing item', () => {
		let fgData = {...baseFGData};
		fgData.children = Array.of(epChild, lkgChild);
		let laData = {...baseLAData};
		laData.children = [fgData];

		var renderer = TestUtils.renderIntoDocument(
			<VLMListView licensingDataList={[laData]} showInUse={true}/>
		);
		expect(renderer).toBeTruthy();

		renderer.updateCollapsable(new Event('click'), 'LA1');
		expect(renderer.state['LA1']).toEqual(true);
	});
});

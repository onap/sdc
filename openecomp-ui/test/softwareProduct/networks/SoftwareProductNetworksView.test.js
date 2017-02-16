/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import expect from 'expect';
import React from 'react';
import TestUtils from 'react-addons-test-utils';
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/networks/SoftwareProductNetworks.js';
import SoftwareProductNetworksView from 'sdc-app/onboarding/softwareProduct/networks/SoftwareProductNetworksView.jsx';
import {statusEnum as versionStatusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';

describe('SoftwareProductNetworks Mapper and View Classes', () => {
	it ('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toExist();
	});

	it ('mapStateToProps data test', () => {

		const currentSoftwareProduct = {
			name: 'VSp',
			description: 'dfdf',
			vendorName: 'V1',
			vendorId: '97B3E2525E0640ACACF87CE6B3753E80',
			category: 'resourceNewCategory.application l4+',
			subCategory: 'resourceNewCategory.application l4+.database',
			id: 'D4774719D085414E9D5642D1ACD59D20',
			version: '0.10',
			viewableVersions: ['0.1', '0.2'],
			status: versionStatusEnum.CHECK_OUT_STATUS,
			lockingUser: 'cs0008'
		};

		const networksList =   [
			{
				name:'dummy_net_1',
				dhcp:true,
				'id':'7F60CD390458421DA588AF4AD217B93F'
			},
			{
				name:'dummy_net_2',
				dhcp:true,
				'id':'AD217B93F7F60CD390458421DA588AF4'
			}
		];

		var obj = {
			softwareProduct: {
				softwareProductEditor: {
					data:currentSoftwareProduct
				},
				softwareProductNetworks:
				{
					networksList
				}
			}
		};
		var results = mapStateToProps(obj);
		expect(results.networksList,).toExist();
	});

	it ('view simple test', () => {

		const currentSoftwareProduct = {
			name: 'VSp',
			description: 'dfdf',
			vendorName: 'V1',
			vendorId: '97B3E2525E0640ACACF87CE6B3753E80',
			category: 'resourceNewCategory.application l4+',
			subCategory: 'resourceNewCategory.application l4+.database',
			id: 'D4774719D085414E9D5642D1ACD59D20',
			version: '0.10',
			viewableVersions: ['0.1', '0.2'],
			status: versionStatusEnum.CHECK_OUT_STATUS,
			lockingUser: 'cs0008'
		};

		const networksList =   [
			{
				name:'dummy_net_1',
				dhcp:true,
				'id':'7F60CD390458421DA588AF4AD217B93F'
			},
			{
				name:'dummy_net_2',
				dhcp:true,
				'id':'AD217B93F7F60CD390458421DA588AF4'
			}
		];

		const versionControllerData = {
			version: '1',
			viewableVersions: [],
			status: 'locked',
			isCheckedOut: true
		};

		var renderer = TestUtils.createRenderer();
		renderer.render(<SoftwareProductNetworksView networksList={networksList} versionControllerData={versionControllerData} currentSoftwareProduct={currentSoftwareProduct}/>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();

	});



});

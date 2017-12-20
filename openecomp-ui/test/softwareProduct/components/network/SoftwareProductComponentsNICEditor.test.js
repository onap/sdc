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
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNICEditor.js';
import SoftwareProductComponentsNICEditorView from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNICEditorView.jsx';

import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {SoftwareProductFactory} from 'test-utils/factories/softwareProduct/SoftwareProductFactory.js';
import {VSPComponentsNicFactory, VSPComponentsNetworkQDataFactory, VSPComponentsNicFactoryQGenericFieldInfo,
	VSPComponentsNicFactoryGenericFieldInfo, VSPComponentsNetworkDataMapFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsNetworkFactories.js';
import CurrentScreenFactory from 'test-utils/factories/common/CurrentScreenFactory.js';

describe('Software Product Component Network NIC Editor and View Classes', () => {
	it('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});


	it('mapStateToProps data test', () => {

		const currentSoftwareProduct = VSPEditorFactory.build();

		var obj = {
			currentScreen: CurrentScreenFactory.build(),
			softwareProduct: SoftwareProductFactory.build({
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductComponents: {
					network: {
						nicEditor: {
							data: {},
							qdata: {},
							dataMap: {},
							qgenericFieldInfo: {},
							genericFieldInfo: {}
						}
					}
				}
			})
		};

		var results = mapStateToProps(obj);
		expect(results.isReadOnlyMode).toBe(false);
		expect(results.currentSoftwareProduct).toBeTruthy();
		expect(results.qdata).toBeTruthy();
		expect(results.dataMap).toBeTruthy();
		expect(results.genericFieldInfo).toBeTruthy();
		expect(results.qgenericFieldInfo).toBeTruthy();
		expect(results.data).toBeTruthy();

	});


	it('Software Product Component Network NIC Editor View Test', () => {

		const props = {
			data: VSPComponentsNicFactory.build(),
			qdata: VSPComponentsNetworkQDataFactory.build(),
			dataMap: VSPComponentsNetworkDataMapFactory.build(),
			genericFieldInfo: VSPComponentsNicFactoryGenericFieldInfo.build(),
			qgenericFieldInfo: VSPComponentsNicFactoryQGenericFieldInfo.build(),
			isFormValid: true,
			formReady: false,
			protocols: []
		};

		var renderer = TestUtils.createRenderer();
		renderer.render(<SoftwareProductComponentsNICEditorView {...props}/>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});
});

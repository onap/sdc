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
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNICEditor.js';
import SoftwareProductComponentsNICEditorView from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNICEditorView.jsx';



describe('Software Product Component Network NIC Editor and View Classes', () => {
	it('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toExist();
	});


	it('mapStateToProps data test', () => {

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
			lockingUser: 'cs0008'
		};


		var obj = {
			softwareProduct: {
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductComponents: {
					network: {
						nicEditor: {
							data: {},
							qdata: {},
							qschema: {}
						}
					}
				}
			}
		};

		var results = mapStateToProps(obj);
		expect(results.currentSoftwareProduct).toExist();
		expect(results.qdata).toExist();
		expect(results.qschema).toExist();
		expect(results.data).toExist();
	});


	it('Software Product Component Network NIC Editor View Test', () => {

		const data = {
			name: '',
			description: '',
			networkName: ''
		};

		const qdata = {};
		const qschema = {};

		var renderer = TestUtils.createRenderer();
		renderer.render(
			<SoftwareProductComponentsNICEditorView
				data={data}
				qdata={qdata}
				qschema={qschema}/>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();

	});
});

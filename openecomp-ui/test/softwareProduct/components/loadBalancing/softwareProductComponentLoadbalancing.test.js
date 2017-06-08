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
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/components/loadBalancing/SoftwareProductComponentLoadBalancing.js';
import SoftwareProductComponentLoadBalancingView from 'sdc-app/onboarding/softwareProduct/components/loadBalancing/SoftwareProductComponentLoadBalancingRefView.jsx';

import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {SoftwareProductFactory} from 'test-utils/factories/softwareProduct/SoftwareProductFactory.js';
import {VSPComponentsVersionControllerFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsNetworkFactories.js';
import VSPQSchemaFactory from 'test-utils/factories/softwareProduct/SoftwareProductQSchemaFactory.js';


describe('SoftwareProductComponentLoadBalancing Mapper and View Classes', () => {
	it('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('mapStateToProps data test', () => {

		const currentSoftwareProduct = VSPEditorFactory.build();

		var obj = {
			softwareProduct: SoftwareProductFactory.build({
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductComponents: {
					componentEditor: {
						qdata: {}
					}
				}
			})
		};

		var results = mapStateToProps(obj);
		expect(results.qdata).toBeTruthy();
	});


	it('VSP Components LoadBalancing view test', () => {

		const currentSoftwareProduct = VSPEditorFactory.build();

		const softwareProductComponents = {
			componentEditor: {
				qdata: {},
				qschema: VSPQSchemaFactory.build()
			}
		};

		const versionControllerData = VSPComponentsVersionControllerFactory.build();

		var renderer = TestUtils.createRenderer();
		renderer.render(
			<SoftwareProductComponentLoadBalancingView
				softwareProductComponents={softwareProductComponents}
				versionControllerData={versionControllerData}
				currentSoftwareProduct={currentSoftwareProduct}
				softwareProductId='123'
				componentId='321'/>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});

});

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
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/networks/SoftwareProductNetworks.js';
import SoftwareProductNetworksView from 'sdc-app/onboarding/softwareProduct/networks/SoftwareProductNetworksView.jsx';
//import {statusEnum as versionStatusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';

import VSPNetworkFactory from 'test-utils/factories/softwareProduct/SoftwareProductNetworkFactory.js';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {VSPComponentsVersionControllerFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsNetworkFactories.js';

describe('SoftwareProductNetworks Mapper and View Classes', () => {
	it ('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it ('mapStateToProps data test', () => {

		const currentSoftwareProduct = VSPEditorFactory.build();

		const networksList =   VSPNetworkFactory.buildList(2);

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
		expect(results.networksList).toBeTruthy();
	});

	it ('view simple test', () => {

		const currentSoftwareProduct = VSPEditorFactory.build();

		const networksList = VSPNetworkFactory.buildList(2);

		const versionControllerData = VSPComponentsVersionControllerFactory.build();
		var renderer = TestUtils.createRenderer();
		renderer.render(<SoftwareProductNetworksView isReadOnlyMode={true} networksList={networksList} versionControllerData={versionControllerData} currentSoftwareProduct={currentSoftwareProduct}/>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});



});

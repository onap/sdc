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
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/processes/SoftwareProductProcesses.js';
import SoftwareProductProcessesView from 'sdc-app/onboarding/softwareProduct/processes/SoftwareProductProcessesView.jsx';

import {VSPProcessStoreFactory} from 'test-utils/factories/softwareProduct/SoftwareProductProcessFactories.js';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {VSPComponentsVersionControllerFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsNetworkFactories.js';

describe('SoftwareProductProcesses Mapper and View Classes', () => {
	it ('mapStateToProps mapper exists', () => {		
		expect(mapStateToProps).toBeTruthy();
	});

	it ('mapStateToProps data test', () => {		
		const currentSoftwareProduct = VSPEditorFactory.build();

		const processesList =   VSPProcessStoreFactory.buildList(2);

		var obj = {
			softwareProduct: {
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductProcesses:
				{
					processesList,
					processesEditor: {data: {}}
				}
			}
		};
		var results = mapStateToProps(obj);
		expect(results.processesList).toBeTruthy();
	});

	it ('view simple test', () => {		
		const currentSoftwareProduct = VSPEditorFactory.build();			
		const processesList = VSPProcessStoreFactory.buildList(2);

		const versionControllerData = VSPComponentsVersionControllerFactory.build();
		
		
		var renderer = TestUtils.createRenderer();
		renderer.render(
			<SoftwareProductProcessesView
				processesList={processesList}
				versionControllerData={versionControllerData}
				currentSoftwareProduct={currentSoftwareProduct}				
				onAddProcess={() => {}}
				onEditProcess={() => {}}
				onDeleteProcess={() => {}}
				isDisplayEditor={false}
				isReadOnlyMode={false} />
			);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});
});

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
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/deployment/SoftwareProductDeployment.js';
import SoftwareProductDeploymentView from 'sdc-app/onboarding/softwareProduct/deployment/SoftwareProductDeploymentView.jsx';

import {VSPDeploymentStoreFactory} from 'test-utils/factories/softwareProduct/SoftwareProductDeploymentFactories.js';
import {VSPComponentsFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsFactories.js';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';

describe('SoftwareProductDeployment Mapper and View Classes', () => {
	it ('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it ('mapStateToProps data test', () => {

		const currentSoftwareProduct = VSPEditorFactory.build();

		const deploymentFlavors =   VSPDeploymentStoreFactory.buildList(2);

		const componentsList = VSPComponentsFactory.buildList(1);

		var state = {
			softwareProduct: {
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductDeployment:
				{
					deploymentFlavors,
					deploymentFlavorsEditor: {data: {}}
				},
				softwareProductComponents: {
					componentsList
				}
			}
		};
		var results = mapStateToProps(state);
		expect(results.deploymentFlavors).toBeTruthy();
	});

	it ('view simple test', () => {

		const currentSoftwareProduct = VSPEditorFactory.build();

		const deploymentFlavors = VSPDeploymentStoreFactory.buildList(2);

		var renderer = TestUtils.createRenderer();
		renderer.render(
			<SoftwareProductDeploymentView
				deploymentFlavors={deploymentFlavors}
				currentSoftwareProduct={currentSoftwareProduct}
				onAddDeployment={() => {}}
				onEditDeployment={() => {}}
				onDeleteDeployment={() => {}}
				isReadOnlyMode={false} />
			);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});
});

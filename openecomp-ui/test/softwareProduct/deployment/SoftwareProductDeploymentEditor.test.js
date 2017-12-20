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
import { mapStateToProps }  from 'sdc-app/onboarding/softwareProduct/deployment/editor/SoftwareProductDeploymentEditor.js';
import SoftwareProductDeploymentEditorView from 'sdc-app/onboarding/softwareProduct/deployment/editor/SoftwareProductDeploymentEditorView.jsx';
import { VSPComponentsFactory } from 'test-utils/factories/softwareProduct/SoftwareProductComponentsFactories.js';
import { VSPEditorFactoryWithLicensingData } from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import { FeatureGroupStoreFactory } from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';
import CurrentScreenFactory from 'test-utils/factories/common/CurrentScreenFactory.js';

describe('Software Product Deployment Editor Module Tests', function () {

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('should return empty data', () => {

		const currentSoftwareProduct = VSPEditorFactoryWithLicensingData.build();
		const componentsList = VSPComponentsFactory.buildList(1);
		const featureGroupsList = FeatureGroupStoreFactory.buildList(2);
		const currentScreen = CurrentScreenFactory.build();

		var state = {
			currentScreen,
			softwareProduct: {
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductDeployment:
				{
					deploymentFlavors: [],
					deploymentFlavorEditor: {data: {}}
				},
				softwareProductComponents: {
					componentsList,
					computeFlavor: {
						computesList: []
					}
				}
			},
			licenseModel: {
				featureGroup: {
					featureGroupsList
				}
			}
		};

		var results = mapStateToProps(state);
		expect(results.data).toEqual({});
	});

	it('jsx view test', () => {
		const componentsList = VSPComponentsFactory.buildList(1);
		var renderer = TestUtils.createRenderer();
		renderer.render(
			<SoftwareProductDeploymentEditorView
				isReadOnlyMode={true}
				selectedFeatureGroupsList={[]}
				componentsList={componentsList}
				data={{}}
				onDataChanged={() => {}}
				onSubmit={() => {}}
				onClose={() => {}}/>
			);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

});

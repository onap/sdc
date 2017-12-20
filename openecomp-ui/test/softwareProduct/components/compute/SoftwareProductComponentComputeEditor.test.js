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
import {mapStateToProps as computeEditorMapStateToProps} from 'sdc-app/onboarding/softwareProduct/components/compute/computeComponents/computeFlavor/ComputeFlavorEditor.js';
import ComputeEditorView from 'sdc-app/onboarding/softwareProduct/components/compute/computeComponents/computeFlavor/ComputeFlavorEditorView.jsx';

import {SoftwareProductFactory} from 'test-utils/factories/softwareProduct/SoftwareProductFactory.js';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {ComputeFlavorBaseData, ComputeFlavorQData, VSPComponentsComputeDataMapFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsComputeFactory.js';
import CurrentScreenFactory from 'test-utils/factories/common/CurrentScreenFactory.js';

describe('Software Product Component Compute-Editor Mapper and View Classes.', () => {

	it('Compute Editor - mapStateToProps mapper exists', () => {
		expect(computeEditorMapStateToProps).toBeTruthy();
	});

	it('Compute Editor - mapStateToProps data test', () => {
		const currentSoftwareProduct = VSPEditorFactory.build();

		var obj = {
			currentScreen: CurrentScreenFactory.build(),
			softwareProduct: SoftwareProductFactory.build({
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductComponents: {
					computeFlavor: {
						computeEditor: {
							data: {},
							qdata: {},
							qgenericFieldInfo: {},
							dataMap: {},
							genericFieldInfo: {},
							formReady: true
						}
					}
				}
			})
		};

		var results = computeEditorMapStateToProps(obj);
		expect(results.data).toBeTruthy();
		expect(results.qdata).toBeTruthy();
		expect(results.qgenericFieldInfo).toBeTruthy();
		expect(results.dataMap).toBeTruthy();
		expect(results.genericFieldInfo).toBeTruthy();
		expect(results.isReadOnlyMode).toBe(false);
		expect(results.isFormValid).toBeTruthy();
		expect(results.formReady).toBeTruthy();
	});

	it('Compute Editor - View Test', () => {

		const props = {
			data: ComputeFlavorBaseData.build(),
			qdata: ComputeFlavorQData.build(),
			dataMap: VSPComponentsComputeDataMapFactory.build(),
			isReadOnlyMode: false,
			onDataChanged: () => {},
			onQDataChanged: () => {},
			onSubmit: () => {},
			onCancel: () => {}
		};

		var renderer = TestUtils.createRenderer();
		renderer.render(<ComputeEditorView {...props}/>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();

	});
});

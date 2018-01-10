/*
 * Copyright © 2016-2017 European Support Limited
 *
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
 */

import React from 'react';
import ShallowRenderer from 'react-test-renderer/shallow';
import {mapStateToProps} from 'sdc-app/onboarding/softwareProduct/components/processes/SoftwareProductComponentProcessesList.js';
import SoftwareProductComponentsProcessesView from 'sdc-app/onboarding/softwareProduct/components/processes/SoftwareProductComponentsProcessesListView.jsx';

import {VSPProcessStoreFactory} from 'test-utils/factories/softwareProduct/SoftwareProductProcessFactories.js';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {VSPComponentsFactory} from 'test-utils/factories/softwareProduct/SoftwareProductComponentsFactories.js';

describe('SoftwareProductComponetsProcesses Mapper and View Classes', () => {
	it('mapStateToProps mapper exists', () => {		
		expect(mapStateToProps).toBeTruthy();
	});

	it('mapStateToProps data test', () => {		
		const currentSoftwareProduct = VSPEditorFactory.build();

		const processesList = VSPProcessStoreFactory.buildList(2);

		var state = {
			softwareProduct: {
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductComponents: {
					componentProcesses: {
						processesList,
						processesEditor: {
							data: {}
						}
					}
				}
			}
		};
		var results = mapStateToProps(state);
		expect(results.processesList.length).toBe(2);
	});

	it('view simple test', () => {		
		const currentSoftwareProduct = VSPEditorFactory.build();
		const currentSoftwareProductComponent = VSPComponentsFactory.build();
		const processesList = VSPProcessStoreFactory.buildList(2);

		const renderer = new ShallowRenderer();
		renderer.render(
			<SoftwareProductComponentsProcessesView
				processesList={processesList}
				currentSoftwareProduct={currentSoftwareProduct}
				softwareProductId={currentSoftwareProduct.id}
				componentId={currentSoftwareProductComponent.id}
				onAddProcess={() => {}}
				onEditProcess={() => {}}
				onDeleteProcess={() => {}}
				isDisplayEditor={false}
				isReadOnlyMode={false}/>
			);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});
});

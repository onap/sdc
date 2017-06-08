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
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/processes/SoftwareProductProcessesEditor.js';
import SoftwareProductProcessesEditorView from 'sdc-app/onboarding/softwareProduct/processes/SoftwareProductProcessesEditorView.jsx';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';

describe('Software Product  Processes Editor Module Tests', function () {

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('should return empty data', () => {

		const currentSoftwareProduct = VSPEditorFactory.build();

		var state = {
			softwareProduct: {
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductProcesses:
				{
					processesList: [],
					processesEditor: {data: {}}
				}
			}
		};

		var results = mapStateToProps(state);
		expect(results.data).toEqual({});
		expect(results.previousData).toEqual(undefined);
	});

	it('jsx view test', () => {
		var view = TestUtils.renderIntoDocument(<SoftwareProductProcessesEditorView
			isReadOnlyMode={true}
			data={{}}
			previousData={{}}
			onDataChanged={() => {}}
			onSubmit={() => {}}
			onClose={() => {}}/>);
		expect(view).toBeTruthy();
	});

});

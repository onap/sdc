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
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/components/processes/SoftwareProductComponentProcessesEditor.js';
import SoftwareProductComponentProcessesEditorView from 'sdc-app/onboarding/softwareProduct/components/processes/SoftwareProductComponentProcessesEditorView.jsx';

describe('Software Product Components  Processes Editor Module Tests', function () {

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('should return empty data', () => {

		var state = {
			softwareProduct: {
				softwareProductComponents: {
					componentProcesses: {
						processesList: [],
						processesEditor: {data: {}}
					}
				}
			}
		};

		var results = mapStateToProps(state);
		expect(results.data).toEqual({});
		expect(results.previousData).toEqual(undefined);
	});

	it('jsx view test', () => {
		var view = TestUtils.renderIntoDocument(
			<SoftwareProductComponentProcessesEditorView
				isReadOnlyMode={true}
				data={{name: '1', description: '1', artifactName: '1'}}
				previousData={{}}
				onDataChanged={() => {}}
				onSubmit={() => {}}
				onClose={() => {}}/>);
		expect(view).toBeTruthy();
	});
});

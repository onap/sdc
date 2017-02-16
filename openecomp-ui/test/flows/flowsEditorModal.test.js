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
import {mapStateToProps} from 'sdc-app/flows/FlowsEditorModal.js';
import FlowsEditorModalView from 'sdc-app/flows/FlowsEditorModalView.jsx';

describe('Flows Editor Modal Mapper and View Classes: ', function () {

	it('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toExist();
	});

	it('mapStateToProps mapper - without currentFlow', () => {
		var flows = {
			serviceID: '123',
			diagramType: 'SOME_TYPE'
		};
		var results = mapStateToProps({flows});
		expect(results.currentFlow).toExist();
		expect(results.currentFlow.artifactName).toBe('');
		expect(results.currentFlow.description).toBe('');
	});

	it('mapStateToProps mapper - populated currentFlow', () => {
		let artifactName = 'test1', description = 'desc';
		var flows = {
			currentFlow: {artifactName, description},
			serviceID: '123',
			diagramType: 'SOME_TYPE'
		};
		var results = mapStateToProps({flows});
		expect(results.currentFlow).toExist();
		expect(results.currentFlow.artifactName).toBe(artifactName);
		expect(results.currentFlow.description).toBe(description);
		expect(results.currentFlow.serviceID).toBe(flows.serviceID);
		expect(results.currentFlow.artifactType).toBe(flows.diagramType);
	});

	it('basic modal view component run with empty artifact', () => {
		let renderer = TestUtils.createRenderer();
		renderer.render(
			<FlowsEditorModalView
				onCancel={()=>{}}
				onDataChanged={()=>{}}
				currentFlow={{artifactName: '', description: ''}}/>);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();
	});

	it('modal view component run with data changed handler', done => {
		let handler = () => done();
		let document = TestUtils.renderIntoDocument(
			<FlowsEditorModalView
				onCancel={()=>{}}
				onDataChanged={handler}
				currentFlow={{artifactName: '', description: ''}}/>);
		let result = TestUtils.scryRenderedDOMComponentsWithTag(document, 'input');
		expect(result).toExist();
		expect(result.length).toExist();
		TestUtils.Simulate.change(result[0]);
	});

	it('modal view component - on save click', done => {
		let handler = () => done();
		var flowsEditorModalView = new FlowsEditorModalView({currentFlow: {}, onSubmit: handler});
		flowsEditorModalView.onSaveClicked();
	});

});

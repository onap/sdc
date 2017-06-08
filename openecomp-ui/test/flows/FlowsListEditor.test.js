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
import {mapStateToProps} from 'sdc-app/flows/FlowsListEditor.js';
import FlowsListEditorView from 'sdc-app/flows/FlowsListEditorView.jsx';

import {FlowUpdateRequestFactory, FlowBasicFactory} from 'test-utils/factories/flows/FlowsFactories.js';

describe('Flows List Editor Mapper and View Classes: ', function () {

	it('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('mapStateToProps mapper - without flowList', () => {
		let flows = {
			isDisplayModal: true,
			isModalInEditMode: false,
			shouldShowWorkflowsEditor: undefined
		};
		let results = mapStateToProps({flows});
		expect(results.flowList).toBeTruthy();
		expect(results.flowList.length).toEqual(0);
		expect(results.shouldShowWorkflowsEditor).toBe(true);
	});

	it('mapStateToProps mapper - populated flowList', () => {
		let flows = {
			flowList: FlowBasicFactory.buildList(1),
			isDisplayModal: true,
			isModalInEditMode: false,
			shouldShowWorkflowsEditor: false
		};
		let results = mapStateToProps({flows});
		expect(results.flowList).toBeTruthy();
		expect(results.flowList.length).toEqual(1);
		expect(results.shouldShowWorkflowsEditor).toBe(false);
	});

	it('mapStateToProps mapper - populated flowList and currentFlow is in readonly', () => {
		let currentFlow = FlowBasicFactory.build();
		currentFlow.readonly = true;
		let flows = {
			flowList: [currentFlow],
			data: currentFlow,
			isDisplayModal: true,
			isModalInEditMode: false,
			shouldShowWorkflowsEditor: false
		};
		let results = mapStateToProps({flows});
		expect(results.currentFlow).toBeTruthy();
		expect(results.isCheckedOut).toEqual(false);
	});

	it('mapStateToProps mapper - populated flowList and currentFlow is in not readonly', () => {
		let currentFlow = FlowBasicFactory.build();
		currentFlow.readonly = false;
		let flows = {
			flowList: [currentFlow],
			data: currentFlow,
			isDisplayModal: true,
			isModalInEditMode: false,
			shouldShowWorkflowsEditor: false
		};
		let results = mapStateToProps({flows});
		expect(results.currentFlow).toBeTruthy();
		expect(results.isCheckedOut).toEqual(true);
	});

	it('mapStateToProps mapper - populated flowList and service is in readonly', () => {
		let currentFlow = FlowBasicFactory.build();
		let flows = {
			flowList: [currentFlow],
			data: currentFlow,
			isDisplayModal: true,
			isModalInEditMode: false,
			shouldShowWorkflowsEditor: false,
			readonly: true
		};
		let results = mapStateToProps({flows});
		expect(results.currentFlow).toBeTruthy();
		expect(results.isCheckedOut).toEqual(false);
	});

	it('mapStateToProps mapper - populated flowList and service is in not readonly', () => {
		let currentFlow = FlowBasicFactory.build();
		let flows = {
			flowList: [currentFlow],
			data: currentFlow,
			isDisplayModal: true,
			isModalInEditMode: false,
			shouldShowWorkflowsEditor: false,
			readonly: false
		};
		let results = mapStateToProps({flows});
		expect(results.currentFlow).toBeTruthy();
		expect(results.isCheckedOut).toEqual(true);
	});

	it('basic view component run with empty flowList and should show the list', () => {
		let renderer = TestUtils.createRenderer();
		let currentFlow = FlowBasicFactory.build();
		renderer.render(<FlowsListEditorView shouldShowWorkflowsEditor={true} flowList={[currentFlow]}/>);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	it('basic view component run with empty flowList and should show the diagram', () => {
		const flow = FlowUpdateRequestFactory.build();
		let renderer = TestUtils.createRenderer();
		renderer.render(<FlowsListEditorView currentFlow={flow} shouldShowWorkflowsEditor={false} flowList={[flow]}/>);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	it('basic view component run with empty flowList and should show popup modal', () => {
		let renderer = TestUtils.createRenderer();
		let currentFlow = FlowBasicFactory.build();
		renderer.render(<FlowsListEditorView isDisplayModal={true} shouldShowWorkflowsEditor={true} flowList={[currentFlow]}/>);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});


});

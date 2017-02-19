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
import {mapStateToProps} from 'sdc-app/flows/FlowsListEditor.js';
import FlowsListEditorView from 'sdc-app/flows/FlowsListEditorView.jsx';

describe('Flows List Editor Mapper and View Classes: ', function () {

	it('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toExist();
	});

	it('mapStateToProps mapper - without flowList', () => {
		let flows = {
			isDisplayModal: true,
			isModalInEditMode: false,
			shouldShowWorkflowsEditor: undefined
		};
		let results = mapStateToProps({flows});
		expect(results.flowList).toExist();
		expect(results.flowList.length).toEqual(0);
		expect(results.shouldShowWorkflowsEditor).toBe(true);
	});

	it('mapStateToProps mapper - populated flowList', () => {
		let artifactName = 'test1', description = 'desc';
		let flows = {
			flowList: [{artifactName, description}],
			isDisplayModal: true,
			isModalInEditMode: false,
			shouldShowWorkflowsEditor: false
		};
		let results = mapStateToProps({flows});
		expect(results.flowList).toExist();
		expect(results.flowList.length).toEqual(1);
		expect(results.shouldShowWorkflowsEditor).toBe(false);
	});

	it('mapStateToProps mapper - populated flowList and currentFlow is in readonly', () => {
		let artifactName = 'test1', description = 'desc';
		let currentFlow = {artifactName, description, readonly: true};
		let flows = {
			flowList: [currentFlow],
			currentFlow,
			isDisplayModal: true,
			isModalInEditMode: false,
			shouldShowWorkflowsEditor: false
		};
		let results = mapStateToProps({flows});
		expect(results.currentFlow).toExist();
		expect(results.isCheckedOut).toEqual(false);
	});

	it('mapStateToProps mapper - populated flowList and currentFlow is in not readonly', () => {
		let artifactName = 'test1', description = 'desc';
		let currentFlow = {artifactName, description, readonly: false};
		let flows = {
			flowList: [currentFlow],
			currentFlow,
			isDisplayModal: true,
			isModalInEditMode: false,
			shouldShowWorkflowsEditor: false
		};
		let results = mapStateToProps({flows});
		expect(results.currentFlow).toExist();
		expect(results.isCheckedOut).toEqual(true);
	});

	it('basic view component run with empty flowList and should show the list', () => {
		let renderer = TestUtils.createRenderer();
		let artifactName = 'test1', description = 'desc';
		let currentFlow = {artifactName, description, readonly: false};
		renderer.render(<FlowsListEditorView shouldShowWorkflowsEditor={true} flowList={[currentFlow]}/>);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();
	});

	it('basic view component run with empty flowList and should show the diagram', () => {
		const flow = {
			'artifactType': 'WORKFLOW',
			'participants': [
				{
					'id': '1',
					'name': 'Customer'
				},
				{
					'id': '2',
					'name': 'CCD'
				},
				{
					'id': '3',
					'name': 'Infrastructure'
				},
				{
					'id': '4',
					'name': 'MSO'
				},
				{
					'id': '5',
					'name': 'SDN-C'
				},
				{
					'id': '6',
					'name': 'A&AI'
				},
				{
					'id': '7',
					'name': 'APP-C'
				},
				{
					'id': '8',
					'name': 'Cloud'
				},
				{
					'id': '9',
					'name': 'DCAE'
				},
				{
					'id': '10',
					'name': 'ALTS'
				},
				{
					'id': '11',
					'name': 'VF'
				}
			],
			'serviceID': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b',
			'artifactDisplayName': 'zizizi',
			'artifactGroupType': 'INFORMATIONAL',
			'uniqueId': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
			'artifactName': 'zizizi',
			'artifactLabel': 'zizizi',
			'artifactUUID': '0295a7cc-8c02-4105-9d7e-c30ce67ecd07',
			'artifactVersion': '1',
			'creationDate': 1470144601623,
			'lastUpdateDate': 1470144601623,
			'description': 'aslkjdfl asfdasdf',
			'mandatory': false,
			'timeout': 0,
			'esId': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
			'artifactChecksum': 'NjBmYjc4NGM5MWIwNmNkMDhmMThhMDAwYmQxYjBiZTU=',
			'heatParameters': [],
			'sequenceDiagramModel': {
				'diagram': {
					'metadata': {
						'id': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
						'name': 'zizizi',
						'ref': 'BLANK'
					},
					'lifelines': [
						{
							'id': '1',
							'name': 'Customer',
							'index': 1,
							'x': 175
						},
						{
							'id': '2',
							'name': 'CCD',
							'index': 2,
							'x': 575
						},
						{
							'id': '3',
							'name': 'Infrastructure',
							'index': 3,
							'x': 975
						},
						{
							'id': '4',
							'name': 'MSO',
							'index': 4,
							'x': 1375
						},
						{
							'id': '5',
							'name': 'SDN-C',
							'index': 5,
							'x': 1775
						},
						{
							'id': '6',
							'name': 'A&AI',
							'index': 6,
							'x': 2175
						},
						{
							'id': '7',
							'name': 'APP-C',
							'index': 7,
							'x': 2575
						},
						{
							'id': '8',
							'name': 'Cloud',
							'index': 8,
							'x': 2975
						},
						{
							'id': '9',
							'name': 'DCAE',
							'index': 9,
							'x': 3375
						},
						{
							'id': '10',
							'name': 'ALTS',
							'index': 10,
							'x': 3775
						},
						{
							'id': '11',
							'name': 'VF',
							'index': 11,
							'x': 4175
						}
					],
					'steps': [
						{
							'message': {
								'id': '9377-5036-c011-cb95-3a8b-82c6-bbb5-bc84',
								'name': '[Unnamed Message]',
								'type': 'request',
								'from': '1',
								'to': '2',
								'index': 1
							}
						},
						{
							'message': {
								'id': '64c4-4fd1-b1da-4355-a060-6e48-ee47-c85c',
								'name': '[Unnamed Message]',
								'type': 'request',
								'from': '1',
								'to': '2',
								'index': 2
							}
						}
					]
				}
			}
		};
		let renderer = TestUtils.createRenderer();
		renderer.render(<FlowsListEditorView currentFlow={flow} shouldShowWorkflowsEditor={false} flowList={[flow]}/>);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();
	});

	it('basic view component run with empty flowList and should show popup modal', () => {
		let renderer = TestUtils.createRenderer();
		let artifactName = 'test1', description = 'desc';
		let currentFlow = {artifactName, description, readonly: false};
		renderer.render(<FlowsListEditorView isDisplayModal={true} shouldShowWorkflowsEditor={true} flowList={[currentFlow]}/>);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();
	});


});

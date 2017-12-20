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
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/attachments/validation/HeatValidation.js';

import HeatValidationView from 'sdc-app/onboarding/softwareProduct/attachments/validation/HeatValidationView.jsx';

describe('SoftwareProductAttachments Modal Mapper and View Classes', () => {

	it ('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});


	it ('mapStateToProps check data', () => {

		const currentSoftwareProduct = {
			name: 'VSp',
			description: 'dfdf',
			vendorName: 'V1',
			vendorId: '97B3E2525E0640ACACF87CE6B3753E80',
			category: 'resourceNewCategory.application l4+',
			subCategory: 'resourceNewCategory.application l4+.database',
			id: 'D4774719D085414E9D5642D1ACD59D20',
			version: '0.10',
			viewableVersions: ['0.1', '0.2'],
			lockingUser: 'cs0008'
		};
		const atTree = {
			'children': [
				{
					'name': 'HEAT',
					'expanded': true,
					'type': 'heat',
					'children': [
						{
							'name': 'heat_zxeyCtMHhf2.yaml',
							'expanded': true,
							'type': 'heat',
							'errors': [
								{
									'level': 'WARNING',
									'message': 'Resource is not defined as output and thus cannot be Shared. resource id - network_4'
								}
							],
							'children': [
								{
									'name': 'heat_env_zxeyCtMHhf2.env',
									'type': 'env'
								}
							]
						}
					]
				}
			]
		};
		const errorList = [
			{
				'errorLevel': 'WARNING',
				'errorMessage': 'Resource is not defined as output and thus cannot be Shared. resource id - network_4',
				'name': 'heat_zxeyCtMHhf2.yaml',
				'hasParent': false,
				'parentName': 'HEAT',
				'type': 'heat'
			},
			{
				'errorLevel': 'WARNING',
				'errorMessage': 'Resource is not defined as output and thus cannot be Shared. resource id - network_3',
				'name': 'heat_zxeyCtMHhf2.yaml',
				'hasParent': false,
				'parentName': 'HEAT',
				'type': 'heat'
			}
		];

		var obj = {
			softwareProduct: {
				softwareProductEditor: {
					data:currentSoftwareProduct
				}, softwareProductAttachments:
				{
					heatValidation: {
						attachmentsTree: atTree,
						errorList: errorList
					}
				}
			}
		};

		var results = mapStateToProps(obj);
		expect(results.attachmentsTree).toBeTruthy();
		expect(results.errorList).toBeTruthy();
		expect(results.currentErrors).toBeTruthy();
		expect(results.currentWarnings).toBeTruthy();
		expect(results.selectedNode).toBe(undefined);
	});


	it('function does exist', () => {

		const atTree = {
			'children': [
				{
					'name': 'HEAT',
					'expanded': true,
					'type': 'heat',
					'children': [
						{
							'name': 'heat_zxeyCtMHhf2.yaml',
							'expanded': true,
							'type': 'heat',
							'errors': [
								{
									'level': 'WARNING',
									'message': 'Resource is not defined as output and thus cannot be Shared. resource id - network_4'
								}
							],
							'children': [
								{
									'name': 'heat_env_zxeyCtMHhf2.env',
									'type': 'env'
								}
							]
						}
					]
				}
			]
		};
		const errorList = [
			{
				'errorLevel': 'WARNING',
				'errorMessage': 'Resource is not defined as output and thus cannot be Shared. resource id - network_4',
				'name': 'heat_zxeyCtMHhf2.yaml',
				'hasParent': false,
				'parentName': 'HEAT',
				'type': 'heat'
			},
			{
				'errorLevel': 'WARNING',
				'errorMessage': 'Resource is not defined as output and thus cannot be Shared. resource id - network_3',
				'name': 'heat_zxeyCtMHhf2.yaml',
				'hasParent': false,
				'parentName': 'HEAT',
				'type': 'heat'
			}
		];
		var currentErrors = [];
		var currentWarnings = [];
		var onSelect = () => { return null; } ;
		var onDeSelect = () => { return null; } ;
		var onToggle = () => { return null; } ;

		var renderer = TestUtils.createRenderer();
		renderer.render(<HeatValidationView
			attachmentsTree={atTree}
			errorList={errorList}
			currentWarnings={currentWarnings}
			currentErrors={currentErrors}
			onSelectNode={onSelect}
			onDeselectNode={onDeSelect}
			toggleExpanded = {onToggle} />);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

});

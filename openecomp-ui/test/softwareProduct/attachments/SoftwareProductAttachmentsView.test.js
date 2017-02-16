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
import {mapStateToProps}  from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachments.js';

import SoftwareProductAttachmentsView from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachmentsView.jsx';
import {statusEnum as versionStatusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';


describe('SoftwareProductAttachments Modal Mapper and View Classes', () => {

	it ('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toExist();
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
			status: versionStatusEnum.CHECK_OUT_STATUS,
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
					attachmentsTree: atTree,
					errorList: errorList
				}
			}
		};

		var results = mapStateToProps(obj);
		expect(results.attachmentsTree).toExist();
		expect(results.errorList).toExist();
		expect(results.hoveredNode).toBe(undefined);
		expect(results.selectedNode).toBe(undefined);
	});


	it('function does exist', () => {

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
			status: versionStatusEnum.CHECK_OUT_STATUS,
			lockingUser: 'cs0008'
		};
		const versionControllerData = {
			version: currentSoftwareProduct.version,
			viewableVersions:currentSoftwareProduct.viewableVersions,
			status: currentSoftwareProduct.status,
			isCheckedOut: true
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


		var renderer = TestUtils.createRenderer();
		renderer.render(<SoftwareProductAttachmentsView
			versionControllerData={versionControllerData}
			currentSoftwareProduct={currentSoftwareProduct}
			attachmentsTree={atTree}
			errorList={errorList}/>);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toExist();
	});

});

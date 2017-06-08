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

import HeatValidationActionHelper from 'sdc-app/onboarding/softwareProduct/attachments/validation/HeatValidationActionHelper.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import deepFreeze from 'deep-freeze';
import {actionTypes} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import {nodeFilters} from 'sdc-app/onboarding/softwareProduct/attachments/validation/HeatValidationConstants.js';

describe('HeatValidationActionHelper ActionHelper', () => {

	it('function does exist', () => {
		expect(HeatValidationActionHelper).toBeTruthy();
	});

	it('toggleExpanded function check', () => {


		const validationData = {
			importStructure: {
				HEAT: [
					{
						fileName: 'hot-mog-0108-bs1271.yml',
						env: {
							fileName: 'hot-mog-0108-bs1271.env'
						},
						errors: [
							{
								'level': 'WARNING',
								'message': 'Port not bind to any NOVA Server, Resource Id [sm02_port_2]'
							},
							{
								'level': 'WARNING',
								'message': 'Port not bind to any NOVA Server, Resource Id [sm01_port_2]'
							}
						]
					}
				]
			}
		};

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
			status: 'Locked',
			lockingUser: 'cs0008',
			validationData
		};


		const store = storeCreator();
		deepFreeze(store.getState());
		deepFreeze(currentSoftwareProduct);

		store.dispatch({
			type:actionTypes.SOFTWARE_PRODUCT_LOADED,
			response: currentSoftwareProduct
		});

		expect(store.getState().softwareProduct.softwareProductAttachments.heatValidation.attachmentsTree.children[0].expanded).toBe(true);
		HeatValidationActionHelper.toggleExpanded(store.dispatch, {path:[0]});
		expect(store.getState().softwareProduct.softwareProductAttachments.heatValidation.attachmentsTree.children[0].expanded).toBe(false);
	});

	it('onSelectNode & onUnselectNode function check', () => {


		const validationData = {
			importStructure: {
				heat: [
					{
						fileName: 'hot-mog-0108-bs1271.yml',
						env: {
							fileName: 'hot-mog-0108-bs1271.env'
						},
						errors: [
							{
								'level': 'WARNING',
								'message': 'Port not bind to any NOVA Server, Resource Id [sm02_port_2]'
							},
							{
								'level': 'WARNING',
								'message': 'Port not bind to any NOVA Server, Resource Id [sm01_port_2]'
							}
						]
					}
				]
			}
		};

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
			status: 'Locked',
			lockingUser: 'cs0008',
			validationData
		};

		deepFreeze(currentSoftwareProduct);

		const store = storeCreator();
		deepFreeze(store.getState());

		store.dispatch({
			type:actionTypes.SOFTWARE_PRODUCT_LOADED,
			response: currentSoftwareProduct
		});
		let expectedNodeName = 'name';
		expect(store.getState().softwareProduct.softwareProductAttachments.heatValidation.selectedNode).toBe(nodeFilters.ALL);
		HeatValidationActionHelper.onSelectNode(store.dispatch, {nodeName:expectedNodeName});
		expect(store.getState().softwareProduct.softwareProductAttachments.heatValidation.selectedNode).toBe(expectedNodeName);
		HeatValidationActionHelper.onDeselectNode(store.dispatch);
		expect(store.getState().softwareProduct.softwareProductAttachments.heatValidation.selectedNode).toBe(nodeFilters.ALL);
	});


});

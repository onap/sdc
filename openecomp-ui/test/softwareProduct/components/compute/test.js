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
import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import {cloneAndSet} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import Configuration from 'sdc-app/config/Configuration.js';
import SoftwareProductComponentsActionHelper from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsActionHelper.js';

const softwareProductId = '123';
const vspComponentId = '111';

describe('Software Product Components Compute Module Tests', function () {

	let restPrefix = '';

	before(function() {
		restPrefix = Configuration.get('restPrefix');
		deepFreeze(restPrefix);
	});

	it('Get Software Products Components Compute', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductComponentCompute = {
			data: JSON.stringify({'vmSizing':{'numOfCPUs':'3','fileSystemSizeGB':'888'},'numOfVMs':{'minimum':'2'}}),
			schema: JSON.stringify({'vmSizing':{'numOfCPUs':'3','fileSystemSizeGB':'888'},'numOfVMs':{'minimum':'2'}})
		};
		deepFreeze(softwareProductComponentCompute);

		const softwareProductComponentComputeData = {
			qdata: JSON.parse(softwareProductComponentCompute.data),
			qschema: JSON.parse(softwareProductComponentCompute.schema)
		};
		deepFreeze(softwareProductComponentComputeData);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentEditor', softwareProductComponentComputeData);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/components/${vspComponentId}/questionnaire`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return softwareProductComponentCompute;
		});

		return SoftwareProductComponentsActionHelper.fetchSoftwareProductComponentQuestionnaire(store.dispatch, {softwareProductId, vspComponentId}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Get Empty Software Products Components Compute', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductComponentQuestionnaire = {
			data: null,
			schema: JSON.stringify({'vmSizing':{'numOfCPUs':'3','fileSystemSizeGB':'888'},'numOfVMs':{'minimum':'2'}})
		};
		deepFreeze(softwareProductComponentQuestionnaire);

		const softwareProductComponentQuestionnaireData = {
			qdata: {},
			qschema: JSON.parse(softwareProductComponentQuestionnaire.schema)
		};
		deepFreeze(softwareProductComponentQuestionnaireData);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentEditor', softwareProductComponentQuestionnaireData);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/components/${vspComponentId}/questionnaire`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return softwareProductComponentQuestionnaire;
		});

		return SoftwareProductComponentsActionHelper.fetchSoftwareProductComponentQuestionnaire(store.dispatch, {softwareProductId, vspComponentId}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Update Software Products Components Compute', () => {
		const store = storeCreator({
			softwareProduct: {
				softwareProductComponents: {
					componentEditor: {
						qdata: {
							numOfCPUs: 3,
							fileSystemSizeGB: 999
						},
						qschema: {
							type: 'object',
							properties: {
								numOfCPUs: {type: 'number'},
								fileSystemSizeGB: {type: 'number'}
							}
						}
					}
				}
			}
		});
		deepFreeze(store);

		const data = {numOfCPUs: 5, fileSystemSizeGB: 300};
		deepFreeze(data);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentEditor.qdata', data);

		SoftwareProductComponentsActionHelper.componentQuestionnaireUpdated(store.dispatch, {data});

		expect(store.getState()).toEqual(expectedStore);
	});
});

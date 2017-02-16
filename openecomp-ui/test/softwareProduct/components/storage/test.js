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

describe('Software Product Components Storage Module Tests', function () {

	let restPrefix = '';

	before(function() {
		restPrefix = Configuration.get('restPrefix');
		deepFreeze(restPrefix);
	});

	it('Get Software Products Components Storage', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductComponentStorage = {
			data: JSON.stringify({'backup':{'backupType':'OnSite','backupSolution':'76333'},'snapshotBackup':{'snapshotFrequency':'2'}}),
			schema: JSON.stringify({'backup':{'backupType':'OnSite','backupSolution':'76333'},'snapshotBackup':{'snapshotFrequency':'2'}})
		};
		deepFreeze(softwareProductComponentStorage);

		const softwareProductComponentStorageData = {
			qdata: JSON.parse(softwareProductComponentStorage.data),
			qschema: JSON.parse(softwareProductComponentStorage.schema)
		};
		deepFreeze(softwareProductComponentStorageData);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentEditor', softwareProductComponentStorageData);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/components/${vspComponentId}/questionnaire`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return softwareProductComponentStorage;
		});

		return SoftwareProductComponentsActionHelper.fetchSoftwareProductComponentQuestionnaire(store.dispatch, {softwareProductId, vspComponentId}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Get Empty Software Products Components Storage', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductComponentQuestionnaire = {
			data: null,
			schema: JSON.stringify({'backup':{'backupType':'OnSite','backupSolution':'76333'},'snapshotBackup':{'snapshotFrequency':'2'}})
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

	it('Update Software Products Components Storage', () => {
		const store = storeCreator({
			softwareProduct: {
				softwareProductComponents: {
					componentEditor: {
						qdata: {
							backupType: 'OnSite',
							backupStorageSize: 30
						},
						qschema: {
							type: 'object',
							properties: {
								backupType: {type: 'string'},
								backupStorageSize: {type: 'number'}
							}
						}
					}
				}
			}
		});
		deepFreeze(store);

		const data = {backupType: 'OffSite', backupStorageSize: 30};
		deepFreeze(data);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductComponents.componentEditor.qdata', data);

		SoftwareProductComponentsActionHelper.componentQuestionnaireUpdated(store.dispatch, {data});

		expect(store.getState()).toEqual(expectedStore);
	});
});

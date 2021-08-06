/*
 * -
 *  ============LICENSE_START=======================================================
 *  Copyright © 2016-2018 European Support Limited
 *  Modifications Copyright (C) 2021 Nordix Foundation.
 *  ================================================================================
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  SPDX-License-Identifier: Apache-2.0
 *  ============LICENSE_END=========================================================
 */

import React from 'react';
import ShallowRenderer from 'react-test-renderer/shallow';
import {mapStateToProps, mapActionsToProps} from 'sdc-app/onboarding/softwareProduct/creation/SoftwareProductCreation.js';
import SoftwareProductCreationView from 'sdc-app/onboarding/softwareProduct/creation/SoftwareProductCreationView.jsx';
import {SoftwareProductCreationFactory, SoftwareProductCreationFactoryWithSelectedVendor} from 'test-utils/factories/softwareProduct/SoftwareProductCreationFactories.js';
import {CategoryWithSubFactory} from 'test-utils/factories/softwareProduct/VSPCategoriesFactory.js';
import {FinalizedLicenseModelFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';
import {storeCreator} from 'sdc-app/AppStore.js';

describe('Software Product Creation Module Tests', function() {
	it ('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it ('should return empty data', () => {
		let state = {
			softwareProductList: [],
			finalizedSoftwareProductList: [],
			archivedSoftwareProductList: [],
			softwareProduct: {
				softwareProductCreation: {
					data: {}
				}
			},
			users: {
				usersList: []
			}
		};
		let props = mapStateToProps(state);
		expect(props.data).toEqual({});
	});

	it ('should return vsp names list', () => {
		let state = {
			finalizedSoftwareProductList: [],
			archivedSoftwareProductList: [],
			softwareProductList: [{
				name: 'vsp1',
				id: 'vsp1_id'
			}, {
				name: 'vsp2',
				id: 'vsp2_id'
			}],
			softwareProduct: {
				softwareProductCreation: {
					data: {}
				}
			},
			users: {
				usersList: []
			}
		};
		let props = mapStateToProps(state);
		expect(props.data).toEqual({});
		expect(props.VSPNames).toEqual({vsp1: 'vsp1_id', vsp2: 'vsp2_id'});
	});

	it ('should return model list', () => {
		const modelList = ["model1", "model2", "model3"];
		let state = {
			finalizedSoftwareProductList: [],
			archivedSoftwareProductList: [],
			softwareProductList: [],
			softwareProduct: {
				modelList: modelList,
				softwareProductCreation: {}
			},
			users: {}
		};
		let props = mapStateToProps(state);
		expect(props.modelList).toEqual(modelList);
	});

	it('simple jsx test', () => {
		const store = storeCreator();
		let dispatch = store.dispatch;

		let state = {
			softwareProductList: [],
			finalizedSoftwareProductList: [],
			archivedSoftwareProductList: [],
			softwareProduct: {
				softwareProductCreation: SoftwareProductCreationFactory.build(),
				softwareProductCategories: CategoryWithSubFactory.buildList({}, {quantity: 2})
			},
			finalizedLicenseModelList: FinalizedLicenseModelFactory.buildList(3),
			users: {
				usersList: []
			}
		};
		let props = Object.assign({}, mapStateToProps(state), mapActionsToProps(dispatch));
		const renderer = new ShallowRenderer();
		renderer.render(
			<SoftwareProductCreationView {...props}/>
			);
		var renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	it('simple jsx test - with selected vendor', () => {
		const store = storeCreator();
		let dispatch = store.dispatch;
		let finalizedLicenseModelList = FinalizedLicenseModelFactory.buildList(3);
		let state = {
			softwareProductList: [],
			finalizedSoftwareProductList: [],
			archivedSoftwareProductList: [],
			softwareProduct: {
				softwareProductCreation: SoftwareProductCreationFactoryWithSelectedVendor.build({selectedVendorId: finalizedLicenseModelList[0].id}),
				softwareProductCategories: CategoryWithSubFactory.buildList({}, {quantity: 2})
			},
			finalizedLicenseModelList,
			users: {
				usersList: []
			}
		};
		let props = Object.assign({}, mapStateToProps(state), mapActionsToProps(dispatch));
		const renderer = new ShallowRenderer();
		renderer.render(
			<SoftwareProductCreationView {...props}/>
		);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});
});

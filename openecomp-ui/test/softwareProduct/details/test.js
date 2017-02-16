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

import {expect} from 'chai';
import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import {cloneAndSet} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';

import SoftwareProductCreationActionHelper from 'sdc-app/onboarding/softwareProduct/creation/SoftwareProductCreationActionHelper.js';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import SoftwareProductCategoriesHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductCategoriesHelper.js';

describe('Software Product Module Tests', function () {
	it('Get Software Products List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductList = [
			{
				name: 'VSP1',
				description: 'hjhj',
				version: '0.1',
				id: 'EBADF561B7FA4A788075E1840D0B5971',
				subCategory: 'resourceNewCategory.network connectivity.virtual links',
				category: 'resourceNewCategory.network connectivity',
				vendorId: '5259EDE4CC814DC9897BA6F69E2C971B',
				vendorName: 'Vendor',
				checkinStatus: 'CHECK_OUT',
				licensingData: {
					'featureGroups': []
				}
			},
			{
				name: 'VSP2',
				description: 'dfdfdfd',
				version: '0.1',
				id: '2F47447D22DB4C53B020CA1E66201EF2',
				subCategory: 'resourceNewCategory.network connectivity.virtual links',
				category: 'resourceNewCategory.network connectivity',
				vendorId: '5259EDE4CC814DC9897BA6F69E2C971B',
				vendorName: 'Vendor',
				checkinStatus: 'CHECK_OUT',
				licensingData: {
					featureGroups: []
				}
			}
		];

		deepFreeze(softwareProductList);

		deepFreeze(store.getState());

		const expectedStore = cloneAndSet(store.getState(), 'softwareProductList', softwareProductList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal('/onboarding-api/v1.0/vendor-software-products/');
			expect(data).to.deep.equal(undefined);
			expect(options).to.equal(undefined);
			return {results: softwareProductList};
		});

		return SoftwareProductActionHelper.fetchSoftwareProductList(store.dispatch).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Add Software Product', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductPostRequest = deepFreeze({
			name: 'vsp1',
			description: 'string',
			vendorId: '1',
			vendorName: 'Vendor',
			icon: 'icon',
			subCategory: 'resourceNewCategory.network connectivity.virtual links',
			category: 'resourceNewCategory.network connectivity',
			licensingData: {}
		});

		const softwareProductToAdd = deepFreeze({
			...softwareProductPostRequest
		});

		const softwareProductIdFromResponse = 'ADDED_ID';
		const softwareProductAfterAdd = deepFreeze({
			...softwareProductToAdd,
			id: softwareProductIdFromResponse
		});

		const expectedStore = cloneAndSet(store.getState(), 'softwareProductList', [softwareProductAfterAdd]);

		mockRest.addHandler('create', ({options, data, baseUrl}) => {
			expect(baseUrl).to.equal('/onboarding-api/v1.0/vendor-software-products/');
			expect(data).to.deep.equal(softwareProductPostRequest);
			expect(options).to.equal(undefined);
			return {
				vspId: softwareProductIdFromResponse
			};
		});

		return SoftwareProductCreationActionHelper.createSoftwareProduct(store.dispatch, {
			softwareProduct: softwareProductToAdd
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});
	it('Save Software product', () => {
		const softwareProduct = {
			name: 'VSP5',
			id: '4730033D16C64E3CA556AB0AC4478218',
			description: 'A software model for Fortigate.',
			subCategory: 'resourceNewCategory.network connectivity.virtual links',
			category: 'resourceNewCategory.network connectivity',
			vendorId: '1',
			vendorName: 'Vendor',
			licensingVersion: '1.0',
			icon: 'icon',
			licensingData: {
				licenceAgreement: '123',
				featureGroups: [
					'123', '234'
				]
			}
		};
		deepFreeze(softwareProduct);

		const store = storeCreator({
			softwareProduct: {
				softwareProductEditor: {data: softwareProduct},
				softwareProductQuestionnaire: {qdata: 'test', qschema: {type: 'string'}}
			}
		});
		deepFreeze(store.getState());

		const toBeUpdatedSoftwareProductId = softwareProduct.id;
		const softwareProductUpdateData = {
			...softwareProduct,
			name: 'VSP5_UPDATED',
			description: 'A software model for Fortigate._UPDATED'
		};
		deepFreeze(softwareProductUpdateData);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProductList', [softwareProductUpdateData]);
		const questionnaireData = {
			general: {
				affinityData: {
					affinityGrouping: true,
					antiAffinityGrouping: false
				}
			}
		};
		deepFreeze(questionnaireData);

		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			const expectedData = {
				name: 'VSP5_UPDATED',
				description: 'A software model for Fortigate._UPDATED',
				subCategory: 'resourceNewCategory.network connectivity.virtual links',
				category: 'resourceNewCategory.network connectivity',
				vendorId: '1',
				vendorName: 'Vendor',
				licensingVersion: '1.0',
				icon: 'icon',
				licensingData: {
					licenceAgreement: '123',
					featureGroups: [
						'123', '234'
					]
				}
			};
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${toBeUpdatedSoftwareProductId}`);
			expect(data).to.deep.equal(expectedData);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});
		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${toBeUpdatedSoftwareProductId}/questionnaire`);
			expect(data).to.deep.equal(questionnaireData);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductActionHelper.updateSoftwareProduct(store.dispatch, {
			softwareProduct: softwareProductUpdateData,
			qdata: questionnaireData
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});
	it('Save Software product data only', () => {
		const softwareProduct = {
			name: 'VSP5',
			id: '4730033D16C64E3CA556AB0AC4478218',
			description: 'A software model for Fortigate.',
			subCategory: 'resourceNewCategory.network connectivity.virtual links',
			category: 'resourceNewCategory.network connectivity',
			vendorId: '1',
			vendorName: 'Vendor',
			licensingVersion: '1.0',
			icon: 'icon',
			licensingData: {
				licenceAgreement: '123',
				featureGroups: [
					'123', '234'
				]
			}
		};
		deepFreeze(softwareProduct);

		const store = storeCreator({
			softwareProduct: {
				softwareProductEditor: {data: softwareProduct},
				softwareProductQuestionnaire: {qdata: 'test', qschema: {type: 'string'}}
			}
		});
		deepFreeze(store.getState());
		const expectedStore = store.getState();

		const toBeUpdatedSoftwareProductId = softwareProduct.id;
		const softwareProductUpdateData = {
			...softwareProduct,
			name: 'VSP5_UPDATED',
			description: 'A software model for Fortigate._UPDATED'
		};
		deepFreeze(softwareProductUpdateData);

		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			const expectedData = {
				name: 'VSP5_UPDATED',
				description: 'A software model for Fortigate._UPDATED',
				subCategory: 'resourceNewCategory.network connectivity.virtual links',
				category: 'resourceNewCategory.network connectivity',
				vendorId: '1',
				vendorName: 'Vendor',
				licensingVersion: '1.0',
				icon: 'icon',
				licensingData: {
					licenceAgreement: '123',
					featureGroups: [
						'123', '234'
					]
				}
			};
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${toBeUpdatedSoftwareProductId}`);
			expect(data).to.deep.equal(expectedData);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductActionHelper.updateSoftwareProductData(store.dispatch, {
			softwareProduct: softwareProductUpdateData
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Save Software product questionnaire only', () => {
		const softwareProduct = {
			name: 'VSP5',
			id: '4730033D16C64E3CA556AB0AC4478218',
			description: 'A software model for Fortigate.',
			subCategory: 'resourceNewCategory.network connectivity.virtual links',
			category: 'resourceNewCategory.network connectivity',
			vendorId: '1',
			vendorName: 'Vendor',
			icon: 'icon',
			licensingData: {
				licenceAgreement: '123',
				featureGroups: [
					'123', '234'
				]
			}
		};
		deepFreeze(softwareProduct);

		const store = storeCreator({
			softwareProduct: {
				softwareProductEditor: {data: softwareProduct},
				softwareProductQuestionnaire: {qdata: 'test', qschema: {type: 'string'}}
			}
		});
		deepFreeze(store.getState());
		const expectedStore = store.getState();

		const toBeUpdatedSoftwareProductId = softwareProduct.id;
		const questionnaireData = {
			general: {
				affinityData: {
					affinityGrouping: true,
					antiAffinityGrouping: false
				}
			}
		};
		deepFreeze(questionnaireData);

		mockRest.addHandler('save', ({data, options, baseUrl}) => {
			expect(baseUrl).to.equal(`/onboarding-api/v1.0/vendor-software-products/${toBeUpdatedSoftwareProductId}/questionnaire`);
			expect(data).to.deep.equal(questionnaireData);
			expect(options).to.equal(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductActionHelper.updateSoftwareProductQuestionnaire(store.dispatch, {
			softwareProductId: softwareProduct.id,
			qdata: questionnaireData
		}).then(() => {
			expect(store.getState()).to.deep.equal(expectedStore);
		});
	});

	it('Handle category without subcategories', () => {
		const categories = deepFreeze([
			{
				name: 'Resource Category 1',
				normalizedName: 'resource category 1',
				uniqueId: 'resourceNewCategory.resource category 1',
				subcategories: [
					{
						name: 'Sub Category for RC 1',
						normalizedName: 'sub category for rc 1',
						uniqueId: 'resourceNewCategory.resource category 1.sub category for rc 1'
					},
					{
						name: 'SC4RC2',
						normalizedName: 'sc4rc2',
						uniqueId: 'resourceNewCategory.resource category 1.sc4rc2'
					},
					{
						name: 'SC4RC1',
						normalizedName: 'sc4rc1',
						uniqueId: 'resourceNewCategory.resource category 1.sc4rc1'
					}
				]
			},
			{
				name: 'Eeeeee',
				normalizedName: 'eeeeee',
				uniqueId: 'resourceNewCategory.eeeeee'
			},
			{
				name: 'Some Recource',
				normalizedName: 'some recource',
				uniqueId: 'resourceNewCategory.some recource',
				subcategories: [
					{
						name: 'Second Sub Category for S',
						normalizedName: 'second sub category for s',
						uniqueId: 'resourceNewCategory.some recource.second sub category for s'
					},
					{
						name: 'Sub Category for Some Rec',
						normalizedName: 'sub category for some rec',
						uniqueId: 'resourceNewCategory.some recource.sub category for some rec'
					}
				]
			}
		]);
		const category = SoftwareProductCategoriesHelper.getCurrentCategoryOfSubCategory('resourceNewCategory.some recource.sub category for some rec', categories);
		expect(category).to.equal('resourceNewCategory.some recource');
	});

});


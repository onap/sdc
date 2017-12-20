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
import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import {cloneAndSet, buildFromExistingObject} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';

import SoftwareProductCreationActionHelper from 'sdc-app/onboarding/softwareProduct/creation/SoftwareProductCreationActionHelper.js';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import SoftwareProductCategoriesHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductCategoriesHelper.js';
import {forms} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';

import {VSPEditorFactory, VSPEditorPostFactory, VSPEditorFactoryWithLicensingData, VSPEditorPostFactoryWithLicensingData} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {CategoryFactory}  from 'test-utils/factories/softwareProduct/VSPCategoriesFactory.js';
import {heatSetupManifest} from 'test-utils/factories/softwareProduct/SoftwareProductAttachmentsFactories.js';

import { FeatureGroupStoreFactory as FeatureGroup} from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';
import {LicenseAgreementStoreFactory as LicenseAgreement} from 'test-utils/factories/licenseModel/LicenseAgreementFactories.js';

import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import {InitializedCurrentScreenFactory} from 'test-utils/factories/common/CurrentScreenFactory.js';

describe('Software Product Details Module Tests', function () {
	it('Get Software Products List', () => {
		const store = storeCreator();
		deepFreeze(store.getState());
		const softwareProductList = VSPEditorFactory.buildList(2);
		deepFreeze(softwareProductList);
		deepFreeze(store.getState());
		const expectedStore = cloneAndSet(store.getState(), 'softwareProductList', softwareProductList);

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual('/onboarding-api/v1.0/vendor-software-products/?versionFilter=Draft');
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: softwareProductList};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual('/onboarding-api/v1.0/vendor-software-products/?versionFilter=Certified');
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: []};
		});

		return SoftwareProductActionHelper.fetchSoftwareProductList(store.dispatch).then(() => {
			return SoftwareProductActionHelper.fetchFinalizedSoftwareProductList(store.dispatch);
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Add Software Product', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductPostRequest = VSPEditorPostFactory.build();
		deepFreeze(softwareProductPostRequest);
		const idFromResponse = '1';
		const expectedVSP = VSPEditorPostFactory.build({id: idFromResponse, vendorId: softwareProductPostRequest.vendorId});
		deepFreeze(expectedVSP);

		mockRest.addHandler('post', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual('/onboarding-api/v1.0/vendor-software-products/');
			expect(data).toEqual(softwareProductPostRequest);
			expect(options).toEqual(undefined);
			return {
				vspId: idFromResponse
			};
		});

		return SoftwareProductCreationActionHelper.createSoftwareProduct(store.dispatch, {
			softwareProduct: softwareProductPostRequest
		}).then((response) => {
			expect(response.vspId).toEqual(idFromResponse);
		});
	});

	it('Fetch Software Product with manifest', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const softwareProductPostRequest = VSPEditorPostFactory.build();
		deepFreeze(softwareProductPostRequest);

		const expectedGenericInfo = {
			'name': {
				isValid: true,
				errorText: '',
				validations: [{type: 'validateName', data: true}, {type: 'maxLength', data: 25}, {
					type: 'required',
					data: true
				}]
			},
			'description': {
				isValid: true,
				errorText: '',
				validations: [{type: 'required', data: true}]
			}
		};
		const expectedFormName = forms.VENDOR_SOFTWARE_PRODUCT_DETAILS;

		const idFromResponse = '1';
		const version = { id: '0.1', label: '0.1'};
		const expectedVSP = VSPEditorPostFactory.build({id: idFromResponse, vendorId: softwareProductPostRequest.vendorId});
		deepFreeze(expectedVSP);
		let expectedStore = cloneAndSet(store.getState(), 'softwareProduct.softwareProductEditor.data', expectedVSP);
		expectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductEditor.genericFieldInfo', expectedGenericInfo);
		expectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductEditor.formName', expectedFormName);
		expectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductQuestionnaire', {qdata: {}, dataMap: {}, qgenericFieldInfo: {}});

		expectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductAttachments.heatValidation', {
			'attachmentsTree': {},
			'errorList': [],
			'selectedNode': 'All'
		});
		let manifest = heatSetupManifest.build();
		expectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductAttachments.heatSetup', manifest);

		const expectedCurrentScreen = InitializedCurrentScreenFactory.build();
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreen.itemPermission);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.props', expectedCurrentScreen.props);

		mockRest.addHandler('post', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual('/onboarding-api/v1.0/vendor-software-products/');
			expect(data).toEqual(softwareProductPostRequest);
			expect(options).toEqual(undefined);
			return {
				vspId: idFromResponse,
				version
			};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${idFromResponse}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return expectedVSP;
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${idFromResponse}/versions/${version.id}/questionnaire`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {data: JSON.stringify({}), schema: JSON.stringify({})};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${idFromResponse}/versions/${version.id}/orchestration-template-candidate/manifest`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return manifest;
		});

		return SoftwareProductCreationActionHelper.createSoftwareProduct(store.dispatch, {
			softwareProduct: softwareProductPostRequest
		}).then(() => {
			return SoftwareProductActionHelper.fetchSoftwareProduct(store.dispatch, {softwareProductId: idFromResponse, version});
		}).then(() => {
			return SoftwareProductActionHelper.loadSoftwareProductHeatCandidate(store.dispatch, {softwareProductId: idFromResponse, version});
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
			let newName = 'newName';
			expectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductEditor.formReady', null);
			ValidationHelper.dataChanged(store.dispatch, {deltaData: {'name': newName}, formName: forms.VENDOR_SOFTWARE_PRODUCT_DETAILS});
			expectedStore = cloneAndSet(expectedStore, 'softwareProduct.softwareProductEditor.data.name', newName);
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Load and edit Software Product licensing data', () => {
		const store = storeCreator();

		const softwareProductPostRequest = VSPEditorPostFactory.build();
		deepFreeze(softwareProductPostRequest);

		const licenseModelId = softwareProductPostRequest.vendorId;
		const LMVersion = VersionFactory.build();
		const secondLicenseModelId = 'secondLicenseModelId';

		let FG1 = FeatureGroup.build();
		let LA1 = LicenseAgreement.build({
			featureGroupsIds: [FG1.id]
		});

		let FG2 = FeatureGroup.build();
		let LA2 = LicenseAgreement.build({
			featureGroupsIds: [FG2.id]
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual('/sdc1/feProxy/rest/v1/categories/resources/');
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return [];
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual('/onboarding-api/v1.0/vendor-license-models/?versionFilter=Certified');
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: []};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${licenseModelId}/versions/${LMVersion.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: {}};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${licenseModelId}/versions/${LMVersion.id}/license-agreements`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: [LA1]};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${licenseModelId}/versions/${LMVersion.id}/feature-groups`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: [FG1]};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${secondLicenseModelId}/versions/${LMVersion.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: {}};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${secondLicenseModelId}/versions/${LMVersion.id}/license-agreements`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: [LA2]};
		});

		mockRest.addHandler('fetch', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${secondLicenseModelId}/versions/${LMVersion.id}/feature-groups`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: [FG2]};
		});

		return SoftwareProductActionHelper.loadSoftwareProductDetailsData(store.dispatch, {licenseModelId, licensingVersion: LMVersion.id}).then(() => {
			let state = store.getState();
			expect(state.licenseModel.licenseAgreement.licenseAgreementList).toEqual([LA1]);
			expect(state.licenseModel.featureGroup.featureGroupsList).toEqual([FG1]);
			return SoftwareProductActionHelper.softwareProductEditorVendorChanged(store.dispatch,
						{deltaData: {vendorId: secondLicenseModelId, licensingVersion: LMVersion.id},
							formName: forms.VENDOR_SOFTWARE_PRODUCT_DETAILS}
			).then(() => {
				let state = store.getState();
				expect(state.licenseModel.licenseAgreement.licenseAgreementList).toEqual([LA2]);
				expect(state.licenseModel.featureGroup.featureGroupsList).toEqual([FG2]);
			});
		});
	});

	it('Save Software product', () => {

		const softwareProduct = VSPEditorFactoryWithLicensingData.build();
		deepFreeze(softwareProduct);

		const version = VersionFactory.build();

		const store = storeCreator({
			softwareProduct: {
				softwareProductEditor: {data: softwareProduct},
				softwareProductQuestionnaire: {qdata: 'test', qschema: {type: 'string'}}
			}
		});
		deepFreeze(store.getState());

		const dataForUpdate = {
			name: 'VSP5_UPDATED',
			description: 'A software model for Fortigate._UPDATED'
		};

		const toBeUpdatedSoftwareProductId = softwareProduct.id;
		let  softwareProductUpdateData = VSPEditorPostFactoryWithLicensingData.build(dataForUpdate);
		delete softwareProductUpdateData.version;

		const softwareProductPutRequest = buildFromExistingObject(VSPEditorFactoryWithLicensingData, softwareProductUpdateData, {id: toBeUpdatedSoftwareProductId, version: softwareProduct.version});

		deepFreeze(softwareProductUpdateData);

		const expectedStore = cloneAndSet(store.getState(), 'softwareProductList', [softwareProductPutRequest]);
		const questionnaireData = {
			general: {
				affinityData: {
					affinityGrouping: true,
					antiAffinityGrouping: false
				}
			}
		};
		deepFreeze(questionnaireData);

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${toBeUpdatedSoftwareProductId}/versions/${version.id}`);
			expect(data).toEqual(softwareProductUpdateData);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});
		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${toBeUpdatedSoftwareProductId}/versions/${version.id}/questionnaire`);
			expect(data).toEqual(questionnaireData);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductActionHelper.updateSoftwareProduct(store.dispatch, {
			softwareProduct: softwareProductPutRequest,
			qdata: questionnaireData,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Save Software product data only', () => {

		const softwareProduct = VSPEditorFactoryWithLicensingData.build();
		deepFreeze(softwareProduct);

		const version = VersionFactory.build();

		const store = storeCreator({
			softwareProduct: {
				softwareProductEditor: {data: softwareProduct},
				softwareProductQuestionnaire: {qdata: 'test', qschema: {type: 'string'}}
			}
		});
		deepFreeze(store.getState());
		const expectedStore = store.getState();

		const dataForUpdate = {
			name: 'VSP5_UPDATED',
			description: 'A software model for Fortigate._UPDATED'
		};

		const toBeUpdatedSoftwareProductId = softwareProduct.id;
		let  softwareProductUpdateData = VSPEditorPostFactoryWithLicensingData.build(dataForUpdate);
		delete softwareProductUpdateData.version;

		const softwareProductPutRequest = buildFromExistingObject(VSPEditorFactoryWithLicensingData, softwareProductUpdateData, {id: toBeUpdatedSoftwareProductId});

		deepFreeze(softwareProductUpdateData);

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${toBeUpdatedSoftwareProductId}/versions/${version.id}`);
			expect(data).toEqual(softwareProductUpdateData);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductActionHelper.updateSoftwareProductData(store.dispatch, {
			softwareProduct: softwareProductPutRequest,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Save Software product questionnaire only', () => {
		const softwareProduct = VSPEditorFactoryWithLicensingData.build();
		deepFreeze(softwareProduct);

		const version = VersionFactory.build();

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

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${toBeUpdatedSoftwareProductId}/versions/${version.id}/questionnaire`);
			expect(data).toEqual(questionnaireData);
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		return SoftwareProductActionHelper.updateSoftwareProductQuestionnaire(store.dispatch, {
			softwareProductId: softwareProduct.id,
			version,
			qdata: questionnaireData
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});

	it('Handle category without subcategories', () => {

		const categories = CategoryFactory.buildList(3);
		categories[0].subcategories = CategoryFactory.buildList(3);
		categories[2].subcategories = CategoryFactory.buildList(3);

		const category = SoftwareProductCategoriesHelper.getCurrentCategoryOfSubCategory(categories[2].subcategories[2].uniqueId, categories);
		expect(category).toEqual(categories[2].uniqueId);
	});

});

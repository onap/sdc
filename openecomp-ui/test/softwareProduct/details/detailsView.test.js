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
import {mapStateToProps} from 'sdc-app/onboarding/softwareProduct/details/SoftwareProductDetails.js';
import SoftwareProductDetailsView from 'sdc-app/onboarding/softwareProduct/details/SoftwareProductDetailsView.jsx';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {CategoryWithSubFactory}  from 'test-utils/factories/softwareProduct/VSPCategoriesFactory.js';
import {LicenseAgreementStoreFactory}  from 'test-utils/factories/licenseModel/LicenseAgreementFactories.js';
import {FeatureGroupStoreFactory}  from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';
import {SchemaGenericFieldInfoFactory}  from 'test-utils/factories/softwareProduct/SoftwareProductQSchemaFactory.js';
import {default as VspQdataFactory, VspDataMapFactory}  from 'test-utils/factories/softwareProduct/VspQdataFactory.js';
import {FinalizedLicenseModelFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';

describe('Software Product Details: ', function () {

	let currentSoftwareProduct = {}, currentSoftwareProductWithLicensingData = {}, softwareProductCategories = [],
		finalizedLicenseModelList, licenseAgreementList, featureGroupsList, qdata = {}, dataMap = {}, genericFieldInfo = {}, qGenericFieldInfo = {};
	let dummyFunc = () => {};

	beforeAll(function() {
		finalizedLicenseModelList = FinalizedLicenseModelFactory.buildList(2);
		currentSoftwareProduct = VSPEditorFactory.build({
			id: 'RTRTG454545',
			vendorId: finalizedLicenseModelList[0].id,
			vendorName: finalizedLicenseModelList[0].vendorName
		});
		softwareProductCategories = CategoryWithSubFactory.buildList(2, {}, {quantity: 1});
		licenseAgreementList = LicenseAgreementStoreFactory.buildList(2);
		featureGroupsList = FeatureGroupStoreFactory.buildList(2, {referencingLicenseAgreements: [licenseAgreementList[0].id]});
		qdata = VspQdataFactory.build();
		dataMap = VspDataMapFactory.build();
		currentSoftwareProductWithLicensingData = {
			...currentSoftwareProduct,
			licensingData: {
				licenseAgreement: licenseAgreementList[0].id,
				featureGroups: [featureGroupsList[0].id]
			}
		};
		genericFieldInfo = {
			'name': {
				isValid: true,
				errorText: '',
				validations: [{type: 'validateName', data: true}, {type: 'maxLength', data: 120}, {
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
		qGenericFieldInfo = SchemaGenericFieldInfoFactory.build();
	});

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('should mapper return vsp basic data', () => {

		var obj = {
			softwareProduct: {
				softwareProductEditor: {
					data: currentSoftwareProduct,
					genericFieldInfo
				},
				softwareProductCategories,
				softwareProductQuestionnaire: {
					qdata,
					genericFieldInfo: qGenericFieldInfo,
					dataMap
				}
			},
			finalizedLicenseModelList,
			licenseModel: {
				licenseAgreement: {
					licenseAgreementList: []
				},
				featureGroup: {
					featureGroupsList: []
				}
			}
		};

		var result = mapStateToProps(obj);
		expect(result.currentSoftwareProduct).toEqual(currentSoftwareProduct);
		expect(result.finalizedLicenseModelList).toEqual(finalizedLicenseModelList);
		expect(result.finalizedLicenseModelList.length).toBeGreaterThan(0);
		expect(finalizedLicenseModelList[0]).toMatchObject({
			id: result.currentSoftwareProduct.vendorId,
			vendorName: result.currentSoftwareProduct.vendorName
		});
		expect(result.softwareProductCategories).toEqual(softwareProductCategories);
		expect(result.licenseAgreementList).toEqual([]);
		expect(result.featureGroupsList).toEqual([]);
		expect(result.qdata).toEqual(qdata);
		expect(result.dataMap).toEqual(dataMap);
		expect(result.isFormValid).toEqual(true);
		expect(result.isReadOnlyMode).toEqual(true);
	});

	it('should mapper return vsp data with selected licenseAgreement and featureGroup', () => {

		var obj = {
			softwareProduct: {
				softwareProductEditor: {
					data: currentSoftwareProductWithLicensingData,
					genericFieldInfo
				},
				softwareProductCategories,
				softwareProductQuestionnaire: {
					qdata,
					genericFieldInfo: qGenericFieldInfo,
					dataMap
				}
			},
			finalizedLicenseModelList: finalizedLicenseModelList,
			licenseModel: {
				licenseAgreement: {
					licenseAgreementList: licenseAgreementList
				},
				featureGroup: {
					featureGroupsList: featureGroupsList
				}
			}
		};

		var result = mapStateToProps(obj);
		expect(result.currentSoftwareProduct).toEqual(currentSoftwareProductWithLicensingData);
		expect(result.finalizedLicenseModelList).toEqual(finalizedLicenseModelList);
		expect(result.finalizedLicenseModelList.length).toBeGreaterThan(0);
		expect(result.finalizedLicenseModelList[0]).toMatchObject({
			id: result.currentSoftwareProduct.vendorId,
			vendorName: result.currentSoftwareProduct.vendorName
		});
		expect(result.softwareProductCategories).toEqual(softwareProductCategories);
		expect(result.licenseAgreementList).toEqual(licenseAgreementList);
		expect(result.licenseAgreementList[0]).toMatchObject({...licenseAgreementList[0], id: result.currentSoftwareProduct.licensingData.licenseAgreement});
		result.currentSoftwareProduct.licensingData.featureGroups.forEach(fg => {
			expect(featureGroupsList[0]).toMatchObject({...featureGroupsList[0], id: fg});
		});
		expect(result.qdata).toEqual(qdata);
		expect(result.isReadOnlyMode).toEqual(true);
	});

	it('VSP Details view test', () => {

		let params = {
			currentSoftwareProduct,
			softwareProductCategories,
			qdata,
			dataMap,
			isFormValid: true,
			finalizedLicenseModelList,
			licenseAgreementList,
			featureGroupsList,
			genericFieldInfo,
			qGenericFieldInfo,
		};
		var renderer = TestUtils.createRenderer();
		renderer.render(
			<SoftwareProductDetailsView
				{...params}
				onSubmit = {dummyFunc}
				onDataChanged = {dummyFunc}
				onValidityChanged = {dummyFunc}
				onQDataChanged = {dummyFunc}
				onVendorParamChanged = {dummyFunc}/>
		);
		let renderedOutput = renderer.getRenderOutput();
		expect(renderedOutput).toBeTruthy();
	});

	it('in view: should change vendorId and update vsp licensing-version', done => {

		let params = {
			currentSoftwareProduct: currentSoftwareProductWithLicensingData,
			softwareProductCategories,
			qdata,
			dataMap,
			isFormValid: true,
			genericFieldInfo,
			qGenericFieldInfo,
			finalizedLicenseModelList,
			licenseAgreementList,
			featureGroupsList
		};
		const onVendorChangedListener = (deltaData) => {
			expect(deltaData.vendorId).toEqual(finalizedLicenseModelList[1].id);
			expect(deltaData.vendorName).toEqual(finalizedLicenseModelList[1].vendorName);
			expect(deltaData.licensingVersion).toEqual('');
			expect(deltaData.licensingData).toEqual({});
			done();
		};

		var vspDetailsView = TestUtils.renderIntoDocument(<SoftwareProductDetailsView
			currentSoftwareProduct = {params.currentSoftwareProduct}
			softwareProductCategories = {params.softwareProductCategories}
			qdata = {params.qdata}
			qGenericFieldInfo = {params.qGenericFieldInfo}
			genericFieldInfo = {params.genericFieldInfo}
			isFormValid={params.isFormValid}
			dataMap={params.dataMap}
			finalizedLicenseModelList = {params.finalizedLicenseModelList}
			licenseAgreementList = {params.licenseAgreementList}
			featureGroupsList = {params.featureGroupsList}
			onSubmit = {dummyFunc}
			onDataChanged = {dummyFunc}
			onValidityChanged = {dummyFunc}
			onQDataChanged = {dummyFunc}
			onVendorParamChanged = {(deltaData) => onVendorChangedListener(deltaData)}/>);
		expect(vspDetailsView).toBeTruthy();
		vspDetailsView.onVendorParamChanged({vendorId: finalizedLicenseModelList[1].id});
	});

	it('in view: should change licensing-version and update licensing data', done => {
		let params = {
			currentSoftwareProduct: currentSoftwareProduct,
			softwareProductCategories,
			qdata,
			dataMap,
			isFormValid: true,
			genericFieldInfo,
			qGenericFieldInfo,
			finalizedLicenseModelList,
			licenseAgreementList,
			featureGroupsList
		};
		const onVendorChangedListener = (deltaData) => {
			expect(deltaData.vendorId).toEqual(finalizedLicenseModelList[1].id);
			expect(deltaData.vendorName).toEqual(finalizedLicenseModelList[1].vendorName);
			expect(deltaData.licensingVersion).toEqual(finalizedLicenseModelList[1].viewableVersion[0]);
			expect(deltaData.licensingData).toEqual({});
			done();
		};

		let vspDetailsView = TestUtils.renderIntoDocument(<SoftwareProductDetailsView
			{...params}
			onSubmit = {dummyFunc}
			onDataChanged = {dummyFunc}
			onValidityChanged = {dummyFunc}
			onQDataChanged = {dummyFunc}
			onVendorParamChanged = {(deltaData) => onVendorChangedListener(deltaData)}/>);
		expect(vspDetailsView).toBeTruthy();
		vspDetailsView.onVendorParamChanged({vendorId: finalizedLicenseModelList[1].id, licensingVersion: finalizedLicenseModelList[1].viewableVersion[0]});
	});

	it('in view: should change subcategory', done => {
		let params = {
			currentSoftwareProduct: currentSoftwareProduct,
			softwareProductCategories,
			qdata,
			dataMap,
			isFormValid: true,
			genericFieldInfo,
			qGenericFieldInfo,
			finalizedLicenseModelList,
			licenseAgreementList,
			featureGroupsList
		};
		const onDataChangedListener = ({category, subCategory}) => {
			expect(category).toEqual(softwareProductCategories[1].uniqueId);
			expect(subCategory).toEqual(softwareProductCategories[1].subcategories[0].uniqueId);
			done();
		};

		let vspDetailsView = TestUtils.renderIntoDocument(<SoftwareProductDetailsView
			{...params}
			onSubmit = {dummyFunc}
			onDataChanged = {({category, subCategory}) => onDataChangedListener({category, subCategory})}
			onValidityChanged = {dummyFunc}
			onQDataChanged = {dummyFunc}
			onVendorParamChanged = {dummyFunc}/>);
		expect(vspDetailsView).toBeTruthy();
		vspDetailsView.onSelectSubCategory(softwareProductCategories[1].subcategories[0].uniqueId);
	});

	it('in view: should change feature groups', done => {

		let params = {
			currentSoftwareProduct: currentSoftwareProductWithLicensingData,
			softwareProductCategories,
			qdata,
			dataMap,
			isFormValid: true,
			genericFieldInfo,
			qGenericFieldInfo,
			finalizedLicenseModelList,
			licenseAgreementList,
			featureGroupsList
		};
		const onDataChangedListener = ({licensingData}) => {
			expect(licensingData.licenseAgreement).toEqual(licenseAgreementList[0].id);
			expect(licensingData.featureGroups).toEqual([
				{enum: featureGroupsList[0].id, title: featureGroupsList[0].name},
				{enum: featureGroupsList[1].id, title: featureGroupsList[1].name}
			]);
			done();
		};

		let vspDetailsView = TestUtils.renderIntoDocument(<SoftwareProductDetailsView
			{...params}
			onSubmit = {dummyFunc}
			onDataChanged = {({licensingData}) => onDataChangedListener({licensingData})}
			onValidityChanged = {dummyFunc}
			onQDataChanged = {dummyFunc}
			onVendorParamChanged = {dummyFunc}/>);
		expect(vspDetailsView).toBeTruthy();
		vspDetailsView.onFeatureGroupsChanged({featureGroups: [
			{enum: featureGroupsList[0].id, title: featureGroupsList[0].name},
			{enum: featureGroupsList[1].id, title: featureGroupsList[1].name}
		]});
	});

	it('in view: should change license agreement', done => {

		let params = {
			currentSoftwareProduct: currentSoftwareProductWithLicensingData,
			softwareProductCategories,
			qdata,
			dataMap,
			isFormValid: true,
			genericFieldInfo,
			qGenericFieldInfo,
			finalizedLicenseModelList,
			licenseAgreementList,
			featureGroupsList
		};
		const onDataChangedListener = ({licensingData}) => {
			expect(licensingData.licenseAgreement).toEqual(licenseAgreementList[1].id);
			expect(licensingData.featureGroups).toEqual([]);
			done();
		};

		let vspDetailsView = TestUtils.renderIntoDocument(<SoftwareProductDetailsView
			{...params}
			onSubmit = {dummyFunc}
			onDataChanged = {({licensingData}) => onDataChangedListener({licensingData})}
			onValidityChanged = {dummyFunc}
			onQDataChanged = {dummyFunc}
			onVendorParamChanged = {dummyFunc}/>);
		expect(vspDetailsView).toBeTruthy();
		vspDetailsView.onLicensingDataChanged({licenseAgreement: licenseAgreementList[1].id, featureGroups: []});
	});
});

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
import {mapStateToProps} from 'sdc-app/onboarding/softwareProduct/details/SoftwareProductDetails.js';
import SoftwareProductDetailsView from 'sdc-app/onboarding/softwareProduct/details/SoftwareProductDetailsView.jsx';
import {vspQschema as vspQuestionnaireSchema} from './vspQschema.js';

describe('Software Product Details: ', function () {

	let currentSoftwareProduct = {}, categories = [], finalizedLicenseModelList, licenseAgreementList, featureGroupsList, vspQschema;
	let dummyFunc = () => {};

	before(function() {
		currentSoftwareProduct = {
			id: 'D4774719D085414E9D5642D1ACD59D20',
			name: 'VSP',
			description: 'dfdf',
			category: 'category1',
			subCategory: 'category1.subCategory',
			vendorId: 'VLM_ID1',
			vendorName: 'VLM1',
			licensingVersion: '1.0',
			licensingData: {}
		};
		categories = [{
			uniqueId: 'category1',
			subcategories: [{
				uniqueId: 'subCategory'
			}]
		}, {
			uniqueId: 'category2',
			subcategories: [{
				uniqueId: 'subCategory2'
			}]
		}];
		finalizedLicenseModelList = [{
			id: 'VLM_ID1',
			name: 'VLM1'
		}];
		licenseAgreementList = [{id: 'LA_ID1'}, {id: 'LA_ID2'}];
		featureGroupsList = [
			{id: 'FG_ID1', name: 'FG1', referencingLicenseAgreements: ['LA_ID1']},
			{id: 'FG_ID2', name: 'FG2', referencingLicenseAgreements: ['LA_ID1']}
		];
		vspQschema = vspQuestionnaireSchema;
	});

	it('should mapper exist', () => {
		expect(mapStateToProps).toExist();
	});

	it('should mapper return vsp basic data', () => {
		var obj = {
			softwareProduct: {
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductCategories: categories,
				softwareProductQuestionnaire: {
					qdata: {},
					qschema: vspQschema
				}
			},
			finalizedLicenseModelList: finalizedLicenseModelList,
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
		expect(finalizedLicenseModelList).toInclude({
			id: result.currentSoftwareProduct.vendorId,
			name: result.currentSoftwareProduct.vendorName
		});
		expect(result.softwareProductCategories).toEqual(categories);
		expect(result.licenseAgreementList).toEqual([]);
		expect(result.featureGroupsList).toEqual([]);
		expect(result.qdata).toEqual({});
		expect(result.qschema).toEqual(vspQschema);
		expect(result.isReadOnlyMode).toEqual(true);
	});

	it('should mapper return vsp data with selected licenseAgreement and featureGroup', () => {
		let vspWithLicensingData = {
			...currentSoftwareProduct,
			licensingData: {
				licenseAgreement: 'LA_ID1',
				featureGroups: [{enum: 'FG_ID1', title: 'FG1'}]
			}
		};
		var obj = {
			softwareProduct: {
				softwareProductEditor: {
					data: vspWithLicensingData
				},
				softwareProductCategories: categories,
				softwareProductQuestionnaire: {
					qdata: {},
					qschema: vspQschema
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
		expect(result.currentSoftwareProduct).toEqual(vspWithLicensingData);
		expect(result.finalizedLicenseModelList).toEqual(finalizedLicenseModelList);
		expect(result.finalizedLicenseModelList.length).toBeGreaterThan(0);
		expect(result.finalizedLicenseModelList).toInclude({
			id: result.currentSoftwareProduct.vendorId,
			name: result.currentSoftwareProduct.vendorName
		});
		expect(result.softwareProductCategories).toEqual(categories);
		expect(result.licenseAgreementList).toEqual(licenseAgreementList);
		expect(result.licenseAgreementList).toInclude({id: result.currentSoftwareProduct.licensingData.licenseAgreement});
		result.currentSoftwareProduct.licensingData.featureGroups.forEach(fg => {
			expect(featureGroupsList).toInclude({
				id: fg.enum,
				name: fg.title,
				referencingLicenseAgreements: [result.currentSoftwareProduct.licensingData.licenseAgreement]
			});
			expect(result.featureGroupsList).toInclude(fg);
		});
		expect(result.qdata).toEqual({});
		expect(result.qschema).toEqual(vspQschema);
		expect(result.isReadOnlyMode).toEqual(true);
	});

	it('VSP Details view test', () => {
		let params = {
			currentSoftwareProduct: currentSoftwareProduct,
			softwareProductCategories: categories,
			qdata: {},
			qschema: vspQschema,
			finalizedLicenseModelList: [{
				id: 'VLM_ID1',
				vendorName: 'VLM1',
				version: '2.0',
				viewableVersions: ['1.0', '2.0']
			}, {
				id: 'VLM_ID2',
				vendorName: 'VLM2',
				version: '3.0',
				viewableVersions: ['1.0', '2.0', '3.0']
			}],
			licenseAgreementList: [{id: 'LA_ID1'}, {id: 'LA_ID2'}],
			featureGroupsList: [
				{id: 'FG_ID1', name: 'FG1', referencingLicenseAgreements: ['LA_ID1']},
				{id: 'FG_ID2', name: 'FG2', referencingLicenseAgreements: ['LA_ID1']}
			]
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
		expect(renderedOutput).toExist();
	});

	it('in view: should change vendorId and update vsp licensing-version', done => {
		let vspWithLicensingData = {
			...currentSoftwareProduct,
			licensingData: {
				licenseAgreement: 'LA_ID1',
				featureGroups: [{enum: 'FG_ID1', title: 'FG1'}]
			}
		};
		let params = {
			currentSoftwareProduct: vspWithLicensingData,
			softwareProductCategories: categories,
			qdata: {},
			qschema: vspQschema,
			finalizedLicenseModelList: [{
				id: 'VLM_ID1',
				vendorName: 'VLM1',
				version: '2.0',
				viewableVersions: ['1.0', '2.0']
			}, {
				id: 'VLM_ID2',
				vendorName: 'VLM2',
				version: '3.0',
				viewableVersions: ['1.0', '2.0', '3.0']
			}],
			licenseAgreementList: [{id: 'LA_ID1'}, {id: 'LA_ID2'}],
			featureGroupsList: [
				{id: 'FG_ID1', name: 'FG1', referencingLicenseAgreements: ['LA_ID1']},
				{id: 'FG_ID2', name: 'FG2', referencingLicenseAgreements: ['LA_ID1']}
			]
		};
		const onVendorChangedListener = (deltaData) => {
			expect(deltaData.vendorId).toEqual('VLM_ID2');
			expect(deltaData.vendorName).toEqual('VLM2');
			expect(deltaData.licensingVersion).toEqual('');
			expect(deltaData.licensingData).toEqual({});
			done();
		};

		var vspDetailsView = TestUtils.renderIntoDocument(<SoftwareProductDetailsView
			currentSoftwareProduct = {params.currentSoftwareProduct}
			softwareProductCategories = {params.softwareProductCategories}
			qdata = {params.qdata}
			qschema = {params.qschema}
			finalizedLicenseModelList = {params.finalizedLicenseModelList}
			licenseAgreementList = {params.licenseAgreementList}
			featureGroupsList = {params.featureGroupsList}
			onSubmit = {dummyFunc}
			onDataChanged = {dummyFunc}
			onValidityChanged = {dummyFunc}
			onQDataChanged = {dummyFunc}
			onVendorParamChanged = {(deltaData) => onVendorChangedListener(deltaData)}/>);
		expect(vspDetailsView).toExist();
		vspDetailsView.onVendorParamChanged({vendorId: 'VLM_ID2'});
	});

	it('in view: should change licensing-version and update licensing data', done => {
		let params = {
			currentSoftwareProduct: currentSoftwareProduct,
			softwareProductCategories: categories,
			qdata: {},
			qschema: vspQschema,
			finalizedLicenseModelList: [{
				id: 'VLM_ID1',
				vendorName: 'VLM1',
				version: '2.0',
				viewableVersions: ['1.0', '2.0']
			}, {
				id: 'VLM_ID2',
				vendorName: 'VLM2',
				version: '3.0',
				viewableVersions: ['1.0', '2.0', '3.0']
			}],
			licenseAgreementList: [{id: 'LA_ID1'}, {id: 'LA_ID2'}],
			featureGroupsList: [
				{id: 'FG_ID1', name: 'FG1', referencingLicenseAgreements: ['LA_ID1']},
				{id: 'FG_ID2', name: 'FG2', referencingLicenseAgreements: ['LA_ID1']}
			]
		};
		const onVendorChangedListener = (deltaData) => {
			expect(deltaData.vendorId).toEqual('VLM_ID2');
			expect(deltaData.vendorName).toEqual('VLM2');
			expect(deltaData.licensingVersion).toEqual('2.0');
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
		expect(vspDetailsView).toExist();
		vspDetailsView.onVendorParamChanged({vendorId: 'VLM_ID2', licensingVersion: '2.0'});
	});

	it('in view: should change subcategory', done => {
		let params = {
			currentSoftwareProduct: currentSoftwareProduct,
			softwareProductCategories: categories,
			qdata: {},
			qschema: vspQschema,
			finalizedLicenseModelList: [{
				id: 'VLM_ID1',
				vendorName: 'VLM1',
				version: '2.0',
				viewableVersions: ['1.0', '2.0']
			}, {
				id: 'VLM_ID2',
				vendorName: 'VLM2',
				version: '3.0',
				viewableVersions: ['1.0', '2.0', '3.0']
			}],
			licenseAgreementList: [{id: 'LA_ID1'}, {id: 'LA_ID2'}],
			featureGroupsList: [
				{id: 'FG_ID1', name: 'FG1', referencingLicenseAgreements: ['LA_ID1']},
				{id: 'FG_ID2', name: 'FG2', referencingLicenseAgreements: ['LA_ID1']}
			]
		};
		const onDataChangedListener = ({category, subCategory}) => {
			expect(category).toEqual('category2');
			expect(subCategory).toEqual('subCategory2');
			done();
		};

		let vspDetailsView = TestUtils.renderIntoDocument(<SoftwareProductDetailsView
			{...params}
			onSubmit = {dummyFunc}
			onDataChanged = {({category, subCategory}) => onDataChangedListener({category, subCategory})}
			onValidityChanged = {dummyFunc}
			onQDataChanged = {dummyFunc}
			onVendorParamChanged = {dummyFunc}/>);
		expect(vspDetailsView).toExist();
		vspDetailsView.onSelectSubCategory('subCategory2');
	});

	it('in view: should change feature groups', done => {
		let vspWithLicensingData = {
			...currentSoftwareProduct,
			licensingData: {
				licenseAgreement: 'LA_ID1',
				featureGroups: [{enum: 'FG_ID1', title: 'FG1'}]
			}
		};
		let params = {
			currentSoftwareProduct: vspWithLicensingData,
			softwareProductCategories: categories,
			qdata: {},
			qschema: vspQschema,
			finalizedLicenseModelList: [{
				id: 'VLM_ID1',
				vendorName: 'VLM1',
				version: '2.0',
				viewableVersions: ['1.0', '2.0']
			}, {
				id: 'VLM_ID2',
				vendorName: 'VLM2',
				version: '3.0',
				viewableVersions: ['1.0', '2.0', '3.0']
			}],
			licenseAgreementList: [{id: 'LA_ID1'}, {id: 'LA_ID2'}],
			featureGroupsList: [
				{id: 'FG_ID1', name: 'FG1', referencingLicenseAgreements: ['LA_ID1']},
				{id: 'FG_ID2', name: 'FG2', referencingLicenseAgreements: ['LA_ID1']}
			]
		};
		const onDataChangedListener = ({licensingData}) => {
			expect(licensingData.licenseAgreement).toEqual('LA_ID1');
			expect(licensingData.featureGroups).toEqual([
				{enum: 'FG_ID1', title: 'FG1'},
				{enum: 'FG_ID2', title: 'FG2'}
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
		expect(vspDetailsView).toExist();
		vspDetailsView.onFeatureGroupsChanged({featureGroups: [
			{enum: 'FG_ID1', title: 'FG1'},
			{enum: 'FG_ID2', title: 'FG2'}
		]});
	});

	it('in view: should change license agreement', done => {
		let vspWithLicensingData = {
			...currentSoftwareProduct,
			licensingData: {
				licenseAgreement: 'LA_ID1',
				featureGroups: [{enum: 'FG_ID1', title: 'FG1'}]
			}
		};
		let params = {
			currentSoftwareProduct: vspWithLicensingData,
			softwareProductCategories: categories,
			qdata: {},
			qschema: vspQschema,
			finalizedLicenseModelList: [{
				id: 'VLM_ID1',
				vendorName: 'VLM1',
				version: '2.0',
				viewableVersions: ['1.0', '2.0']
			}, {
				id: 'VLM_ID2',
				vendorName: 'VLM2',
				version: '3.0',
				viewableVersions: ['1.0', '2.0', '3.0']
			}],
			licenseAgreementList: [{id: 'LA_ID1'}, {id: 'LA_ID2'}],
			featureGroupsList: [
				{id: 'FG_ID1', name: 'FG1', referencingLicenseAgreements: ['LA_ID1']},
				{id: 'FG_ID2', name: 'FG2', referencingLicenseAgreements: ['LA_ID1']}
			]
		};
		const onDataChangedListener = ({licensingData}) => {
			expect(licensingData.licenseAgreement).toEqual('LA_ID2');
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
		expect(vspDetailsView).toExist();
		vspDetailsView.onLicensingDataChanged({licenseAgreement: 'LA_ID2', featureGroups: []});
	});
});

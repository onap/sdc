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

import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {CategoryWithSubFactory}  from 'test-utils/factories/softwareProduct/VSPCategoriesFactory.js';
import {LicenseAgreementStoreFactory}  from 'test-utils/factories/licenseModel/LicenseAgreementFactories.js';
import {FeatureGroupStoreFactory}  from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';
import {default as SoftwareProductQSchemaFactory}  from 'test-utils/factories/softwareProduct/SoftwareProductQSchemaFactory.js';
import {default as VspQdataFactory}  from 'test-utils/factories/softwareProduct/VspQdataFactory.js';
import {VSPComponentsFactory}  from 'test-utils/factories/softwareProduct/SoftwareProductComponentsFactories.js';
import {FinalizedLicenseModelFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';


import {mapStateToProps} from 'sdc-app/onboarding/softwareProduct/landingPage/SoftwareProductLandingPage.js';
import SoftwareProductLandingPageView from 'sdc-app/onboarding/softwareProduct/landingPage/SoftwareProductLandingPageView.jsx';


describe('Software Product Landing Page: ', function () {

	let currentSoftwareProduct = {}, softwareProductCategories = [],
		finalizedLicenseModelList, licenseAgreementList, featureGroupsList, qschema, qdata = {};
	const dummyFunc = () => {};
	beforeAll(function() {
		finalizedLicenseModelList = FinalizedLicenseModelFactory.buildList(2);
		currentSoftwareProduct = VSPEditorFactory.build({id:'RTRTG454545', vendorId: finalizedLicenseModelList[0].id, vendorName: finalizedLicenseModelList[0].name});
		softwareProductCategories = CategoryWithSubFactory.buildList(2,{},{quantity: 1});
		licenseAgreementList = LicenseAgreementStoreFactory.buildList(2);
		featureGroupsList = FeatureGroupStoreFactory.buildList(2,{referencingLicenseAgreements:[licenseAgreementList[0].id]});
		qdata = VspQdataFactory.build();
		qschema = SoftwareProductQSchemaFactory.build(qdata);

	});



	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('should mapper return vsp basic data', () => {
		const obj = {
			softwareProduct: {
				softwareProductEditor: {
					data: currentSoftwareProduct
				},
				softwareProductCategories,
				softwareProductQuestionnaire: {
					qdata,
					qschema
				},
				softwareProductComponents: {
					componentsList:[]
				}
			},
			finalizedLicenseModelList,
			licenseModel: {
				licenseAgreement: {
					licenseAgreementList
				},
				featureGroup: {
					featureGroupsList
				}
			}
		};

		const result = mapStateToProps(obj);
		expect(result.currentSoftwareProduct).toBeTruthy();
		expect(result.isReadOnlyMode).toEqual(true);

	});

	it('vsp landing basic view', () => {

		const params = {
			currentSoftwareProduct,
			isReadOnlyMode: false,
			componentsList: VSPComponentsFactory.buildList(2)
		};

		let vspLandingView = TestUtils.renderIntoDocument(<SoftwareProductLandingPageView
			{...params}/>);
		expect(vspLandingView).toBeTruthy();
	});

	it('vsp landing handleOnDragEnter test ', () => {

		const params = {
			currentSoftwareProduct,
			isReadOnlyMode: false,
			componentsList: VSPComponentsFactory.buildList(2)
		};

		let vspLandingView = TestUtils.renderIntoDocument(<SoftwareProductLandingPageView
			{...params}/>);
		expect(vspLandingView).toBeTruthy();
		vspLandingView.handleOnDragEnter(false);
		expect(vspLandingView.state.dragging).toEqual(true);
	});


	it('vsp landing handleImportSubmit test ', () => {

		const params = {
			currentSoftwareProduct,
			isReadOnlyMode: false,
			componentsList: VSPComponentsFactory.buildList(2),
			onUploadConfirmation:  dummyFunc,
			onUpload: dummyFunc,
			onInvalidFileSizeUpload: dummyFunc
		};

		let vspLandingView = TestUtils.renderIntoDocument(<SoftwareProductLandingPageView
			{...params}/>);
		expect(vspLandingView).toBeTruthy();
		const files = [
			{
				name: 'aaa',
				size: 123
			}
		];

		vspLandingView.handleImportSubmit(files, false);
		expect(vspLandingView.state.dragging).toEqual(false);
		expect(vspLandingView.state.fileName).toEqual(files[0].name);
		const files1 = [
			{
				name: 'bbb',
				size: 0
			}
		];
		vspLandingView.handleImportSubmit(files1, false);
	});

	it('vsp landing handleImportSubmit with damaged file test ', () => {

		const params = {
			currentSoftwareProduct,
			isReadOnlyMode: false,
			componentsList: VSPComponentsFactory.buildList(2),
			onUploadConfirmation:  dummyFunc,
			onUpload: dummyFunc,
			onInvalidFileSizeUpload: dummyFunc
		};

		let vspLandingView = TestUtils.renderIntoDocument(<SoftwareProductLandingPageView
			{...params}/>);
		expect(vspLandingView).toBeTruthy();
		const files = [
			{
				name: 'aaa',
				size: 0
			}
		];

		vspLandingView.handleImportSubmit(files, false);
		expect(vspLandingView.state.dragging).toEqual(false);
		expect(vspLandingView.state.fileName).toEqual('');
	});
});

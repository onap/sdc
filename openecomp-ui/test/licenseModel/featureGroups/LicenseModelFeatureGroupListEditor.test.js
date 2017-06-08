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
import {mapStateToProps} from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupListEditor.js';
import FeatureGroupsListEditorView from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupListEditorView.jsx';
import { FeatureGroupStoreFactory } from 'test-utils/factories/licenseModel/FeatureGroupFactories.js';
import {LicenseModelOverviewFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';
import { buildListFromFactory } from 'test-utils/Util.js';
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';

describe('License Model  Feature Group List  Module Tests', function () {

	it('should mapper exist', () => {
		expect(mapStateToProps).toBeTruthy();
	});


	it('should return empty data', () => {


		let licenseModel = LicenseModelOverviewFactory.build({
			featureGroup: {
				featureGroupEditor: {},
				featureGroupsList: []
			},
			licenseModelEditor: {
				data:{
					...VersionControllerUtilsFactory.build()
				}
			}
		});
		var results = mapStateToProps({licenseModel});
		expect(results.vendorName).toEqual(undefined);
		expect(results.featureGroupsModal.show).toEqual(false);
		expect(results.featureGroupsModal.editMode).toEqual(false);
		expect(results.featureGroupsList).toEqual([]);
	});

	it('should return true for show and edit mode and vendorName should be not empty', () => {

		let licenseModel = LicenseModelOverviewFactory.build({
			featureGroup: {
				featureGroupEditor: {
					data: FeatureGroupStoreFactory.build()
				},
				featureGroupsList: []
			}
		});
		var results = mapStateToProps({licenseModel});
		expect(results.featureGroupsModal.show).toEqual(true);
		expect(results.featureGroupsModal.editMode).toEqual(true);
		expect(results.vendorName).toEqual(licenseModel.licenseModelEditor.data.vendorName);
	});

	it('jsx view test', () => {
		var view = TestUtils.renderIntoDocument(<FeatureGroupsListEditorView  vendorName=''
			licenseModelId=''
			featureGroupsModal={{show: false, editMode: false}}
			isReadOnlyMode={false}
			onAddFeatureGroupClick={()=>{}}
			featureGroupsList={[]} />);
		expect(view).toBeTruthy();
	});

	it('jsx view list test', () => {
		var view = TestUtils.renderIntoDocument(<FeatureGroupsListEditorView  vendorName=''
			licenseModelId=''
			featureGroupsModal={{show: false, editMode: true}}
			isReadOnlyMode={false}
			onAddFeatureGroupClick={()=>{}}
			featureGroupsList={buildListFromFactory(FeatureGroupStoreFactory)} />);
		expect(view).toBeTruthy();
	});

});

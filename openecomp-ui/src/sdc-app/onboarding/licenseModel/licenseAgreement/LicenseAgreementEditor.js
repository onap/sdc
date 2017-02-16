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

import {connect} from 'react-redux';
import LicenseAgreementActionHelper from './LicenseAgreementActionHelper.js';
import LicenseAgreementEditorView from './LicenseAgreementEditorView.jsx';

export const mapStateToProps = ({licenseModel: {licenseAgreement, featureGroup}}) => {


	let {data, selectedTab, selectedFeatureGroupsButtonTab} = licenseAgreement.licenseAgreementEditor;

	let previousData;
	const licenseAgreementId = data ? data.id : null;
	if(licenseAgreementId) {
		previousData = licenseAgreement.licenseAgreementList.find(licenseAgreement => licenseAgreement.id === licenseAgreementId);
	}

	const {featureGroupsList = []} = featureGroup;

	return {
		data,
		previousData,
		selectedTab,
		selectedFeatureGroupsButtonTab,
		featureGroupsList
	};
};

export const mapActionsToProps = (dispatch, {licenseModelId}) => {
	return {
		onDataChanged: deltaData => LicenseAgreementActionHelper.licenseAgreementEditorDataChanged(dispatch, {deltaData}),
		onTabSelect: tab => LicenseAgreementActionHelper.selectLicenseAgreementEditorTab(dispatch, {tab}),
		onFeatureGroupsButtonTabSelect: buttonTab => LicenseAgreementActionHelper.selectLicenseAgreementEditorFeatureGroupsButtonTab(dispatch, {buttonTab}),
		onCancel: () => LicenseAgreementActionHelper.closeLicenseAgreementEditor(dispatch),
		onSubmit: ({previousLicenseAgreement, licenseAgreement}) => {
			LicenseAgreementActionHelper.closeLicenseAgreementEditor(dispatch);
			LicenseAgreementActionHelper.saveLicenseAgreement(dispatch, {licenseModelId, previousLicenseAgreement, licenseAgreement});
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LicenseAgreementEditorView);

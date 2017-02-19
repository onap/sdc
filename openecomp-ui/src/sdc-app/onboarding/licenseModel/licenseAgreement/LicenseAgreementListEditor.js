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
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';
import LicenseAgreementActionHelper from './LicenseAgreementActionHelper.js';
import LicenseAgreementListEditorView from './LicenseAgreementListEditorView.jsx';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';

const mapStateToProps = ({licenseModel: {licenseAgreement, licenseModelEditor}}) => {
	let {licenseAgreementList} = licenseAgreement;
	let {data} = licenseAgreement.licenseAgreementEditor;
	let {vendorName} = licenseModelEditor.data;

	let isReadOnlyMode = VersionControllerUtils.isReadOnly(licenseModelEditor.data);

	return {
		vendorName,
		licenseAgreementList,
		isReadOnlyMode,
		isDisplayModal: Boolean(data),
		isModalInEditMode: Boolean(data && data.id)
	};
};

const mapActionsToProps = (dispatch, {licenseModelId}) => {
	return {
		onAddLicenseAgreementClick: () => LicenseAgreementActionHelper.openLicenseAgreementEditor(dispatch, {licenseModelId}),
		onEditLicenseAgreementClick: licenseAgreement => LicenseAgreementActionHelper.openLicenseAgreementEditor(dispatch, {licenseModelId, licenseAgreement}),
		onDeleteLicenseAgreement: licenseAgreement => LicenseAgreementActionHelper.openDeleteLicenseAgreementConfirm(dispatch, {licenseAgreement}),
		onCallVCAction: action => {
			LicenseModelActionHelper.performVCAction(dispatch, {licenseModelId, action}).then(() => {
				LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {licenseModelId});
			});
		},
		switchLicenseModelVersion: version => LicenseAgreementActionHelper.switchVersion(dispatch, {licenseModelId, version}),
		onClose: () => OnboardingActionHelper.navigateToOnboardingCatalog(dispatch)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LicenseAgreementListEditorView);

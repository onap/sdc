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
import LicenseKeyGroupsActionHelper from './LicenseKeyGroupsActionHelper.js';
import LicenseKeyGroupsListEditorView from './LicenseKeyGroupsListEditorView.jsx';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';

const mapStateToProps = ({licenseModel: {licenseKeyGroup, licenseModelEditor}}) => {
	let {licenseKeyGroupsList} = licenseKeyGroup;
	let {data} = licenseKeyGroup.licenseKeyGroupsEditor;
	let {vendorName} = licenseModelEditor.data;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(licenseModelEditor.data);

	return {
		vendorName,
		licenseKeyGroupsList,
		isReadOnlyMode,
		isDisplayModal: Boolean(data),
		isModalInEditMode: Boolean(data && data.id)
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onAddLicenseKeyGroupClick: () => LicenseKeyGroupsActionHelper.openLicenseKeyGroupsEditor(dispatch),
		onEditLicenseKeyGroupClick: licenseKeyGroup => LicenseKeyGroupsActionHelper.openLicenseKeyGroupsEditor(dispatch, {licenseKeyGroup}),
		onDeleteLicenseKeyGroupClick: licenseKeyGroup => LicenseKeyGroupsActionHelper.openDeleteLicenseAgreementConfirm(dispatch, {licenseKeyGroup})
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LicenseKeyGroupsListEditorView);


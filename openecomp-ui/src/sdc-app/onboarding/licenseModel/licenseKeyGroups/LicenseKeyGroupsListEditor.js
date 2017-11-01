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
import {connect} from 'react-redux';
import i18n from 'nfvo-utils/i18n/i18n.js';
import LicenseKeyGroupsActionHelper from './LicenseKeyGroupsActionHelper.js';
import LicenseKeyGroupsListEditorView, {generateConfirmationMsg} from './LicenseKeyGroupsListEditorView.jsx';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import {actionTypes as globalMoadlActions}  from 'nfvo-components/modal/GlobalModalConstants.js';

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

const mapActionsToProps = (dispatch, {licenseModelId, version}) => {	
	return {
		onAddLicenseKeyGroupClick: () => LicenseKeyGroupsActionHelper.openLicenseKeyGroupsEditor(dispatch),
		onEditLicenseKeyGroupClick: licenseKeyGroup => LicenseKeyGroupsActionHelper.openLicenseKeyGroupsEditor(dispatch, {licenseKeyGroup, licenseModelId, version}),
		onDeleteLicenseKeyGroupClick: licenseKeyGroup => dispatch({
			type: globalMoadlActions.GLOBAL_MODAL_WARNING,
			data:{
				msg: generateConfirmationMsg(licenseKeyGroup),
				confirmationButtonText: i18n('Delete'),
				title: i18n('Delete'),
				onConfirmed: ()=>LicenseKeyGroupsActionHelper.deleteLicenseKeyGroup(dispatch, {licenseModelId, licenseKeyGroupId:licenseKeyGroup.id, version})
			}
		})
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LicenseKeyGroupsListEditorView);


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
import LicenseAgreementActionHelper from './LicenseAgreementActionHelper.js';
import LicenseAgreementListEditorView from './LicenseAgreementListEditorView.jsx';
import {actionTypes as globalMoadlActions}  from 'nfvo-components/modal/GlobalModalConstants.js';

const mapStateToProps = ({licenseModel: {licenseAgreement, licenseModelEditor}}) => {

	let {licenseAgreementList} = licenseAgreement;
	let {data} = licenseAgreement.licenseAgreementEditor;
	let {vendorName, version} = licenseModelEditor.data;

	return {
		vendorName,
		version,
		licenseAgreementList,
		isDisplayModal: Boolean(data),
		isModalInEditMode: Boolean(data && data.id)
	};

};

const mapActionsToProps = (dispatch, {licenseModelId}) => {
	return {
		onAddLicenseAgreementClick: (version) => LicenseAgreementActionHelper.openLicenseAgreementEditor(dispatch, {licenseModelId, version}),
		onEditLicenseAgreementClick: (licenseAgreement, version) => LicenseAgreementActionHelper.openLicenseAgreementEditor(dispatch, {licenseModelId, licenseAgreement, version}),
		onDeleteLicenseAgreement: (licenseAgreement, version) => dispatch({
			type: globalMoadlActions.GLOBAL_MODAL_WARNING,
			data:{
				msg: i18n('Are you sure you want to delete "{name}"?', {name: licenseAgreement.name}),
				confirmationButtonText: i18n('Delete'),
				title: i18n('Delete'),
				onConfirmed: ()=>LicenseAgreementActionHelper.deleteLicenseAgreement(dispatch, {licenseModelId, licenseAgreementId: licenseAgreement.id, version})
			}
		})
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LicenseAgreementListEditorView);

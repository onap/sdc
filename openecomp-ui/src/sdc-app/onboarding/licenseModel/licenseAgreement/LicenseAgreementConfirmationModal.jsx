import React from 'react';
import {connect} from 'react-redux';
import ConfirmationModalView from 'nfvo-components/confirmations/ConfirmationModalView.jsx';
import LicenseAgreementActionHelper from './LicenseAgreementActionHelper.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

function renderMsg(licenseAgreementToDelete) {
	let name = licenseAgreementToDelete ? licenseAgreementToDelete.name : '';
	let msg = i18n('Are you sure you want to delete "{name}"?', {name});
	return(
		<div>
			<p>{msg}</p>
		</div>
	);
};

const mapStateToProps = ({licenseModel: {licenseAgreement}}, {licenseModelId}) => {
	let {licenseAgreementToDelete} = licenseAgreement;
	const show = licenseAgreementToDelete !== false;
	return {
		show,
		title: 'Warning!',
		type: 'warning',
		msg: renderMsg(licenseAgreementToDelete),
		confirmationDetails: {licenseAgreementToDelete, licenseModelId}
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onConfirmed: ({licenseAgreementToDelete, licenseModelId}) => {

			LicenseAgreementActionHelper.deleteLicenseAgreement(dispatch, {licenseModelId, licenseAgreementId: licenseAgreementToDelete.id});
			LicenseAgreementActionHelper.hideDeleteConfirm(dispatch);
		},
		onDeclined: () => {
			LicenseAgreementActionHelper.hideDeleteConfirm(dispatch);
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(ConfirmationModalView);


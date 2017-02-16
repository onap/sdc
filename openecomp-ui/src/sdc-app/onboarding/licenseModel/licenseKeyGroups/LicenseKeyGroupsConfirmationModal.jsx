import React from 'react';
import {connect} from 'react-redux';
import ConfirmationModalView from 'nfvo-components/confirmations/ConfirmationModalView.jsx';
import LicenseKeyGroupsActionHelper from './LicenseKeyGroupsActionHelper.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

function renderMsg(licenseKeyGroupToDelete) {
	let name = licenseKeyGroupToDelete ? licenseKeyGroupToDelete.name : '';
	let msg = i18n('Are you sure you want to delete "{name}"?', {name});
	let subMsg =  licenseKeyGroupToDelete
					&& licenseKeyGroupToDelete.referencingFeatureGroups
					&& licenseKeyGroupToDelete.referencingFeatureGroups.length > 0 ?
					i18n('This license key group is associated with one or more feature groups') :
					'';
	return(
		<div>
			<p>{msg}</p>
			<p>{subMsg}</p>
		</div>
	);
};

const mapStateToProps = ({licenseModel: {licenseKeyGroup}}, {licenseModelId}) => {
	let {licenseKeyGroupToDelete} = licenseKeyGroup;
	const show = licenseKeyGroupToDelete !== false;
	return {
		show,
		title: 'Warning!',
		type: 'warning',
		msg: renderMsg(licenseKeyGroupToDelete),
		confirmationDetails: {licenseKeyGroupToDelete, licenseModelId}
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onConfirmed: ({licenseKeyGroupToDelete, licenseModelId}) => {

			LicenseKeyGroupsActionHelper.deleteLicenseKeyGroup(dispatch, {licenseModelId, licenseKeyGroupId:licenseKeyGroupToDelete.id});
			LicenseKeyGroupsActionHelper.hideDeleteConfirm(dispatch);
		},
		onDeclined: () => {
			LicenseKeyGroupsActionHelper.hideDeleteConfirm(dispatch);
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(ConfirmationModalView);


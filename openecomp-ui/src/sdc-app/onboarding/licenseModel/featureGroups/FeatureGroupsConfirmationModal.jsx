import React from 'react';
import {connect} from 'react-redux';
import ConfirmationModalView from 'nfvo-components/confirmations/ConfirmationModalView.jsx';
import FeatureGroupsActionHelper from './FeatureGroupsActionHelper.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

function renderMsg(featureGroupToDelete) {
	let name = featureGroupToDelete ? featureGroupToDelete.name : '';
	let msg = i18n('Are you sure you want to delete "{name}"?', {name});
	let subMsg = featureGroupToDelete
	&& featureGroupToDelete.referencingLicenseAgreements
	&& featureGroupToDelete.referencingLicenseAgreements.length > 0 ?
		i18n('This feature group is associated with one ore more license agreements') :
		'';
	return (
		<div>
			<p>{msg}</p>
			<p>{subMsg}</p>
		</div>
	);
};

const mapStateToProps = ({licenseModel: {featureGroup}}, {licenseModelId}) => {
	let {featureGroupToDelete} = featureGroup;
	const show = featureGroupToDelete !== false;
	return {
		show,
		title: 'Warning!',
		type: 'warning',
		msg: renderMsg(featureGroupToDelete),
		confirmationDetails: {featureGroupToDelete, licenseModelId}
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onConfirmed: ({featureGroupToDelete, licenseModelId}) => {
			FeatureGroupsActionHelper.deleteFeatureGroup(dispatch, {featureGroupId: featureGroupToDelete.id, licenseModelId});
			FeatureGroupsActionHelper.hideDeleteConfirm(dispatch);
		},
		onDeclined: () => {
			FeatureGroupsActionHelper.hideDeleteConfirm(dispatch);
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(ConfirmationModalView);


import React from 'react';
import {connect} from 'react-redux';
import ConfirmationModalView from 'nfvo-components/confirmations/ConfirmationModalView.jsx';
import EntitlementPoolsActionHelper from './EntitlementPoolsActionHelper.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

function renderMsg(entitlementPoolToDelete) {
	let poolName = entitlementPoolToDelete ? entitlementPoolToDelete.name : '';
	let msg = i18n('Are you sure you want to delete "{poolName}"?', {poolName});
	let subMsg = entitlementPoolToDelete
	&& entitlementPoolToDelete.referencingFeatureGroups
	&& entitlementPoolToDelete.referencingFeatureGroups.length > 0 ?
		i18n('This entitlement pool is associated with one or more feature groups') :
		'';
	return (
		<div>
			<p>{msg}</p>
			<p>{subMsg}</p>
		</div>
	);
};

const mapStateToProps = ({licenseModel: {entitlementPool}}, {licenseModelId}) => {
	let {entitlementPoolToDelete} = entitlementPool;
	const show = entitlementPoolToDelete !== false;
	return {
		show,
		title: 'Warning!',
		type: 'warning',
		msg: renderMsg(entitlementPoolToDelete),
		confirmationDetails: {entitlementPoolToDelete, licenseModelId}
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onConfirmed: ({entitlementPoolToDelete, licenseModelId}) => {
			EntitlementPoolsActionHelper.deleteEntitlementPool(dispatch, {
				licenseModelId,
				entitlementPoolId: entitlementPoolToDelete.id
			});
			EntitlementPoolsActionHelper.hideDeleteConfirm(dispatch);
		},
		onDeclined: () => {
			EntitlementPoolsActionHelper.hideDeleteConfirm(dispatch);
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(ConfirmationModalView);


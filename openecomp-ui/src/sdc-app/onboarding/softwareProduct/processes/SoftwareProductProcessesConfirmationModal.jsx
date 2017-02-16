import React from 'react';
import {connect} from 'react-redux';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ConfirmationModalView from 'nfvo-components/confirmations/ConfirmationModalView.jsx';
import SoftwareProductProcessesActionHelper from './SoftwareProductProcessesActionHelper.js';

function renderMsg(processToDelete) {
	let name = processToDelete ? processToDelete.name : '';
	let msg = i18n('Are you sure you want to delete "{name}"?', {name});
	return (
		<div>
			<p>{msg}</p>
		</div>
	);
};

const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductEditor, softwareProductProcesses} = softwareProduct;
	let {processToDelete} = softwareProductProcesses;
	let softwareProductId = softwareProductEditor.data.id;

	const show = processToDelete !== false;
	return {
		show,
		title: i18n('Warning!'),
		type: 'warning',
		msg: renderMsg(processToDelete),
		confirmationDetails: {processToDelete, softwareProductId}
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onConfirmed: ({processToDelete, softwareProductId}) => {
			SoftwareProductProcessesActionHelper.deleteProcess(dispatch, {process: processToDelete, softwareProductId});
			SoftwareProductProcessesActionHelper.hideDeleteConfirm(dispatch);
		},
		onDeclined: () => {
			SoftwareProductProcessesActionHelper.hideDeleteConfirm(dispatch);
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(ConfirmationModalView);


import React from 'react';
import {connect} from 'react-redux';
import ConfirmationModalView from 'nfvo-components/confirmations/ConfirmationModalView.jsx';
import SoftwareProductComponentProcessesActionHelper from './SoftwareProductComponentProcessesActionHelper.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

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
	let {softwareProductEditor, softwareProductComponents} = softwareProduct;
	let {componentProcesses} = softwareProductComponents;
	let {processToDelete} = componentProcesses;
	let softwareProductId = softwareProductEditor.data.id;
	const show = processToDelete !== false;
	return {
		show,
		title: 'Warning!',
		type: 'warning',
		msg: renderMsg(processToDelete),
		confirmationDetails: {processToDelete, softwareProductId}
	};
};

const mapActionsToProps = (dispatch,{componentId, softwareProductId}) => {
	return {
		onConfirmed: ({processToDelete}) => {
			SoftwareProductComponentProcessesActionHelper.deleteProcess(dispatch, {process: processToDelete, softwareProductId, componentId});
			SoftwareProductComponentProcessesActionHelper.hideDeleteConfirm(dispatch);
		},
		onDeclined: () => {
			SoftwareProductComponentProcessesActionHelper.hideDeleteConfirm(dispatch);
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(ConfirmationModalView);


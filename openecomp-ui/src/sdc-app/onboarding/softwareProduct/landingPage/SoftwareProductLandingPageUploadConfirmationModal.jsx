import {connect} from 'react-redux';
import ConfirmationModalView from 'nfvo-components/confirmations/ConfirmationModalView.jsx';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';

import i18n from 'nfvo-utils/i18n/i18n.js';

const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductEditor} = softwareProduct;
	let {uploadData} = softwareProductEditor;
	const show = uploadData ? true : false;
	return {
		show,
		title: 'Warning!',
		type: 'warning',
		msg: i18n('Upload will erase existing data. Do you want to continue?'),
		confirmationDetails: {uploadData}
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onConfirmed: ({uploadData}) => {
			let {softwareProductId, formData, failedNotificationTitle} = uploadData;
			SoftwareProductActionHelper.uploadFile(dispatch, {
				softwareProductId,
				formData,
				failedNotificationTitle
			});
			SoftwareProductActionHelper.hideUploadConfirm(dispatch);
		},
		onDeclined: () => {
			SoftwareProductActionHelper.hideUploadConfirm(dispatch);
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(ConfirmationModalView);


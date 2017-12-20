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
import SoftwareProductComponentsMonitoringView from './SoftwareProductComponentsMonitoringView.jsx';
import SoftwareProductComponentsMonitoringAction from './SoftwareProductComponentsMonitoringActionHelper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';



export const mapStateToProps = ({softwareProduct}) => {

	let {softwareProductComponents: {monitoring}} = softwareProduct;
	return {
		filenames: monitoring
	};

};

const mapActionsToProps = (dispatch, {softwareProductId, version, componentId}) => {

	return {
		onDropMibFileToUpload: (formData, type) =>
			SoftwareProductComponentsMonitoringAction.uploadFile(dispatch, {
				softwareProductId,
				version,
				componentId,
				formData,
				type
			}),

		onDeleteFile: type => SoftwareProductComponentsMonitoringAction.deleteFile(dispatch, {
			softwareProductId,
			version,
			componentId,
			type
		}),

		onFileUploadError: () => dispatch({
			type: modalActionTypes.GLOBAL_MODAL_ERROR,
			data: {
				title: i18n('Upload Failed'),
				msg: i18n('Expected "zip" file. Please check the provided file type.')
			}
		})
	};

};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductComponentsMonitoringView);

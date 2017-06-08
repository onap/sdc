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
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import {mapStateToProps as attachmentsMapStateToProps, mapActionsToProps as attachmentsMapActionsToProps} from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachments.js';
// import AttachmentsView from './AttachmentsView.jsx';
import AttachmentsView from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachmentsView.jsx';
import UploadScreenActionHelper from './UploadScreenActionHelper.js';
import HeatSetup from './HeatSetup';

export const mapStateToProps = (state) => {
	let original = attachmentsMapStateToProps(state);
	return {
		...original,
		HeatSetupComponent: HeatSetup,
		isReadOnlyMode: false
	};
};

const mapActionsToProps = (dispatch, {softwareProductId}) => {
	let original = attachmentsMapActionsToProps(dispatch, {softwareProductId});
	return {
		...original,
		onDownload: heatCandidate => UploadScreenActionHelper.downloadHeatFile(dispatch, heatCandidate),
		onUpload: formData => UploadScreenActionHelper.uploadFile(dispatch, formData),
		onSave: (heatCandidate) => SoftwareProductActionHelper.updateSoftwareProductHeatCandidate(dispatch, {softwareProductId, heatCandidate}),
		onProcessAndValidate: (heatData, heatDataCache) => UploadScreenActionHelper.processAndValidateHeat(dispatch, heatData, heatDataCache)
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(AttachmentsView);

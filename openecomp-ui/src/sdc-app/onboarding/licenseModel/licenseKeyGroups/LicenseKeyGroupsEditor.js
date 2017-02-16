/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import {connect} from 'react-redux';
import LicenseKeyGroupsActionHelper from './LicenseKeyGroupsActionHelper.js';
import LicenseKeyGroupsEditorView from './LicenseKeyGroupsEditorView.jsx';

const mapStateToProps = ({licenseModel: {licenseKeyGroup}}) => {


	let {data} = licenseKeyGroup.licenseKeyGroupsEditor;

	let previousData;
	const licenseKeyGroupId = data ? data.id : null;
	if(licenseKeyGroupId) {
		previousData = licenseKeyGroup.licenseKeyGroupsList.find(licenseKeyGroup => licenseKeyGroup.id === licenseKeyGroupId);
	}

	return {
		data,
		previousData
	};
};

const mapActionsToProps = (dispatch, {licenseModelId}) => {
	return {
		onDataChanged: deltaData => LicenseKeyGroupsActionHelper.licenseKeyGroupEditorDataChanged(dispatch, {deltaData}),
		onCancel: () => LicenseKeyGroupsActionHelper.closeLicenseKeyGroupEditor(dispatch),
		onSubmit: ({previousLicenseKeyGroup, licenseKeyGroup}) => {
			LicenseKeyGroupsActionHelper.closeLicenseKeyGroupEditor(dispatch);
			LicenseKeyGroupsActionHelper.saveLicenseKeyGroup(dispatch, {licenseModelId, previousLicenseKeyGroup, licenseKeyGroup});
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LicenseKeyGroupsEditorView);

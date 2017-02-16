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
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import SoftwareProductComponentsMonitoringView from './SoftwareProductComponentsMonitoringView.jsx';
import SoftwareProductComponentsMonitoringAction from './SoftwareProductComponentsMonitoringActionHelper.js';


export const mapStateToProps = ({softwareProduct}) => {

	let {softwareProductEditor: {data:currentVSP = {}}, softwareProductComponents: {monitoring}} = softwareProduct;
	let {trapFilename, pollFilename} = monitoring;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(currentVSP);

	return {
		isReadOnlyMode,
		trapFilename,
		pollFilename
	};
};

const mapActionsToProps = (dispatch, {softwareProductId, componentId}) => {
	return {
		onDropMibFileToUpload: (formData, type) =>
			SoftwareProductComponentsMonitoringAction.uploadSnmpFile(dispatch, {
				softwareProductId,
				componentId,
				formData,
				type
			}),

		onDeleteSnmpFile: type => SoftwareProductComponentsMonitoringAction.deleteSnmpFile(dispatch, {
			softwareProductId,
			componentId,
			type
		})

	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductComponentsMonitoringView);

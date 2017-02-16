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

import SoftwareProductProcessesActionHelper from './SoftwareProductProcessesActionHelper.js';
import SoftwareProductProcessesView from './SoftwareProductProcessesView.jsx';

const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductEditor: {data: currentSoftwareProduct = {}}, softwareProductProcesses: {processesList, processesEditor}} = softwareProduct;
	let isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);
	let {data} = processesEditor;

	return {
		currentSoftwareProduct,
		processesList,
		isDisplayEditor: Boolean(data),
		isModalInEditMode: Boolean(data && data.id),
		isReadOnlyMode
	};
};

const mapActionsToProps = (dispatch, {softwareProductId}) => {
	return {
		onAddProcess: () => SoftwareProductProcessesActionHelper.openEditor(dispatch),
		onEditProcess: (process) => SoftwareProductProcessesActionHelper.openEditor(dispatch, process),
		onDeleteProcess: (process) => SoftwareProductProcessesActionHelper.openDeleteProcessesConfirm(dispatch, {process, softwareProductId})
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductProcessesView);

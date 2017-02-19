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
import SoftwareProductComponentProcessesActionHelper from './SoftwareProductComponentProcessesActionHelper';
import SoftwareProductComponentProcessesEditorView from './SoftwareProductComponentProcessesEditorView.jsx';

const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductComponents: {componentProcesses = {}}} = softwareProduct;
	let {processesList = [], processesEditor = {}} = componentProcesses;
	let {data} = processesEditor;

	let previousData;
	const processId = data ? data.id : null;
	if(processId) {
		previousData = processesList.find(process => process.id === processId);
	}

	return {
		data,
		previousData
	};
};

const mapActionsToProps = (dispatch, {softwareProductId, componentId}) => {

	return {
		onDataChanged: deltaData => SoftwareProductComponentProcessesActionHelper.processEditorDataChanged(dispatch, {deltaData}),
		onCancel: () => SoftwareProductComponentProcessesActionHelper.closeEditor(dispatch),
		onSubmit: ({previousProcess, process}) => {
			SoftwareProductComponentProcessesActionHelper.closeEditor(dispatch);
			SoftwareProductComponentProcessesActionHelper.saveProcess(dispatch, {softwareProductId, previousProcess, componentId, process});
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(SoftwareProductComponentProcessesEditorView);

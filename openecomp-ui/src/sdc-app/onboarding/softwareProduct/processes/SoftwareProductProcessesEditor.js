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
import SoftwareProductProcessesActionHelper from './SoftwareProductProcessesActionHelper';
import SoftwareProductProcessesEditorView from './SoftwareProductProcessesEditorView.jsx';

const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductProcesses: {processesList, processesEditor}} = softwareProduct;
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

const mapActionsToProps = (dispatch, {softwareProductId}) => {
	return {
		onDataChanged: deltaData => SoftwareProductProcessesActionHelper.processEditorDataChanged(dispatch, {deltaData}),
		onSubmit: ({previousProcess, process}) => {
			SoftwareProductProcessesActionHelper.closeEditor(dispatch);
			SoftwareProductProcessesActionHelper.saveProcess(dispatch, {softwareProductId, previousProcess, process});
		},
		onClose: () => SoftwareProductProcessesActionHelper.closeEditor(dispatch)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(SoftwareProductProcessesEditorView);

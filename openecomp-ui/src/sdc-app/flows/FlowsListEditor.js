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
import FlowsActions from './FlowsActions.js';
import FlowsListEditorView from './FlowsListEditorView.jsx';

export const mapStateToProps = ({flows}) => {
	let {flowList = [], isDisplayModal, isModalInEditMode, shouldShowWorkflowsEditor = true, currentFlow = undefined} = flows;
	let isCheckedOut = currentFlow ? !currentFlow.readonly : true;

	return {
		flowList,
		isDisplayModal,
		isCheckedOut,
		isModalInEditMode,
		shouldShowWorkflowsEditor,
		currentFlow
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onAddWorkflowClick: () => FlowsActions.openFlowDetailsEditor(dispatch),
		onEditFlowDetailsClick: flow => FlowsActions.openFlowDetailsEditor(dispatch, flow),
		onEditFlowDiagramClick: flow => FlowsActions.fetchArtifact(dispatch, {flow}),
		onDeleteFlowClick: flow => FlowsActions.deleteFlow(dispatch, {flow}),
		onSequenceDiagramSaveClick: flow => {
			FlowsActions.closeFlowDiagramEditor(dispatch);
			FlowsActions.createOrUpdateFlow(dispatch, {flow});
		},
		onSequenceDiagramCloseClick: () => FlowsActions.closeFlowDiagramEditor(dispatch)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(FlowsListEditorView);

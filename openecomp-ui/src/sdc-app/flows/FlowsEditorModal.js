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
import FlowsEditorModalView from './FlowsEditorModalView.jsx';
import FlowsActions from './FlowsActions.js';

export const mapStateToProps = ({flows}) => {

	let {currentFlow = {artifactName: '', description: ''}, serviceID, diagramType, flowParticipants} = flows;
	if(!currentFlow.serviceID){
		currentFlow.serviceID = serviceID;
	}
	if(!currentFlow.artifactType){
		currentFlow.artifactType = diagramType;
	}
	if(!currentFlow.participants){
		currentFlow.participants = flowParticipants;
	}

	return {
		currentFlow
	};
};

const mapActionsToProps = (dispatch, {isNewArtifact}) => {
	return {
		onSubmit: flow => {
			FlowsActions.closeFlowDetailsEditor(dispatch);
			FlowsActions.createOrUpdateFlow(dispatch, {flow}, isNewArtifact);
		},
		onCancel: () => FlowsActions.closeFlowDetailsEditor(dispatch),
		onDataChanged: deltaData => FlowsActions.flowDetailsDataChanged(dispatch, {deltaData})
	};
};

export default connect(mapStateToProps, mapActionsToProps)(FlowsEditorModalView);

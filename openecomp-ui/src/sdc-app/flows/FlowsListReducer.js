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

import {actionTypes} from './FlowsConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.FLOW_LIST_LOADED:
			return {
				...state,
				flowList: action.results,
				flowParticipants: action.participants,
				serviceID: action.serviceID,
				diagramType: action.diagramType
			};
		case actionTypes.ADD_OR_UPDATE_FLOW:
		case actionTypes.ARTIFACT_LOADED:
			let flowList = state.flowList || [];
			let index = flowList.findIndex(flow => flow.uniqueId === action.flow.uniqueId);
			if (index === -1) {
				index = flowList.length;
			}
			let flowToBeUpdated = flowList[index];
			flowList = [
				...flowList.slice(0, index),
				{...flowToBeUpdated, ...action.flow},
				...flowList.slice(index + 1)
			];
			return {
				...state,
				flowList,
				serviceID: action.flow.serviceID,
				diagramType: action.flow.artifactType || state.diagramType
			};
		case actionTypes.CURRENT_FLOW_DATA_CHANGED:
			return {
				...state,
				currentFlow: {
					...state.currentFlow,
					...action.deltaData
				}
			};
		case actionTypes.DELETE_FLOW:
			return {
				...state,
				flowList: state.flowList.filter(flow => flow.uniqueId !== action.flow.uniqueId)
			};
		case actionTypes.OPEN_FLOW_DETAILS_EDITOR:
			return {
				...state,
				currentFlow: action.flow,
				isDisplayModal: true,
				isModalInEditMode: Boolean(action.flow && action.flow.uniqueId)
			};

		case actionTypes.CLOSE_FLOW_DETAILS_EDITOR:
			return {
				...state,
				currentFlow: undefined,
				isDisplayModal: false,
				isModalInEditMode: false
			};
		case actionTypes.OPEN_FLOW_DIAGRAM_EDITOR:
			return {
				...state,
				currentFlow: action.flow,
				shouldShowWorkflowsEditor: false
			};
		case actionTypes.CLOSE_FLOW_DIAGRAM_EDITOR:
			return {
				...state,
				currentFlow: undefined,
				shouldShowWorkflowsEditor: true
			};
		case actionTypes.RESET:
			return {};
	}

	return state;
};

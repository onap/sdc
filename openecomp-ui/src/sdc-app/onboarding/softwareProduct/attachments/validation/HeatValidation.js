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
import HeatValidationView from './HeatValidationView.jsx';
import HeatValidationActionHelper from './HeatValidationActionHelper.js';
import {errorLevels, nodeFilters} from './HeatValidationConstants.js';

export const mapStateToProps = ({softwareProduct: {softwareProductAttachments: {heatValidation}}}) => {
	let {attachmentsTree, selectedNode, errorList} = heatValidation;
	let currentErrors = [], currentWarnings = [];
	if (errorList) {
		for (let i = 0 ; i < errorList.length ; i++) {
			if (errorList[i].level === errorLevels.ERROR && (errorList[i].name === selectedNode || selectedNode === nodeFilters.ALL)) {
				currentErrors[currentErrors.length] = errorList[i];
			}
			if (errorList[i].level === errorLevels.WARNING  && (errorList[i].name === selectedNode || selectedNode === nodeFilters.ALL)) {
				currentWarnings[currentWarnings.length] = errorList[i];
			}
		}
	}
	return {
		attachmentsTree,
		selectedNode,
		errorList,
		currentErrors,
		currentWarnings
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		toggleExpanded: (path) => HeatValidationActionHelper.toggleExpanded(dispatch, {path}),
		onSelectNode: (nodeName) => HeatValidationActionHelper.onSelectNode(dispatch, {nodeName}),
		onDeselectNode: () => HeatValidationActionHelper.onDeselectNode(dispatch)
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(HeatValidationView);

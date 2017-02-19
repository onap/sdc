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
import SoftwareProductAttachmentsView from './SoftwareProductAttachmentsView.jsx';
import SoftwareProductAttachmentsActionHelper from './SoftwareProductAttachmentsActionHelper.js';

export const mapStateToProps = ({softwareProduct: {softwareProductAttachments}}) => {
	let {attachmentsTree, hoveredNode, selectedNode, errorList} = softwareProductAttachments;
	return {
		attachmentsTree,
		hoveredNode,
		selectedNode,
		errorList
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		toggleExpanded: (path) => SoftwareProductAttachmentsActionHelper.toggleExpanded(dispatch, {path}),
		onSelectNode: (nodeName) => SoftwareProductAttachmentsActionHelper.onSelectNode(dispatch, {nodeName}),
		onUnselectNode: () => SoftwareProductAttachmentsActionHelper.onUnselectNode(dispatch)
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductAttachmentsView);

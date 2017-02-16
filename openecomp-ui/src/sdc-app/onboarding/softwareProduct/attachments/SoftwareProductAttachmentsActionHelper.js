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

import {actionTypes} from './SoftwareProductAttachmentsConstants.js';

export default {

	toggleExpanded(dispatch, {path}) {
		dispatch({
			type: actionTypes.TOGGLE_EXPANDED,
			path
		});
	},

	onSelectNode(dispatch, {nodeName}) {
		dispatch({
			type: actionTypes.SELECTED_NODE,
			nodeName
		});
	},

	onUnselectNode(dispatch) {
		dispatch({
			type: actionTypes.UNSELECTED_NODE
		});
	}
};

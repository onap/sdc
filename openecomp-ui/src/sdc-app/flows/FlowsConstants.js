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

import keyMirror from 'nfvo-utils/KeyMirror.js';

export const actionTypes = keyMirror({

	OPEN_FLOW_DETAILS_EDITOR: null,
	CLOSE_FLOW_DETAILS_EDITOR: null,

	OPEN_FLOW_DIAGRAM_EDITOR: null,
	CLOSE_FLOW_DIAGRAM_EDITOR: null,

	FLOW_LIST_LOADED: null,
	ADD_OR_UPDATE_FLOW: null,
	ARTIFACT_LOADED: null,
	DELETE_FLOW: null,

	CURRENT_FLOW_DATA_CHANGED: null,

	RESET: null

});

export const enums = {
	WORKFLOW: 'WORKFLOW',
	NETWORK: 'NETWORK_CALL_FLOW',
	INFORMATIONAL: 'INFORMATIONAL',
	INSTANTIATION_FLOWS: 'instantiationflows',
	MESSAGE_FLOWS: 'messageflows'
};

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

import {actionTypes} from './SoftwareProductComponentsNetworkConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.NICEditor.OPEN:
			return {
				...state,
				data: action.nic
			};
		case actionTypes.NICEditor.CLOSE:
			return {};
		case actionTypes.NICEditor.NIC_QUESTIONNAIRE_UPDATE:
			return {
				...state,
				qdata: action.payload.qdata || state.qdata,
				qschema: action.payload.qschema || state.qschema
			};
		case actionTypes.NICEditor.DATA_CHANGED:
			return {
				...state,
				data: {
					...state.data,
					...action.deltaData
				}
			};
		default:
			return state;
	}
};

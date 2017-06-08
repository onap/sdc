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
import {actionTypes} from './SoftwareProductComponentsMonitoringConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.SNMP_FILES_DATA_CHANGE:
			return {
				...state,
				trapFilename: action.data.trapFilename,
				pollFilename: action.data.pollFilename
			};
		case actionTypes.SNMP_TRAP_UPLOADED:
			return {
				...state,
				trapFilename: action.data.filename
			};
		case actionTypes.SNMP_POLL_UPLOADED:
			return {
				...state,
				pollFilename: action.data.filename
			};
		case actionTypes.SNMP_TRAP_DELETED:
			return {
				...state,
				trapFilename: undefined
			};
		case actionTypes.SNMP_POLL_DELETED:
			return {
				...state,
				pollFilename: undefined
			};
		default:
			return state;
	}
};

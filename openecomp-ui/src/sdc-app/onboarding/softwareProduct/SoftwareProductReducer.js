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

import {combineReducers} from 'redux';
import {actionTypes} from './SoftwareProductConstants.js';
import SoftwareProductAttachmentsReducer from './attachments/SoftwareProductAttachmentsReducer.js';
import SoftwareProductCreationReducer from './creation/SoftwareProductCreationReducer.js';
import SoftwareProductDetailsReducer from './details/SoftwareProductDetailsReducer.js';
import SoftwareProductProcessesListReducer from './processes/SoftwareProductProcessesListReducer.js';
import SoftwareProductProcessesEditorReducer from './processes/SoftwareProductProcessesEditorReducer.js';
import SoftwareProductNetworksListReducer from './networks/SoftwareProductNetworksListReducer.js';
import SoftwareProductComponentsListReducer from './components/SoftwareProductComponentsListReducer.js';
import SoftwareProductComponentEditorReducer from './components/SoftwareProductComponentEditorReducer.js';
import  {actionTypes as processesActionTypes} from './processes/SoftwareProductProcessesConstants.js';
import SoftwareProductComponentProcessesListReducer  from './components/processes/SoftwareProductComponentProcessesListReducer.js';
import SoftwareProductComponentProcessesEditorReducer from './components/processes/SoftwareProductComponentProcessesEditorReducer.js';
import  {actionTypes as componentProcessesActionTypes} from './components/processes/SoftwareProductComponentProcessesConstants.js';
import SoftwareProductComponentsNICListReducer from './components/network/SoftwareProductComponentsNICListReducer.js';
import SoftwareProductComponentsNICEditorReducer from './components/network/SoftwareProductComponentsNICEditorReducer.js';
import SoftwareProductComponentsMonitoringReducer from './components/monitoring/SoftwareProductComponentsMonitoringReducer.js';

export default combineReducers({
	softwareProductAttachments: SoftwareProductAttachmentsReducer,
	softwareProductCreation: SoftwareProductCreationReducer,
	softwareProductEditor: SoftwareProductDetailsReducer,
	softwareProductProcesses: combineReducers({
		processesList: SoftwareProductProcessesListReducer,
		processesEditor: SoftwareProductProcessesEditorReducer,
		processToDelete: (state = false, action) => action.type === processesActionTypes.SOFTWARE_PRODUCT_PROCESS_DELETE_CONFIRM ? action.processToDelete : state
	}),
	softwareProductNetworks: combineReducers({
		networksList: SoftwareProductNetworksListReducer
	}),
	softwareProductComponents: combineReducers({
		componentsList: SoftwareProductComponentsListReducer,
		componentEditor: SoftwareProductComponentEditorReducer,
		componentProcesses: combineReducers({
			processesList: SoftwareProductComponentProcessesListReducer,
			processesEditor: SoftwareProductComponentProcessesEditorReducer,
			processToDelete: (state = false, action) => action.type === componentProcessesActionTypes.SOFTWARE_PRODUCT_PROCESS_DELETE_COMPONENTS_CONFIRM ? action.processToDelete : state,
		}),
		network: combineReducers({
			nicList: SoftwareProductComponentsNICListReducer,
			nicEditor: SoftwareProductComponentsNICEditorReducer
		}),
		monitoring: SoftwareProductComponentsMonitoringReducer
	}),
	softwareProductCategories: (state = [], action) => {
		if (action.type === actionTypes.SOFTWARE_PRODUCT_CATEGORIES_LOADED) {
			return action.softwareProductCategories;
		}
		return state;
	},
	softwareProductQuestionnaire: (state = {}, action) => {
		if (action.type === actionTypes.SOFTWARE_PRODUCT_QUESTIONNAIRE_UPDATE) {
			return {
				...state,
				...action.payload
			};
		}
		return state;
	}
});

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
import {combineReducers} from 'redux';
import {actionTypes, PRODUCT_QUESTIONNAIRE} from './SoftwareProductConstants.js';
import SoftwareProductAttachmentsReducer from './attachments/SoftwareProductAttachmentsReducer.js';
import HeatValidationReducer from './attachments/validation/HeatValidationReducer.js';
import HeatSetupReducer from './attachments/setup/HeatSetupReducer.js';
import {actionTypes as heatSetupActionTypes} from './attachments/setup/HeatSetupConstants.js';
import SoftwareProductCreationReducer from './creation/SoftwareProductCreationReducer.js';
import SoftwareProductDetailsReducer from './details/SoftwareProductDetailsReducer.js';
import SoftwareProductProcessesListReducer from './processes/SoftwareProductProcessesListReducer.js';
import SoftwareProductProcessesEditorReducer from './processes/SoftwareProductProcessesEditorReducer.js';
import SoftwareProductDeploymentListReducer from './deployment/SoftwareProductDeploymentListReducer.js';
import SoftwareProductDeploymentEditorReducer from './deployment/editor/SoftwareProductDeploymentEditorReducer.js';
import SoftwareProductNetworksListReducer from './networks/SoftwareProductNetworksListReducer.js';
import SoftwareProductComponentsListReducer from './components/SoftwareProductComponentsListReducer.js';
import SoftwareProductComponentEditorReducer from './components/SoftwareProductComponentEditorReducer.js';
import  {actionTypes as processesActionTypes} from './processes/SoftwareProductProcessesConstants.js';
import SoftwareProductComponentProcessesListReducer  from './components/processes/SoftwareProductComponentProcessesListReducer.js';
import SoftwareProductComponentProcessesEditorReducer from './components/processes/SoftwareProductComponentProcessesEditorReducer.js';
import  {actionTypes as componentProcessesActionTypes} from './components/processes/SoftwareProductComponentProcessesConstants.js';
import SoftwareProductComponentsNICListReducer from './components/network/SoftwareProductComponentsNICListReducer.js';
import SoftwareProductComponentsNICEditorReducer from './components/network/SoftwareProductComponentsNICEditorReducer.js';
import SoftwareProductComponentsImageListReducer from './components/images/SoftwareProductComponentsImageListReducer.js';
import SoftwareProductComponentsImageEditorReducer from './components/images/SoftwareProductComponentsImageEditorReducer.js';
import SoftwareProductComponentsNICCreationReducer from './components/network/NICCreation/NICCreationReducer.js';
import SoftwareProductComponentsMonitoringReducer from './components/monitoring/SoftwareProductComponentsMonitoringReducer.js';
import SoftwareProductComponentsComputeFlavorListReducer from './components/compute/computeComponents/computeFlavor/ComputeFlavorListReducer.js';
import SoftwareProductComponentsComputeFlavorReducer from './components/compute/computeComponents/computeFlavor/ComputeFlavorReducer.js';
import {createPlainDataReducer} from 'sdc-app/common/reducers/PlainDataReducer.js';
import SoftwareProductDependenciesReducer from './dependencies/SoftwareProductDependenciesReducer.js';
import {createJSONSchemaReducer, createComposedJSONSchemaReducer} from 'sdc-app/common/reducers/JSONSchemaReducer.js';
import {COMPONENTS_QUESTIONNAIRE, COMPONENTS_COMPUTE_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';
import {NIC_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNetworkConstants.js';
import {IMAGE_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/images/SoftwareProductComponentsImageConstants.js';

import VNFImportReducer from './vnfMarketPlace/VNFImportReducer.js';

export default combineReducers({
	softwareProductAttachments: combineReducers({
		attachmentsDetails: SoftwareProductAttachmentsReducer,
		heatValidation: HeatValidationReducer,
		heatSetup: HeatSetupReducer,
		heatSetupCache: (state = {}, action) => action.type === heatSetupActionTypes.FILL_HEAT_SETUP_CACHE ? action.payload : state
	}),
	softwareProductCreation: createPlainDataReducer(SoftwareProductCreationReducer),
	softwareProductEditor: createPlainDataReducer(SoftwareProductDetailsReducer),
	softwareProductProcesses: combineReducers({
		processesList: SoftwareProductProcessesListReducer,
		processesEditor: createPlainDataReducer(SoftwareProductProcessesEditorReducer),
		processToDelete: (state = false, action) => action.type === processesActionTypes.SOFTWARE_PRODUCT_PROCESS_DELETE_CONFIRM ? action.processToDelete : state
	}),
	softwareProductDeployment: combineReducers({
		deploymentFlavors: SoftwareProductDeploymentListReducer,
		deploymentFlavorEditor: createPlainDataReducer(SoftwareProductDeploymentEditorReducer)
	}),
	softwareProductNetworks: combineReducers({
		networksList: SoftwareProductNetworksListReducer
	}),
	softwareProductDependencies: SoftwareProductDependenciesReducer,
	softwareProductComponents: combineReducers({
		componentsList: SoftwareProductComponentsListReducer,
		componentEditor: createPlainDataReducer(createComposedJSONSchemaReducer(COMPONENTS_QUESTIONNAIRE, SoftwareProductComponentEditorReducer)),
		componentProcesses: combineReducers({
			processesList: SoftwareProductComponentProcessesListReducer,
			processesEditor: createPlainDataReducer(SoftwareProductComponentProcessesEditorReducer),
			processToDelete: (state = false, action) => action.type === componentProcessesActionTypes.SOFTWARE_PRODUCT_PROCESS_DELETE_COMPONENTS_CONFIRM ? action.processToDelete : state,
		}),
		network: combineReducers({
			nicList: SoftwareProductComponentsNICListReducer,
			nicEditor: createPlainDataReducer(createComposedJSONSchemaReducer(NIC_QUESTIONNAIRE, SoftwareProductComponentsNICEditorReducer)),
			nicCreation: createPlainDataReducer(SoftwareProductComponentsNICCreationReducer)
		}),
		images: combineReducers({
			imagesList: SoftwareProductComponentsImageListReducer,
			imageEditor: createPlainDataReducer(createComposedJSONSchemaReducer(IMAGE_QUESTIONNAIRE, SoftwareProductComponentsImageEditorReducer))
		}),
		computeFlavor: combineReducers({
			computesList: SoftwareProductComponentsComputeFlavorListReducer,
			computeEditor: createPlainDataReducer(createComposedJSONSchemaReducer(COMPONENTS_COMPUTE_QUESTIONNAIRE, SoftwareProductComponentsComputeFlavorReducer)),
		}),
		monitoring: SoftwareProductComponentsMonitoringReducer
	}),
	softwareProductCategories: (state = [], action) => {
		if (action.type === actionTypes.SOFTWARE_PRODUCT_CATEGORIES_LOADED) {
			return action.softwareProductCategories;
		}
		return state;
	},
	softwareProductQuestionnaire: createJSONSchemaReducer(PRODUCT_QUESTIONNAIRE),
	VNFMarketPlaceImport: VNFImportReducer,
});

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

import LicenseModelActionHelper from './licenseModel/LicenseModelActionHelper.js';
import LicenseAgreementActionHelper from './licenseModel/licenseAgreement/LicenseAgreementActionHelper.js';
import FeatureGroupsActionHelper from './licenseModel/featureGroups/FeatureGroupsActionHelper.js';
import LicenseKeyGroupsActionHelper from './licenseModel/licenseKeyGroups/LicenseKeyGroupsActionHelper.js';
import EntitlementPoolsActionHelper from './licenseModel/entitlementPools/EntitlementPoolsActionHelper.js';
import SoftwareProductActionHelper from './softwareProduct/SoftwareProductActionHelper.js';
import SoftwareProductProcessesActionHelper from './softwareProduct/processes/SoftwareProductProcessesActionHelper.js';
import SoftwareProductNetworksActionHelper from './softwareProduct/networks/SoftwareProductNetworksActionHelper.js';
import SoftwareProductComponentsActionHelper from './softwareProduct/components/SoftwareProductComponentsActionHelper.js';
import SoftwareProductComponentProcessesActionHelper from './softwareProduct/components/processes/SoftwareProductComponentProcessesActionHelper.js';
import SoftwareProductComponentsNetworkActionHelper from './softwareProduct/components/network/SoftwareProductComponentsNetworkActionHelper.js';
import SoftwareProductComponentsMonitoringAction from './softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringActionHelper.js';
import {actionTypes, enums} from './OnboardingConstants.js';
import {navigationItems as SoftwareProductNavigationItems, actionTypes as SoftwareProductActionTypes} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import store from 'sdc-app/AppStore.js';

function setCurrentScreen(dispatch, screen, props = {}) {
	dispatch({
		type: actionTypes.SET_CURRENT_SCREEN,
		currentScreen: {
			screen,
			props
		}
	});
}

function  getCurrentLicenseModelVersion(licenseModelId) {
	return store.getState().licenseModelList.find(({id}) => id === licenseModelId).version;
}

export default {

	navigateToOnboardingCatalog(dispatch) {
		LicenseModelActionHelper.fetchLicenseModels(dispatch);
		SoftwareProductActionHelper.fetchSoftwareProductList(dispatch);
		setCurrentScreen(dispatch, enums.SCREEN.ONBOARDING_CATALOG);
	},

	navigateToLicenseAgreements(dispatch, {licenseModelId, version}) {
		if(!version) {
			version = getCurrentLicenseModelVersion(licenseModelId);
		}
		LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {licenseModelId, version});
		LicenseModelActionHelper.fetchLicenseModelById(dispatch, {licenseModelId, version}).then(() => {
			setCurrentScreen(dispatch, enums.SCREEN.LICENSE_AGREEMENTS, {licenseModelId});
		});
	},

	navigateToFeatureGroups(dispatch, {licenseModelId, version}) {
		if(!version) {
			version = getCurrentLicenseModelVersion(licenseModelId);
		}
		FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId, version});
		setCurrentScreen(dispatch, enums.SCREEN.FEATURE_GROUPS, {licenseModelId});
	},

	navigateToEntitlementPools(dispatch, {licenseModelId, version}) {
		if(!version) {
			version = getCurrentLicenseModelVersion(licenseModelId);
		}
		EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {licenseModelId, version});
		setCurrentScreen(dispatch, enums.SCREEN.ENTITLEMENT_POOLS, {licenseModelId});
	},

	navigateToLicenseKeyGroups(dispatch, {licenseModelId, version}) {
		if(!version) {
			version = getCurrentLicenseModelVersion(licenseModelId);
		}
		LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {licenseModelId, version});
		setCurrentScreen(dispatch, enums.SCREEN.LICENSE_KEY_GROUPS, {licenseModelId});
	},

	navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, licenseModelId, version, licensingVersion}) {
		SoftwareProductActionHelper.fetchSoftwareProduct(dispatch, {softwareProductId, version}).then(response => {
			if(!licensingVersion) {
				licensingVersion = response[0].licensingVersion;
			}
			if (!licenseModelId) {
				licenseModelId = response[0].vendorId;
			}

			SoftwareProductActionHelper.loadSoftwareProductDetailsData(dispatch, {licenseModelId, licensingVersion});
			SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(dispatch, {softwareProductId, version});

			setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE, {softwareProductId, licenseModelId, version});
		});
	},

	navigateToSoftwareProductDetails(dispatch, {softwareProductId}) {
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_DETAILS, {softwareProductId});
	},

	navigateToSoftwareProductAttachments(dispatch, {softwareProductId}) {
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS, {softwareProductId});
	},

	navigateToSoftwareProductProcesses(dispatch, {softwareProductId, version}) {
		if (softwareProductId) {
			SoftwareProductProcessesActionHelper.fetchProcessesList(dispatch, {softwareProductId, version});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES, {softwareProductId});
	},

	navigateToSoftwareProductNetworks(dispatch, {softwareProductId, version}) {
		if (softwareProductId) {
			SoftwareProductNetworksActionHelper.fetchNetworksList(dispatch, {softwareProductId, version});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS, {softwareProductId});
	},

	navigateToSoftwareProductComponents(dispatch, {softwareProductId}) {
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS, {softwareProductId});
	},

	navigateToSoftwareProductComponentProcesses(dispatch, {softwareProductId, componentId, version}) {
		if (componentId && softwareProductId) {
			SoftwareProductComponentProcessesActionHelper.fetchProcessesList(dispatch, {componentId, softwareProductId, version});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES, {softwareProductId, componentId});
	},

	navigateToSoftwareProductComponentMonitoring(dispatch, {softwareProductId, componentId}){
		if (componentId && softwareProductId) {
			SoftwareProductComponentsMonitoringAction.fetchExistingFiles(dispatch, {componentId, softwareProductId});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING, {softwareProductId, componentId});
	},

	navigateToComponentStorage(dispatch, {softwareProductId, componentId}) {
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE, {softwareProductId, componentId});
	},

	navigateToComponentCompute(dispatch, {softwareProductId, componentId}) {
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE, {softwareProductId, componentId});
	},

	navigateToComponentNetwork(dispatch, {softwareProductId, componentId, version}) {
		SoftwareProductComponentsNetworkActionHelper.fetchNICsList(dispatch, {softwareProductId, componentId, version});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK, {softwareProductId, componentId});
	},

	navigateToSoftwareProductComponentGeneral(dispatch, {softwareProductId, componentId, version}) {
		if (componentId && softwareProductId) {
			SoftwareProductComponentsActionHelper.fetchSoftwareProductComponent(dispatch, {softwareProductId, vspComponentId: componentId, version});
			SoftwareProductComponentsActionHelper.fetchSoftwareProductComponentQuestionnaire(dispatch, {
				softwareProductId,
				vspComponentId: componentId,
				version
			});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL, {softwareProductId, componentId});
	},

	navigateToSoftwareProductComponentGeneralAndUpdateLeftPanel(dispatch, {softwareProductId, componentId, version}) {
		this.navigateToSoftwareProductComponentGeneral(dispatch, {softwareProductId, componentId, version});
		dispatch({
			type: SoftwareProductActionTypes.TOGGLE_NAVIGATION_ITEM,
			mapOfExpandedIds: {
				[SoftwareProductNavigationItems.COMPONENTS]: true,
				[SoftwareProductNavigationItems.COMPONENTS + '|' + componentId]: true
			}
		});
	},

	navigateToComponentLoadBalancing(dispatch, {softwareProductId, componentId}) {
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING, {softwareProductId, componentId});
	}
};

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
import LicenseModelActionHelper from './licenseModel/LicenseModelActionHelper.js';
import LicenseAgreementActionHelper from './licenseModel/licenseAgreement/LicenseAgreementActionHelper.js';
import FeatureGroupsActionHelper from './licenseModel/featureGroups/FeatureGroupsActionHelper.js';
import LicenseKeyGroupsActionHelper from './licenseModel/licenseKeyGroups/LicenseKeyGroupsActionHelper.js';
import EntitlementPoolsActionHelper from './licenseModel/entitlementPools/EntitlementPoolsActionHelper.js';
import SoftwareProductActionHelper from './softwareProduct/SoftwareProductActionHelper.js';
import SoftwareProductProcessesActionHelper from './softwareProduct/processes/SoftwareProductProcessesActionHelper.js';
import SoftwareProductDeploymentActionHelper from './softwareProduct/deployment/SoftwareProductDeploymentActionHelper.js';
import SoftwareProductNetworksActionHelper from './softwareProduct/networks/SoftwareProductNetworksActionHelper.js';
import SoftwareProductComponentsActionHelper from './softwareProduct/components/SoftwareProductComponentsActionHelper.js';
import SoftwareProductComponentProcessesActionHelper from './softwareProduct/components/processes/SoftwareProductComponentProcessesActionHelper.js';
import SoftwareProductComponentsNetworkActionHelper from './softwareProduct/components/network/SoftwareProductComponentsNetworkActionHelper.js';
import SoftwareProductDependenciesActionHelper from './softwareProduct/dependencies/SoftwareProductDependenciesActionHelper.js';
import ComputeFlavorActionHelper from './softwareProduct/components/compute/ComputeFlavorActionHelper.js';
import OnboardActionHelper from './onboard/OnboardActionHelper.js';
import SoftwareProductComponentsMonitoringAction from './softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringActionHelper.js';
import {actionTypes, enums} from './OnboardingConstants.js';
import SoftwareProductComponentsImageActionHelper from './softwareProduct/components/images/SoftwareProductComponentsImageActionHelper.js';
import {navigationItems as SoftwareProductNavigationItems, actionTypes as SoftwareProductActionTypes, onboardingOriginTypes} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import ActivityLogActionHelper from 'sdc-app/common/activity-log/ActivityLogActionHelper.js';
import licenseModelOverviewActionHelper from 'sdc-app/onboarding/licenseModel/overview/licenseModelOverviewActionHelper.js';
import store from 'sdc-app/AppStore.js';
import {selectedButton as licenseModelOverviewSelectedButton} from 'sdc-app/onboarding/licenseModel/overview/LicenseModelOverviewConstants.js';
import {tabsMapping as attachmentsTabsMapping} from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachmentsConstants.js';
import SoftwareProductAttachmentsActionHelper from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachmentsActionHelper.js';

function setCurrentScreen(dispatch, screen, props = {}) {
	dispatch({
		type: actionTypes.SET_CURRENT_SCREEN,
		currentScreen: {
			screen,
			props,
			forceBreadCrumbsUpdate: true
		}
	});
}

function  getCurrentLicenseModelVersion(licenseModelId) {
	return store.getState().licenseModelList.find(({id}) => id === licenseModelId).version;
}

function getCurrentSoftwareProductVersion(softwareProductId) {
	return store.getState().softwareProductList.find(({id}) => id === softwareProductId).version;
}

export default {

	navigateToOnboardingCatalog(dispatch) {
		LicenseModelActionHelper.fetchLicenseModels(dispatch);
		LicenseModelActionHelper.fetchFinalizedLicenseModels(dispatch);
		SoftwareProductActionHelper.fetchSoftwareProductList(dispatch);
		SoftwareProductActionHelper.fetchFinalizedSoftwareProductList(dispatch);
		OnboardActionHelper.resetOnboardStore(dispatch);
		setCurrentScreen(dispatch, enums.SCREEN.ONBOARDING_CATALOG);
	},

	autoSaveBeforeNavigate(dispatch, {softwareProductId, version, vspComponentId, dataToSave}) {
		if(softwareProductId) {
			if(vspComponentId) {
				return SoftwareProductComponentsActionHelper.updateSoftwareProductComponent(dispatch, {
					softwareProductId, version, vspComponentId,
					componentData: dataToSave.componentData,
					qdata: dataToSave.qdata
				});
			}
			return SoftwareProductActionHelper.updateSoftwareProduct(dispatch, {
				softwareProduct: dataToSave.softwareProduct,
				qdata: dataToSave.qdata
			});
		}
		return Promise.resolve();
	},

	navigateToLicenseModelOverview(dispatch, {licenseModelId, version}) {
		if (!version) {
			version = getCurrentLicenseModelVersion(licenseModelId);
		}

		/**
		 * TODO change to specific rest
		 */

		LicenseModelActionHelper.fetchLicenseModelById(dispatch, {licenseModelId, version}).then(() => {
			LicenseModelActionHelper.fetchLicenseModelItems(dispatch, {licenseModelId, version}).then(() =>{
				setCurrentScreen(dispatch, enums.SCREEN.LICENSE_MODEL_OVERVIEW, {licenseModelId, version});
			});
			licenseModelOverviewActionHelper.selectVLMListView(dispatch, {buttonTab: licenseModelOverviewSelectedButton.VLM_LIST_VIEW});
		});
	},
	navigateToLicenseAgreements(dispatch, {licenseModelId, version}) {
		if(!version) {
			version = getCurrentLicenseModelVersion(licenseModelId);
		}
		LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {licenseModelId, version});
		LicenseModelActionHelper.fetchLicenseModelById(dispatch, {licenseModelId, version}).then(() => {
			setCurrentScreen(dispatch, enums.SCREEN.LICENSE_AGREEMENTS, {licenseModelId, version});
		});
	},

	navigateToFeatureGroups(dispatch, {licenseModelId, version}) {
		if(!version) {
			version = getCurrentLicenseModelVersion(licenseModelId);
		}
		FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId, version});
		setCurrentScreen(dispatch, enums.SCREEN.FEATURE_GROUPS, {licenseModelId, version});
	},

	navigateToEntitlementPools(dispatch, {licenseModelId, version}) {
		if(!version) {
			version = getCurrentLicenseModelVersion(licenseModelId);
		}
		EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {licenseModelId, version});
		setCurrentScreen(dispatch, enums.SCREEN.ENTITLEMENT_POOLS, {licenseModelId, version});
	},

	navigateToLicenseKeyGroups(dispatch, {licenseModelId, version}) {
		if(!version) {
			version = getCurrentLicenseModelVersion(licenseModelId);
		}
		LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {licenseModelId, version});
		setCurrentScreen(dispatch, enums.SCREEN.LICENSE_KEY_GROUPS, {licenseModelId, version});
	},

	navigateToLicenseModelActivityLog(dispatch, {licenseModelId, version}){
		if(!version) {
			version = getCurrentLicenseModelVersion(licenseModelId);
		}
		ActivityLogActionHelper.fetchActivityLog(dispatch, {itemId: licenseModelId, versionId: version.id});
		setCurrentScreen(dispatch, enums.SCREEN.ACTIVITY_LOG, {licenseModelId, version});
	},

	navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, licenseModelId, version, licensingVersion}) {

		if (!version) {
			version = getCurrentSoftwareProductVersion(softwareProductId);
		}

		SoftwareProductComponentsActionHelper.clearComponentsStore(dispatch);
		SoftwareProductActionHelper.fetchSoftwareProduct(dispatch, {softwareProductId, version}).then(response => {
			if(!licensingVersion) {
				licensingVersion = response[0].licensingVersion;
				if (!licensingVersion) {
					licensingVersion = {id: '1.0', label: '1.0'};
				}
			}
			if (!licenseModelId) {
				licenseModelId = response[0].vendorId;
			}

			const newVersion = response[0].version ? response[0].version : version;

			SoftwareProductActionHelper.loadSoftwareProductDetailsData(dispatch, {licenseModelId, licensingVersion});

			SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(dispatch, {softwareProductId, version: newVersion});
			if(response[0].onboardingOrigin === onboardingOriginTypes.ZIP) {
				SoftwareProductActionHelper.loadSoftwareProductHeatCandidate(dispatch, {softwareProductId, version: newVersion});
			}
			setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE, {softwareProductId, licenseModelId, version: newVersion});
		});
	},

	navigateToSoftwareProductDetails(dispatch, {softwareProductId, version}) {
		SoftwareProductActionHelper.fetchSoftwareProduct(dispatch, {softwareProductId, version});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_DETAILS, {softwareProductId, version});
	},

	navigateToSoftwareProductAttachmentsSetupTab(dispatch, {softwareProductId, version}) {
		SoftwareProductActionHelper.loadSoftwareProductHeatCandidate(dispatch, {softwareProductId, version});
		SoftwareProductAttachmentsActionHelper.setActiveTab(dispatch, {activeTab: attachmentsTabsMapping.SETUP});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS, {softwareProductId, version});
	},

	navigateToSoftwareProductAttachmentsValidationTab(dispatch, {softwareProductId, version}) {
		SoftwareProductActionHelper.processAndValidateHeatCandidate(dispatch, {softwareProductId, version}).then(() => {
			SoftwareProductAttachmentsActionHelper.setActiveTab(dispatch, {activeTab: attachmentsTabsMapping.VALIDATION});
			setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS, {softwareProductId, version});
		});
	},

	navigateToSoftwareProductProcesses(dispatch, {softwareProductId, version}) {
		if (softwareProductId) {
			SoftwareProductProcessesActionHelper.fetchProcessesList(dispatch, {softwareProductId, version});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES, {softwareProductId, version});
	},

	navigateToSoftwareProductNetworks(dispatch, {softwareProductId, version}) {
		if (softwareProductId) {
			SoftwareProductNetworksActionHelper.fetchNetworksList(dispatch, {softwareProductId, version});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS, {softwareProductId, version});
	},

	navigateToSoftwareProductDependencies(dispatch, {softwareProductId, version}) {
		SoftwareProductDependenciesActionHelper.fetchDependencies(dispatch, {softwareProductId, version});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES, {softwareProductId, version});
	},

	navigateToSoftwareProductComponents(dispatch, {softwareProductId, version}) {
		SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(dispatch, {softwareProductId, version});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS, {softwareProductId, version});
	},
	navigateToSoftwareProductDeployment(dispatch, {softwareProductId, version}) {
		SoftwareProductDeploymentActionHelper.fetchDeploymentFlavorsList(dispatch, {softwareProductId, version});
		ComputeFlavorActionHelper.fetchComputesListForVSP(dispatch, {softwareProductId, version});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT, {softwareProductId, version});
	},
	navigateToSoftwareProductActivityLog(dispatch, {softwareProductId, version}){
		ActivityLogActionHelper.fetchActivityLog(dispatch, {itemId: softwareProductId, versionId: version.id});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG, {softwareProductId, version});
	},

	navigateToSoftwareProductComponentProcesses(dispatch, {softwareProductId, componentId, version}) {
		if (componentId && softwareProductId) {
			SoftwareProductComponentProcessesActionHelper.fetchProcessesList(dispatch, {componentId, softwareProductId, version});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES, {softwareProductId, componentId, version});
	},

	navigateToSoftwareProductComponentMonitoring(dispatch, {softwareProductId, version, componentId}){
		if (componentId && softwareProductId && version) {
			SoftwareProductComponentsMonitoringAction.fetchExistingFiles(dispatch, {componentId, softwareProductId, version});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING, {softwareProductId, componentId, version});
	},

	navigateToComponentStorage(dispatch, {softwareProductId, componentId, version}) {
		SoftwareProductComponentsActionHelper.fetchSoftwareProductComponent(dispatch, {softwareProductId, vspComponentId: componentId, version});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE, {softwareProductId, version, componentId});
	},

	navigateToComponentCompute(dispatch, {softwareProductId, componentId, version}) {
		SoftwareProductComponentsActionHelper.fetchSoftwareProductComponent(dispatch, {softwareProductId, vspComponentId: componentId, version});
		if (componentId && softwareProductId) {
			ComputeFlavorActionHelper.fetchComputesList(dispatch, {softwareProductId, componentId, version});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE, {softwareProductId, version, componentId});
	},

	navigateToComponentNetwork(dispatch, {softwareProductId, componentId, version}) {
		SoftwareProductComponentsNetworkActionHelper.fetchNICsList(dispatch, {softwareProductId, componentId, version});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK, {softwareProductId, version, componentId});
	},

	navigateToSoftwareProductComponentGeneral(dispatch, {softwareProductId, componentId, version}) {
		if (componentId && softwareProductId) {
			SoftwareProductComponentsActionHelper.fetchSoftwareProductComponent(dispatch, {softwareProductId, vspComponentId: componentId, version});
		}
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL, {softwareProductId, version, componentId});
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

	navigateToComponentLoadBalancing(dispatch, {softwareProductId, componentId, version}) {
		SoftwareProductComponentsActionHelper.fetchSoftwareProductComponent(dispatch, {softwareProductId, vspComponentId: componentId, version});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING, {softwareProductId, version, componentId});
	},

	navigateToComponentImages(dispatch, {softwareProductId, componentId, version}) {
		SoftwareProductComponentsImageActionHelper.fetchImagesList(dispatch, {
			softwareProductId,
			componentId,
			version
		});
		setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES, {softwareProductId, version, componentId});
	},

	updateCurrentScreenVersion(dispatch, version) {
		dispatch({
			type: actionTypes.SET_CURRENT_SCREEN_VERSION,
			version
		});
	}

};

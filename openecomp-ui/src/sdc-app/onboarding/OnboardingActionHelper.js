/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

import UsersActionHelper from './users/UsersActionHelper.js';
import VersionsPageActionHelper from './versionsPage/VersionsPageActionHelper.js';
import PermissionsActionHelper from './permissions/PermissionsActionHelper.js';
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
import MergeEditorActionHelper from 'sdc-app/common/merge/MergeEditorActionHelper.js';
import SoftwareProductComponentsMonitoringAction from './softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringActionHelper.js';
import { actionTypes, enums } from './OnboardingConstants.js';
import {
    actionTypes as SoftwareProductActionTypes,
    onboardingOriginTypes
} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import ActivityLogActionHelper from 'sdc-app/common/activity-log/ActivityLogActionHelper.js';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';
import SoftwareProductComponentsImageActionHelper from './softwareProduct/components/images/SoftwareProductComponentsImageActionHelper.js';
import licenseModelOverviewActionHelper from 'sdc-app/onboarding/licenseModel/overview/licenseModelOverviewActionHelper.js';
import { tabsMapping as attachmentsTabsMapping } from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachmentsConstants.js';
import SoftwareProductAttachmentsActionHelper from 'sdc-app/onboarding/softwareProduct/attachments/SoftwareProductAttachmentsActionHelper.js';
import { actionTypes as filterActionTypes } from './onboard/filter/FilterConstants.js';
import FeaturesActionHelper from 'sdc-app/features/FeaturesActionHelper.js';
import { notificationActions } from 'nfvo-components/notification/NotificationsConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SoftwareProductValidationActionHelper from './softwareProduct/validation/SoftwareProductValidationActionHelper.js';
import { actionTypes as modalActionTypes } from 'nfvo-components/modal/GlobalModalConstants.js';

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

export function updateCurrentScreenProps(dispatch, props = {}) {
    dispatch({
        type: actionTypes.UPDATE_CURRENT_SCREEN_PROPS,
        props
    });
}

const OnboardingActionHelper = {
    loadItemsLists(dispatch) {
        LicenseModelActionHelper.fetchLicenseModels(dispatch);
        LicenseModelActionHelper.fetchFinalizedLicenseModels(dispatch);
        LicenseModelActionHelper.fetchArchivedLicenseModels(dispatch);
        SoftwareProductActionHelper.fetchSoftwareProductList(dispatch);
        SoftwareProductActionHelper.fetchFinalizedSoftwareProductList(dispatch);
        SoftwareProductActionHelper.fetchArchivedSoftwareProductList(dispatch);
    },

    async navigateToOnboardingCatalog(dispatch) {
        await FeaturesActionHelper.getFeaturesList(dispatch);
        UsersActionHelper.fetchUsersList(dispatch);
        this.loadItemsLists(dispatch);
        setCurrentScreen(dispatch, enums.SCREEN.ONBOARDING_CATALOG);
        dispatch({
            type: filterActionTypes.FILTER_DATA_CHANGED,
            deltaData: {}
        });
    },

    autoSaveBeforeNavigate(
        dispatch,
        { softwareProductId, version, vspComponentId, dataToSave }
    ) {
        if (softwareProductId) {
            if (vspComponentId) {
                return SoftwareProductComponentsActionHelper.updateSoftwareProductComponent(
                    dispatch,
                    {
                        softwareProductId,
                        version,
                        vspComponentId,
                        componentData: dataToSave.componentData,
                        qdata: dataToSave.qdata
                    }
                );
            }
            return SoftwareProductActionHelper.updateSoftwareProduct(dispatch, {
                softwareProduct: dataToSave.softwareProduct,
                version,
                qdata: dataToSave.qdata
            });
        }
        return Promise.resolve();
    },

    navigateToLicenseModelOverview(
        dispatch,
        { licenseModelId, version, status }
    ) {
        /**
         * TODO change to specific rest
         */

        LicenseModelActionHelper.fetchLicenseModelById(dispatch, {
            licenseModelId,
            version
        }).then(() => {
            LicenseModelActionHelper.fetchLicenseModelItems(dispatch, {
                licenseModelId,
                version
            }).then(() => {
                setCurrentScreen(
                    dispatch,
                    enums.SCREEN.LICENSE_MODEL_OVERVIEW,
                    { licenseModelId, version, status }
                );
            });
            licenseModelOverviewActionHelper.selectVLMListView(dispatch, {
                buttonTab: null
            });
        });
    },
    navigateToLicenseAgreements(dispatch, { licenseModelId, version, status }) {
        LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {
            licenseModelId,
            version
        });
        LicenseModelActionHelper.fetchLicenseModelById(dispatch, {
            licenseModelId,
            version
        }).then(() => {
            setCurrentScreen(dispatch, enums.SCREEN.LICENSE_AGREEMENTS, {
                licenseModelId,
                version,
                status
            });
        });
    },

    navigateToFeatureGroups(dispatch, { licenseModelId, version, status }) {
        FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {
            licenseModelId,
            version
        });
        setCurrentScreen(dispatch, enums.SCREEN.FEATURE_GROUPS, {
            licenseModelId,
            version,
            status
        });
    },

    navigateToEntitlementPools(dispatch, { licenseModelId, version, status }) {
        EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {
            licenseModelId,
            version
        });
        setCurrentScreen(dispatch, enums.SCREEN.ENTITLEMENT_POOLS, {
            licenseModelId,
            version,
            status
        });
    },

    navigateToLicenseKeyGroups(dispatch, { licenseModelId, version, status }) {
        LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {
            licenseModelId,
            version
        });
        setCurrentScreen(dispatch, enums.SCREEN.LICENSE_KEY_GROUPS, {
            licenseModelId,
            version,
            status
        });
    },

    navigateToLicenseModelActivityLog(
        dispatch,
        { licenseModelId, version, status }
    ) {
        ActivityLogActionHelper.fetchActivityLog(dispatch, {
            itemId: licenseModelId,
            versionId: version.id
        });
        setCurrentScreen(dispatch, enums.SCREEN.ACTIVITY_LOG, {
            licenseModelId,
            version,
            status
        });
    },
    async getUpdatedSoftwareProduct(dispatch, { softwareProductId, version }) {
        const response = await SoftwareProductActionHelper.fetchSoftwareProduct(
            dispatch,
            {
                softwareProductId,
                version
            }
        );
        let newResponse = false;
        let newVersion = false;
        // checking if there was healing and a new version should be open
        if (response[0].version !== version.id) {
            newResponse = await SoftwareProductActionHelper.fetchSoftwareProduct(
                dispatch,
                {
                    softwareProductId,
                    version: { ...version, id: response[0].version }
                }
            );
            newVersion = await ItemsHelper.fetchVersion({
                itemId: softwareProductId,
                versionId: response[0].version
            });

            dispatch(
                notificationActions.showInfo({
                    message: i18n(
                        'This is the current version of the VSP, as a result of healing'
                    )
                })
            );
        }
        return Promise.resolve(
            newResponse
                ? { softwareProduct: newResponse[0], newVersion }
                : { softwareProduct: response[0], newVersion: version }
        );
    },
    async navigateToSoftwareProductLandingPage(
        dispatch,
        { softwareProductId, version, status }
    ) {
        SoftwareProductComponentsActionHelper.clearComponentsStore(dispatch);
        /**
         * TODO remove when Filter toggle will be removed
         */
        LicenseModelActionHelper.fetchFinalizedLicenseModels(dispatch);

        const {
            softwareProduct,
            newVersion
        } = await this.getUpdatedSoftwareProduct(dispatch, {
            softwareProductId,
            version
        });

        let { vendorId: licenseModelId, licensingVersion } = softwareProduct;
        SoftwareProductActionHelper.loadSoftwareProductDetailsData(dispatch, {
            licenseModelId,
            licensingVersion
        });
        SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(
            dispatch,
            { softwareProductId, version: newVersion }
        );
        if (softwareProduct.onboardingOrigin === onboardingOriginTypes.ZIP) {
            SoftwareProductActionHelper.loadSoftwareProductHeatCandidate(
                dispatch,
                { softwareProductId, version: newVersion }
            );
        }
        setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE, {
            softwareProductId,
            licenseModelId,
            version: newVersion,
            status
        });
    },

    navigateToSoftwareProductDetails(
        dispatch,
        { softwareProductId, version, status }
    ) {
        SoftwareProductActionHelper.fetchSoftwareProduct(dispatch, {
            softwareProductId,
            version
        }).then(response => {
            let { vendorId: licenseModelId, licensingVersion } = response[0];
            SoftwareProductActionHelper.loadLicensingVersionsList(dispatch, {
                licenseModelId
            });
            SoftwareProductActionHelper.loadSoftwareProductDetailsData(
                dispatch,
                { licenseModelId, licensingVersion }
            );
            setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_DETAILS, {
                softwareProductId,
                version,
                status
            });
        });
    },

    navigateToSoftwareProductAttachmentsSetupTab(
        dispatch,
        { softwareProductId, version, status }
    ) {
        SoftwareProductActionHelper.loadSoftwareProductHeatCandidate(dispatch, {
            softwareProductId,
            version
        });
        SoftwareProductAttachmentsActionHelper.setActiveTab(dispatch, {
            activeTab: attachmentsTabsMapping.SETUP
        });
        setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS, {
            softwareProductId,
            version,
            status
        });
    },
    navigateToSoftwareProductAttachmentsValidationTab(
        dispatch,
        { softwareProductId, version, status }
    ) {
        SoftwareProductActionHelper.processAndValidateHeatCandidate(dispatch, {
            softwareProductId,
            version
        }).then(() => {
            SoftwareProductAttachmentsActionHelper.setActiveTab(dispatch, {
                activeTab: attachmentsTabsMapping.VALIDATION
            });
            setCurrentScreen(
                dispatch,
                enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS,
                { softwareProductId, version, status }
            );
        });
    },

    navigateToSoftwareProductProcesses(
        dispatch,
        { softwareProductId, version, status }
    ) {
        if (softwareProductId) {
            SoftwareProductProcessesActionHelper.fetchProcessesList(dispatch, {
                softwareProductId,
                version
            });
        }
        setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES, {
            softwareProductId,
            version,
            status
        });
    },

    navigateToSoftwareProductNetworks(
        dispatch,
        { softwareProductId, version, status }
    ) {
        if (softwareProductId) {
            SoftwareProductNetworksActionHelper.fetchNetworksList(dispatch, {
                softwareProductId,
                version
            });
        }
        setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS, {
            softwareProductId,
            version,
            status
        });
    },

    navigateToSoftwareProductValidation(
        dispatch,
        { softwareProductId, version, status }
    ) {
        SoftwareProductValidationActionHelper.fetchVspChecks(dispatch)
            .then(() => {
                SoftwareProductValidationActionHelper.setCertificationChecked(
                    dispatch,
                    []
                );
                SoftwareProductValidationActionHelper.setComplianceChecked(
                    dispatch,
                    []
                );
                setCurrentScreen(
                    dispatch,
                    enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION,
                    {
                        softwareProductId,
                        version,
                        status
                    }
                );
            })
            .catch(error => {
                dispatch({
                    type: modalActionTypes.GLOBAL_MODAL_ERROR,
                    data: {
                        title: 'ERROR',
                        msg: error.responseJSON.message,
                        cancelButtonText: i18n('OK')
                    }
                });
            });
    },

    navigateToSoftwareProductValidationResults(
        dispatch,
        { softwareProductId, version, status }
    ) {
        setCurrentScreen(
            dispatch,
            enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION_RESULTS,
            { softwareProductId, version, status }
        );
    },

    navigateToSoftwareProductDependencies(
        dispatch,
        { softwareProductId, version, status }
    ) {
        SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(
            dispatch,
            { softwareProductId, version }
        ).then(result => {
            if (result.listCount >= 2) {
                SoftwareProductDependenciesActionHelper.fetchDependencies(
                    dispatch,
                    { softwareProductId, version }
                );
                setCurrentScreen(
                    dispatch,
                    enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES,
                    { softwareProductId, version, status }
                );
            } else {
                this.navigateToSoftwareProductLandingPage(dispatch, {
                    softwareProductId,
                    version,
                    status
                });
            }
        });
    },

    navigateToSoftwareProductComponents(
        dispatch,
        { softwareProductId, version, status }
    ) {
        SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(
            dispatch,
            { softwareProductId, version }
        );
        setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS, {
            softwareProductId,
            version,
            status
        });
    },
    navigateToSoftwareProductDeployment(
        dispatch,
        { softwareProductId, version, status }
    ) {
        SoftwareProductDeploymentActionHelper.fetchDeploymentFlavorsList(
            dispatch,
            { softwareProductId, version }
        );
        ComputeFlavorActionHelper.fetchComputesListForVSP(dispatch, {
            softwareProductId,
            version
        });
        setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT, {
            softwareProductId,
            version,
            status
        });
    },
    navigateToSoftwareProductActivityLog(
        dispatch,
        { softwareProductId, version, status }
    ) {
        ActivityLogActionHelper.fetchActivityLog(dispatch, {
            itemId: softwareProductId,
            versionId: version.id
        });
        setCurrentScreen(dispatch, enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG, {
            softwareProductId,
            version,
            status
        });
    },

    navigateToSoftwareProductComponentProcesses(
        dispatch,
        { softwareProductId, componentId, version, status }
    ) {
        if (componentId && softwareProductId) {
            SoftwareProductComponentProcessesActionHelper.fetchProcessesList(
                dispatch,
                { componentId, softwareProductId, version }
            );
        }
        setCurrentScreen(
            dispatch,
            enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES,
            { softwareProductId, componentId, version, status }
        );
    },

    navigateToSoftwareProductComponentMonitoring(
        dispatch,
        { softwareProductId, version, componentId, status }
    ) {
        if (componentId && softwareProductId && version) {
            SoftwareProductComponentsMonitoringAction.fetchExistingFiles(
                dispatch,
                { componentId, softwareProductId, version }
            );
        }
        setCurrentScreen(
            dispatch,
            enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING,
            { softwareProductId, componentId, version, status }
        );
    },

    navigateToComponentStorage(
        dispatch,
        { softwareProductId, componentId, version, status }
    ) {
        SoftwareProductComponentsActionHelper.fetchSoftwareProductComponent(
            dispatch,
            { softwareProductId, vspComponentId: componentId, version }
        );
        setCurrentScreen(
            dispatch,
            enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE,
            { softwareProductId, version, componentId, status }
        );
    },

    navigateToComponentCompute(
        dispatch,
        { softwareProductId, componentId, version, status }
    ) {
        SoftwareProductComponentsActionHelper.fetchSoftwareProductComponent(
            dispatch,
            { softwareProductId, vspComponentId: componentId, version }
        );
        if (componentId && softwareProductId) {
            ComputeFlavorActionHelper.fetchComputesList(dispatch, {
                softwareProductId,
                componentId,
                version
            });
        }
        setCurrentScreen(
            dispatch,
            enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE,
            { softwareProductId, version, componentId, status }
        );
    },

    navigateToComponentNetwork(
        dispatch,
        { softwareProductId, componentId, version, status }
    ) {
        SoftwareProductComponentsNetworkActionHelper.fetchNICsList(dispatch, {
            softwareProductId,
            componentId,
            version
        });
        setCurrentScreen(
            dispatch,
            enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK,
            { softwareProductId, version, componentId, status }
        );
    },

    navigateToSoftwareProductComponentGeneral(
        dispatch,
        { softwareProductId, componentId, version, status }
    ) {
        if (componentId && softwareProductId) {
            SoftwareProductComponentsActionHelper.fetchSoftwareProductComponent(
                dispatch,
                { softwareProductId, vspComponentId: componentId, version }
            );
        }
        setCurrentScreen(
            dispatch,
            enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL,
            { softwareProductId, version, componentId, status }
        );
    },

    navigateToSoftwareProductComponentGeneralAndUpdateLeftPanel(
        dispatch,
        { softwareProductId, componentId, version, status }
    ) {
        this.navigateToSoftwareProductComponentGeneral(dispatch, {
            softwareProductId,
            componentId,
            version,
            status
        });
        dispatch({
            type: SoftwareProductActionTypes.TOGGLE_NAVIGATION_ITEM,
            mapOfExpandedIds: {
                [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS]: true,
                [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS +
                '|' +
                componentId]: true
            }
        });
    },

    navigateToComponentLoadBalancing(
        dispatch,
        { softwareProductId, componentId, version, status }
    ) {
        SoftwareProductComponentsActionHelper.fetchSoftwareProductComponent(
            dispatch,
            { softwareProductId, vspComponentId: componentId, version }
        );
        setCurrentScreen(
            dispatch,
            enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING,
            { softwareProductId, version, componentId, status }
        );
    },

    navigateToComponentImages(
        dispatch,
        { softwareProductId, componentId, version, status }
    ) {
        SoftwareProductComponentsImageActionHelper.fetchImagesList(dispatch, {
            softwareProductId,
            componentId,
            version
        });
        setCurrentScreen(
            dispatch,
            enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES,
            { softwareProductId, version, componentId, status }
        );
    },

    async navigateToVersionsPage(
        dispatch,
        { itemType, itemId, itemName, additionalProps, users }
    ) {
        PermissionsActionHelper.fetchItemUsers(dispatch, {
            itemId,
            allUsers: users
        });
        VersionsPageActionHelper.selectNone(dispatch);
        await VersionsPageActionHelper.fetchVersions(dispatch, {
            itemType,
            itemId
        });
        const items = await ItemsHelper.fetchItem(itemId);
        setCurrentScreen(dispatch, enums.SCREEN.VERSIONS_PAGE, {
            status: items.status,
            itemType,
            itemId,
            itemName,
            vendorName: items.properties.vendorName,
            vendorId: items.properties.vendorId,
            additionalProps
        });
    },

    checkMergeStatus(dispatch, { itemId, versionId, version }) {
        return ItemsHelper.fetchVersion({ itemId, versionId }).then(
            response => {
                let state = (response && response.state) || {};
                let { synchronizationState } = state;
                // let inMerge = synchronizationState === SyncStates.MERGE;
                MergeEditorActionHelper.fetchConflicts(dispatch, {
                    itemId,
                    version
                }).then(data => {
                    dispatch({
                        type: actionTypes.CHECK_MERGE_STATUS,
                        synchronizationState,
                        conflictInfoList: data.conflictInfoList
                    });
                });
            }
        );
    },

    forceBreadCrumbsUpdate(dispatch) {
        dispatch({
            type: actionTypes.SET_CURRENT_SCREEN,
            currentScreen: {
                forceBreadCrumbsUpdate: true
            }
        });
    },

    updateCurrentScreenVersion(dispatch, version) {
        dispatch({
            type: actionTypes.SET_CURRENT_SCREEN_VERSION,
            version
        });
    }
};

export default OnboardingActionHelper;

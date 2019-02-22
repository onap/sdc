import { itemTypes } from 'sdc-app/onboarding/versionsPage/VersionsPageConstants.js';
import { enums, screenTypes } from 'sdc-app/onboarding/OnboardingConstants.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import { actionTypes as SoftwareProductActionTypes } from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';
import versionPageActionHelper from 'sdc-app/onboarding/versionsPage/VersionsPageActionHelper.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import { actionTypes as modalActionTypes } from 'nfvo-components/modal/GlobalModalConstants.js';

const ScreensHelper = {
    async loadScreen(dispatch, { screen, screenType, props }) {
        if (screen === enums.SCREEN.ONBOARDING_CATALOG) {
            OnboardingActionHelper.navigateToOnboardingCatalog(dispatch);
            return;
        }

        screenType = !screenType ? this.getScreenType(screen) : screenType;

        if (screenType === screenTypes.LICENSE_MODEL) {
            const { licenseModelId, version, licenseModel, usersList } = props;
            const item = await ItemsHelper.fetchItem(licenseModelId);
            let itemStatusPromise =
                version && screen
                    ? ItemsHelper.checkItemStatus(dispatch, {
                          itemId: licenseModelId,
                          versionId: version.id
                      })
                    : Promise.resolve();
            itemStatusPromise.then(updatedVersion => {
                if (
                    updatedVersion &&
                    updatedVersion.status !== version.status
                ) {
                    dispatch({
                        type: modalActionTypes.GLOBAL_MODAL_WARNING,
                        data: {
                            title: i18n('Commit error'),
                            msg: i18n('Item version was certified by Owner'),
                            cancelButtonText: i18n('Cancel')
                        }
                    });
                    versionPageActionHelper.fetchVersions(dispatch, {
                        itemType: itemTypes.LICENSE_MODEL,
                        itemId: licenseModelId
                    });
                }
                let newVersion = updatedVersion ? updatedVersion : version;
                const screenProps = {
                    licenseModelId,
                    version: newVersion,
                    status: item.status
                };
                switch (screen) {
                    case enums.SCREEN.LICENSE_MODEL_OVERVIEW:
                        OnboardingActionHelper.navigateToLicenseModelOverview(
                            dispatch,
                            screenProps
                        );
                        break;
                    case enums.SCREEN.LICENSE_AGREEMENTS:
                        OnboardingActionHelper.navigateToLicenseAgreements(
                            dispatch,
                            screenProps
                        );
                        break;
                    case enums.SCREEN.FEATURE_GROUPS:
                        OnboardingActionHelper.navigateToFeatureGroups(
                            dispatch,
                            screenProps
                        );
                        break;
                    case enums.SCREEN.ENTITLEMENT_POOLS:
                        OnboardingActionHelper.navigateToEntitlementPools(
                            dispatch,
                            screenProps
                        );
                        break;
                    case enums.SCREEN.LICENSE_KEY_GROUPS:
                        OnboardingActionHelper.navigateToLicenseKeyGroups(
                            dispatch,
                            screenProps
                        );
                        break;
                    case enums.SCREEN.ACTIVITY_LOG:
                        OnboardingActionHelper.navigateToLicenseModelActivityLog(
                            dispatch,
                            screenProps
                        );
                        break;
                    case enums.SCREEN.VERSIONS_PAGE:
                    default:
                        OnboardingActionHelper.navigateToVersionsPage(
                            dispatch,
                            {
                                itemId: licenseModelId,
                                itemType: itemTypes.LICENSE_MODEL,
                                itemName: licenseModel.name,
                                users: usersList
                            }
                        );
                        break;
                }
            });
        } else if (screenType === screenTypes.SOFTWARE_PRODUCT) {
            const {
                softwareProductId,
                componentId,
                version,
                softwareProduct,
                usersList
            } = props;
            const item = await ItemsHelper.fetchItem(softwareProductId);
            let itemStatusPromise =
                version && screen
                    ? ItemsHelper.checkItemStatus(dispatch, {
                          itemId: softwareProductId,
                          versionId: version.id
                      })
                    : Promise.resolve();
            itemStatusPromise.then(updatedVersion => {
                if (
                    updatedVersion &&
                    updatedVersion.status !== version.status
                ) {
                    dispatch({
                        type: modalActionTypes.GLOBAL_MODAL_WARNING,
                        data: {
                            title: i18n('Commit error'),
                            msg: i18n('Item version already Certified'),
                            cancelButtonText: i18n('Cancel')
                        }
                    });
                    versionPageActionHelper.fetchVersions(dispatch, {
                        itemType: itemTypes.SOFTWARE_PRODUCT,
                        itemId: softwareProductId
                    });
                }

                let newVersion = updatedVersion ? updatedVersion : version;

                const vspProps = {
                    softwareProductId,
                    componentId,
                    version: newVersion,
                    status: item.status
                };
                if (
                    screen ===
                    screenTypes.SOFTWARE_PRODUCT_COMPONENT_DEFAULT_GENERAL
                ) {
                    OnboardingActionHelper.navigateToSoftwareProductComponentGeneralAndUpdateLeftPanel(
                        dispatch,
                        vspProps
                    );
                }

                switch (screen) {
                    case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
                        OnboardingActionHelper.navigateToSoftwareProductLandingPage(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
                        OnboardingActionHelper.navigateToSoftwareProductDetails(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS_SETUP:
                        OnboardingActionHelper.navigateToSoftwareProductAttachmentsSetupTab(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS_VALIDATION:
                        OnboardingActionHelper.navigateToSoftwareProductAttachmentsValidationTab(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
                        OnboardingActionHelper.navigateToSoftwareProductProcesses(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT:
                        OnboardingActionHelper.navigateToSoftwareProductDeployment(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
                        OnboardingActionHelper.navigateToSoftwareProductNetworks(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION:
                        OnboardingActionHelper.navigateToSoftwareProductValidation(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION_MONITOR:
                        OnboardingActionHelper.navigateToSoftwareProductValidationMonitor(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES:
                        OnboardingActionHelper.navigateToSoftwareProductDependencies(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG:
                        OnboardingActionHelper.navigateToSoftwareProductActivityLog(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:
                        if (!componentId) {
                            OnboardingActionHelper.navigateToSoftwareProductComponents(
                                dispatch,
                                vspProps
                            );
                            dispatch({
                                type:
                                    SoftwareProductActionTypes.TOGGLE_NAVIGATION_ITEM,
                                mapOfExpandedIds: {
                                    [enums.SCREEN
                                        .SOFTWARE_PRODUCT_COMPONENTS]: true
                                }
                            });
                        } else {
                            OnboardingActionHelper.navigateToSoftwareProductComponentGeneralAndUpdateLeftPanel(
                                dispatch,
                                vspProps
                            );
                        }
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
                        OnboardingActionHelper.navigateToSoftwareProductComponentGeneral(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
                        OnboardingActionHelper.navigateToComponentCompute(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
                        OnboardingActionHelper.navigateToComponentLoadBalancing(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
                        OnboardingActionHelper.navigateToComponentNetwork(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
                        OnboardingActionHelper.navigateToComponentStorage(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
                        OnboardingActionHelper.navigateToSoftwareProductComponentProcesses(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
                        OnboardingActionHelper.navigateToSoftwareProductComponentMonitoring(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES:
                        OnboardingActionHelper.navigateToComponentImages(
                            dispatch,
                            vspProps
                        );
                        break;
                    case enums.SCREEN.SOFTWARE_PRODUCT_VERSIONS_PAGE:
                    default:
                        OnboardingActionHelper.navigateToVersionsPage(
                            dispatch,
                            {
                                itemId: softwareProductId,
                                itemType: itemTypes.SOFTWARE_PRODUCT,
                                itemName: softwareProduct.name,
                                users: usersList,
                                additionalProps: {
                                    licenseModelId: softwareProduct.vendorId,
                                    licensingVersion:
                                        softwareProduct.licensingVersion
                                }
                            }
                        );
                        break;
                }
            });
        }
    },

    getScreenType(screen) {
        switch (screen) {
            case enums.SCREEN.LICENSE_MODEL_OVERVIEW:
            case enums.SCREEN.LICENSE_AGREEMENTS:
            case enums.SCREEN.FEATURE_GROUPS:
            case enums.SCREEN.ENTITLEMENT_POOLS:
            case enums.SCREEN.LICENSE_KEY_GROUPS:
            case enums.SCREEN.ACTIVITY_LOG:
                return screenTypes.LICENSE_MODEL;
            case screenTypes.SOFTWARE_PRODUCT_COMPONENT_DEFAULT_GENERAL:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES:
            case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
            case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
            case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS:
            case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
            case enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT:
            case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
            case enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES:
            case enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:
                return screenTypes.SOFTWARE_PRODUCT;
        }
    },

    loadLandingScreen(
        dispatch,
        {
            previousScreenName,
            screenType,
            props: { licenseModelId, softwareProductId, version }
        }
    ) {
        let selectedScreenType = screenType
            ? screenType
            : this.getScreenType(previousScreenName);
        let screen =
            selectedScreenType === screenTypes.SOFTWARE_PRODUCT
                ? enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE
                : enums.SCREEN.LICENSE_MODEL_OVERVIEW;
        let props = { licenseModelId, softwareProductId, version };
        return this.loadScreen(dispatch, {
            screen,
            screenType: selectedScreenType,
            props
        });
    }
};

export default ScreensHelper;

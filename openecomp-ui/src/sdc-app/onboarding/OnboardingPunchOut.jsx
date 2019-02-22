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

import React from 'react';

import { render } from 'react-dom';
import ReactDOM from 'react-dom';

import isEqual from 'lodash/isEqual.js';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Application from 'sdc-app/Application.jsx';
import store from 'sdc-app/AppStore.js';
import Configuration from 'sdc-app/config/Configuration.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import ConfigHelper from 'sdc-app/common/helpers/ConfigHelper.js';
import {
    onboardingMethod as onboardingMethodTypes,
    onboardingOriginTypes
} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';

import { itemTypes } from './versionsPage/VersionsPageConstants.js';

import { AppContainer } from 'react-hot-loader';
import HeatSetupActionHelper from './softwareProduct/attachments/setup/HeatSetupActionHelper.js';

import { actionTypes, enums, screenTypes } from './OnboardingConstants.js';
import { actionTypes as modalActionTypes } from 'nfvo-components/modal/GlobalModalConstants.js';
import OnboardingActionHelper from './OnboardingActionHelper.js';
import Onboarding from './Onboarding.js';

let isVSPValidationDisabled = true;
ConfigHelper.fetchVspConfig()
    .then(response => {
        isVSPValidationDisabled =
            response.enabled === undefined || response.enabled === ''
                ? true
                : !response.enabled;
    })
    .catch(error => {
        let dispatch = action => store.dispatch(action);
        dispatch({
            type: modalActionTypes.GLOBAL_MODAL_ERROR,
            data: {
                title: i18n('ERROR'),
                msg: error.message,
                cancelButtonText: i18n('OK')
            }
        });
    });

export default class OnboardingPunchOut {
    render({ options: { data, apiRoot, apiHeaders }, onEvent }, element) {
        if (!this.unsubscribeFromStore) {
            this.unsubscribeFromStore = store.subscribe(() =>
                this.handleStoreChange()
            );
        }

        if (!this.isConfigSet) {
            Configuration.setCatalogApiRoot(apiRoot);
            Configuration.setCatalogApiHeaders(apiHeaders);
            this.isConfigSet = true;
        }

        this.onEvent = (...args) => onEvent(...args);
        this.handleData(data);

        if (!this.rendered) {
            render(
                <AppContainer>
                    <Application>
                        <Onboarding />
                    </Application>
                </AppContainer>,
                element
            );
            if (module.hot) {
                module.hot.accept('sdc-app/onboarding/Onboarding.js', () => {
                    const NextOnboarding = require('sdc-app/onboarding/Onboarding.js')
                        .default;
                    render(
                        <AppContainer>
                            <Application>
                                <NextOnboarding />
                            </Application>
                        </AppContainer>,
                        element
                    );
                });
            }
            this.rendered = true;
        }
    }

    unmount(element) {
        ReactDOM.unmountComponentAtNode(element);
        this.rendered = false;
        this.unsubscribeFromStore();
        this.unsubscribeFromStore = null;
    }

    handleData(data) {
        let { breadcrumbs: { selectedKeys = [] } = {} } = data;
        let dispatch = action => store.dispatch(action);
        let {
            currentScreen,
            users: { usersList },
            softwareProductList,
            finalizedSoftwareProductList,
            licenseModelList,
            finalizedLicenseModelList,
            softwareProduct: {
                softwareProductEditor: { data: vspData = {} },
                softwareProductComponents = {},
                softwareProductQuestionnaire = {}
            },
            archivedLicenseModelList
        } = store.getState();
        const wholeSoftwareProductList = [
            ...softwareProductList,
            ...finalizedSoftwareProductList
        ];
        const wholeLicenseModelList = [
            ...licenseModelList,
            ...finalizedLicenseModelList,
            ...archivedLicenseModelList
        ];

        let { props: { version, isReadOnlyMode }, screen } = currentScreen;
        let {
            componentEditor: {
                data: componentData = {},
                qdata: componentQData = {}
            }
        } = softwareProductComponents;
        if (this.programmaticBreadcrumbsUpdate) {
            this.prevSelectedKeys = selectedKeys;
            this.programmaticBreadcrumbsUpdate = false;
            return;
        }
        if (!isEqual(selectedKeys, this.prevSelectedKeys)) {
            this.breadcrumbsPrefixSelected = isEqual(
                selectedKeys,
                this.prevSelectedKeys &&
                    this.prevSelectedKeys.slice(0, selectedKeys.length)
            );

            const [, screenType, prevVspId, , prevComponentId] =
                this.prevSelectedKeys || [];
            let preNavigate = Promise.resolve();
            if (
                screenType === enums.BREADCRUMS.SOFTWARE_PRODUCT &&
                screen !== 'VERSIONS_PAGE' &&
                !isReadOnlyMode
            ) {
                let dataToSave = prevVspId
                    ? prevComponentId
                      ? { componentData, qdata: componentQData }
                      : {
                            softwareProduct: vspData,
                            qdata: softwareProductQuestionnaire.qdata
                        }
                    : {};
                preNavigate = OnboardingActionHelper.autoSaveBeforeNavigate(
                    dispatch,
                    {
                        softwareProductId: prevVspId,
                        version,
                        vspComponentId: prevComponentId,
                        dataToSave
                    }
                );
            }

            let {
                currentScreen: { props: { softwareProductId } },
                softwareProduct: {
                    softwareProductAttachments: { heatSetup, heatSetupCache }
                }
            } = store.getState();
            let heatSetupPopupPromise =
                currentScreen.screen ===
                enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS
                    ? HeatSetupActionHelper.heatSetupLeaveConfirmation(
                          dispatch,
                          { softwareProductId, heatSetup, heatSetupCache }
                      )
                    : Promise.resolve();
            Promise.all([preNavigate, heatSetupPopupPromise])
                .then(() => {
                    this.prevSelectedKeys = selectedKeys;
                    if (selectedKeys.length === 0) {
                        ScreensHelper.loadScreen(dispatch, {
                            screen: enums.SCREEN.ONBOARDING_CATALOG
                        });
                    } else if (
                        selectedKeys.length === 1 ||
                        selectedKeys[1] === enums.BREADCRUMS.LICENSE_MODEL
                    ) {
                        let [
                            licenseModelId,
                            ,
                            licenseModelScreen
                        ] = selectedKeys;
                        let licenseModel = wholeLicenseModelList.find(
                            vlm => vlm.id === licenseModelId
                        );
                        ScreensHelper.loadScreen(dispatch, {
                            screen: licenseModelScreen,
                            screenType: screenTypes.LICENSE_MODEL,
                            props: {
                                licenseModelId,
                                version,
                                licenseModel,
                                usersList
                            }
                        });
                    } else if (
                        selectedKeys.length <= 4 &&
                        selectedKeys[1] === enums.BREADCRUMS.SOFTWARE_PRODUCT
                    ) {
                        let [
                            licenseModelId,
                            ,
                            softwareProductId,
                            softwareProductScreen
                        ] = selectedKeys;
                        let softwareProduct = softwareProductId
                            ? wholeSoftwareProductList.find(
                                  ({ id }) => id === softwareProductId
                              )
                            : wholeSoftwareProductList.find(
                                  ({ vendorId }) => vendorId === licenseModelId
                              );
                        if (!softwareProductId) {
                            softwareProductId = softwareProduct.id;
                        }
                        if (
                            softwareProductScreen ===
                            enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS
                        ) {
                            softwareProduct = { ...vspData };
                            //check current vsp fields to determine which file has uploaded

                            if (
                                vspData.onboardingOrigin.toLowerCase() ===
                                    onboardingOriginTypes.ZIP ||
                                vspData.candidateOnboardingOrigin.toLowerCase() ===
                                    onboardingOriginTypes.ZIP
                            ) {
                                softwareProductScreen =
                                    enums.SCREEN
                                        .SOFTWARE_PRODUCT_ATTACHMENTS_SETUP;
                            } else if (
                                vspData.onboardingOrigin ===
                                onboardingOriginTypes.CSAR
                            ) {
                                softwareProductScreen =
                                    enums.SCREEN
                                        .SOFTWARE_PRODUCT_ATTACHMENTS_VALIDATION;
                            }
                        }

                        ScreensHelper.loadScreen(dispatch, {
                            screen: softwareProductScreen,
                            screenType: screenTypes.SOFTWARE_PRODUCT,
                            props: {
                                softwareProductId,
                                softwareProduct,
                                version,
                                usersList
                            }
                        });
                    } else if (
                        selectedKeys.length === 5 &&
                        selectedKeys[1] === enums.BREADCRUMS.SOFTWARE_PRODUCT &&
                        selectedKeys[3] ===
                            enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS
                    ) {
                        let [
                            licenseModelId,
                            ,
                            softwareProductId,
                            ,
                            componentId
                        ] = selectedKeys;
                        let softwareProduct = softwareProductId
                            ? wholeSoftwareProductList.find(
                                  ({ id }) => id === softwareProductId
                              )
                            : wholeSoftwareProductList.find(
                                  ({ vendorId }) => vendorId === licenseModelId
                              );
                        if (!softwareProductId) {
                            softwareProductId = softwareProduct.id;
                        }
                        ScreensHelper.loadScreen(dispatch, {
                            screen: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS,
                            screenType: screenTypes.SOFTWARE_PRODUCT,
                            props: {
                                softwareProductId,
                                softwareProduct,
                                componentId,
                                version,
                                usersList
                            }
                        });
                    } else if (
                        selectedKeys.length === 6 &&
                        selectedKeys[1] === enums.BREADCRUMS.SOFTWARE_PRODUCT &&
                        selectedKeys[3] ===
                            enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS
                    ) {
                        let [
                            licenseModelId,
                            ,
                            softwareProductId,
                            ,
                            componentId,
                            componentScreen
                        ] = selectedKeys;
                        let softwareProduct = softwareProductId
                            ? wholeSoftwareProductList.find(
                                  ({ id }) => id === softwareProductId
                              )
                            : wholeSoftwareProductList.find(
                                  ({ vendorId }) => vendorId === licenseModelId
                              );
                        if (!softwareProductId) {
                            softwareProductId = softwareProduct.id;
                        }
                        ScreensHelper.loadScreen(dispatch, {
                            screen: componentScreen,
                            screenType: screenTypes.SOFTWARE_PRODUCT,
                            props: {
                                softwareProductId,
                                softwareProduct,
                                componentId,
                                version,
                                usersList
                            }
                        });
                    } else {
                        console.error(
                            'Unknown breadcrumbs path: ',
                            selectedKeys
                        );
                    }
                })
                .catch(() => {
                    store.dispatch({
                        type: actionTypes.SET_CURRENT_SCREEN,
                        currentScreen: {
                            ...currentScreen,
                            forceBreadCrumbsUpdate: true
                        }
                    });
                });
        }
    }

    handleStoreChange() {
        let {
            currentScreen,
            versionsPage: { versionsList: { itemType, itemId } },
            softwareProduct: {
                softwareProductEditor: {
                    data: currentSoftwareProduct = { onboardingMethod: '' }
                },
                softwareProductComponents: { componentsList }
            },
            licenseModel: {
                licenseModelEditor: { data: currentLicenseModel = {} }
            }
        } = store.getState();

        let breadcrumbsData = {
            itemType,
            itemId,
            currentScreen,
            currentSoftwareProduct,
            currentLicenseModel,
            componentsList
        };

        if (
            currentScreen.forceBreadCrumbsUpdate ||
            !isEqual(breadcrumbsData, this.prevBreadcrumbsData) ||
            this.breadcrumbsPrefixSelected
        ) {
            this.prevBreadcrumbsData = breadcrumbsData;
            this.breadcrumbsPrefixSelected = false;
            this.programmaticBreadcrumbsUpdate = true;
            let breadcrumbs = this.buildBreadcrumbs(breadcrumbsData);
            this.onEvent('breadcrumbsupdated', breadcrumbs);
            store.dispatch({
                type: actionTypes.SET_CURRENT_SCREEN,
                currentScreen: {
                    ...currentScreen,
                    forceBreadCrumbsUpdate: false
                }
            });
        }
    }

    buildBreadcrumbs({
        currentScreen: { screen, props },
        itemType,
        itemId,
        currentSoftwareProduct,
        currentLicenseModel,
        componentsList
    }) {
        let {
            onboardingMethod,
            onboardingOrigin,
            candidateOnboardingOrigin
        } = currentSoftwareProduct;
        let screenToBreadcrumb;
        switch (screen) {
            case enums.SCREEN.ONBOARDING_CATALOG:
                return [];

            case enums.SCREEN.VERSIONS_PAGE:
                let firstMenuItems =
                    itemType === itemTypes.LICENSE_MODEL
                        ? [
                              {
                                  selectedKey: itemId,
                                  menuItems: [
                                      {
                                          key: itemId,
                                          displayText: props.itemName
                                      }
                                  ]
                              }
                          ]
                        : [
                              {
                                  selectedKey:
                                      props.additionalProps.licenseModelId ||
                                      currentSoftwareProduct.vendorId,
                                  menuItems: [
                                      {
                                          key: props.vendorId,
                                          displayText: props.vendorName
                                      }
                                  ]
                              },
                              {
                                  selectedKey:
                                      enums.BREADCRUMS.SOFTWARE_PRODUCT,
                                  menuItems: [
                                      {
                                          key: enums.BREADCRUMS.LICENSE_MODEL,
                                          displayText: i18n('License Model')
                                      },
                                      {
                                          key:
                                              enums.BREADCRUMS.SOFTWARE_PRODUCT,
                                          displayText: i18n('Software Products')
                                      }
                                  ]
                              },
                              {
                                  selectedKey: itemId,
                                  menuItems: [
                                      {
                                          key: itemId,
                                          displayText: props.itemName
                                      }
                                  ]
                              }
                          ];
                return [
                    ...firstMenuItems,
                    {
                        selectedKey: enums.BREADCRUMS.VERSIONS_PAGE,
                        menuItems: [
                            {
                                key: enums.BREADCRUMS.VERSIONS_PAGE,
                                displayText: i18n('Versions Page')
                            }
                        ]
                    }
                ];

            case enums.SCREEN.LICENSE_AGREEMENTS:
            case enums.SCREEN.FEATURE_GROUPS:
            case enums.SCREEN.ENTITLEMENT_POOLS:
            case enums.SCREEN.LICENSE_KEY_GROUPS:
            case enums.SCREEN.LICENSE_MODEL_OVERVIEW:
            case enums.SCREEN.ACTIVITY_LOG:
                screenToBreadcrumb = {
                    [enums.SCREEN.LICENSE_AGREEMENTS]:
                        enums.BREADCRUMS.LICENSE_AGREEMENTS,
                    [enums.SCREEN.FEATURE_GROUPS]:
                        enums.BREADCRUMS.FEATURE_GROUPS,
                    [enums.SCREEN.ENTITLEMENT_POOLS]:
                        enums.BREADCRUMS.ENTITLEMENT_POOLS,
                    [enums.SCREEN.LICENSE_KEY_GROUPS]:
                        enums.BREADCRUMS.LICENSE_KEY_GROUPS,
                    [enums.SCREEN.LICENSE_MODEL_OVERVIEW]:
                        enums.BREADCRUMS.LICENSE_MODEL_OVERVIEW,
                    [enums.SCREEN.ACTIVITY_LOG]: enums.BREADCRUMS.ACTIVITY_LOG
                };
                return [
                    {
                        selectedKey: currentLicenseModel.id,
                        menuItems: [
                            {
                                key: currentLicenseModel.id,
                                displayText: currentLicenseModel.vendorName
                            }
                        ]
                    },
                    {
                        selectedKey: enums.BREADCRUMS.LICENSE_MODEL,
                        menuItems: [
                            {
                                key: enums.BREADCRUMS.LICENSE_MODEL,
                                displayText: i18n('License Model')
                            }
                        ]
                    },
                    {
                        selectedKey: screenToBreadcrumb[screen],
                        menuItems: [
                            {
                                key: enums.BREADCRUMS.LICENSE_MODEL_OVERVIEW,
                                displayText: i18n('Overview')
                            },
                            {
                                key: enums.BREADCRUMS.LICENSE_AGREEMENTS,
                                displayText: i18n('License Agreements')
                            },
                            {
                                key: enums.BREADCRUMS.FEATURE_GROUPS,
                                displayText: i18n('Feature Groups')
                            },
                            {
                                key: enums.BREADCRUMS.ENTITLEMENT_POOLS,
                                displayText: i18n('Entitlement Pools')
                            },
                            {
                                key: enums.BREADCRUMS.LICENSE_KEY_GROUPS,
                                displayText: i18n('License Key Groups')
                            },
                            {
                                key: enums.BREADCRUMS.ACTIVITY_LOG,
                                displayText: i18n('Activity Log')
                            }
                        ]
                    }
                ];

            case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
            case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
            case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS:
            case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
            case enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT:
            case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
            case enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION:
            case enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION_MONITOR:
            case enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES:
            case enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:

            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES:
            case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
                screenToBreadcrumb = {
                    [enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_LANDING_PAGE,
                    [enums.SCREEN.SOFTWARE_PRODUCT_DETAILS]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_DETAILS,
                    [enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_ATTACHMENTS,
                    [enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_PROCESSES,
                    [enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_DEPLOYMENT,
                    [enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_NETWORKS,
                    [enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_VALIDATION,
                    [enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION_MONITOR]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_VALIDATION_MONITOR,
                    [enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_DEPENDENCIES,
                    [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS,
                    [enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_ACTIVITY_LOG
                };
                let componentScreenToBreadcrumb = {
                    [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_PROCESSES,
                    [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_COMPUTE,
                    [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_STORAGE,
                    [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_NETWORK,
                    [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_GENERAL,
                    [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING]:
                        enums.BREADCRUMS
                            .SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING,
                    [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_IMAGES,
                    [enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING]:
                        enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_MONITORING
                };

                let returnedBreadcrumb = [
                    {
                        selectedKey: currentSoftwareProduct.vendorId,
                        menuItems: [
                            {
                                key: currentSoftwareProduct.vendorId,
                                displayText: currentSoftwareProduct.vendorName
                            }
                        ]
                    },
                    {
                        selectedKey: enums.BREADCRUMS.SOFTWARE_PRODUCT,
                        menuItems: [
                            {
                                key: enums.BREADCRUMS.LICENSE_MODEL,
                                displayText: i18n('License Model')
                            },
                            {
                                key: enums.BREADCRUMS.SOFTWARE_PRODUCT,
                                displayText: i18n('Software Products')
                            }
                        ]
                    },
                    {
                        selectedKey: currentSoftwareProduct.id,
                        menuItems: [
                            {
                                key: currentSoftwareProduct.id,
                                displayText: currentSoftwareProduct.name
                            }
                        ]
                    },
                    .../*screen === enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE ? [] :*/ [
                        {
                            selectedKey:
                                screenToBreadcrumb[screen] ||
                                enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS,
                            menuItems: [
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_LANDING_PAGE,
                                    displayText: i18n('Overview')
                                },
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_DETAILS,
                                    displayText: i18n('General')
                                },
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_DEPLOYMENT,
                                    displayText: i18n('Deployment Flavors')
                                },
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_PROCESSES,
                                    displayText: i18n('Process Details')
                                },
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_NETWORKS,
                                    displayText: i18n('Networks')
                                },
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_VALIDATION,
                                    displayText: i18n('Validation')
                                },
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_VALIDATION_MONITOR,
                                    displayText: i18n('Validation Monitor')
                                },
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_DEPENDENCIES,
                                    displayText: i18n('Components Dependencies')
                                },
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_ATTACHMENTS,
                                    displayText: i18n('Attachments')
                                },
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_ACTIVITY_LOG,
                                    displayText: i18n('Activity Log')
                                },
                                {
                                    key:
                                        enums.BREADCRUMS
                                            .SOFTWARE_PRODUCT_COMPONENTS,
                                    displayText: i18n('Components')
                                }
                            ].filter(item => {
                                switch (item.key) {
                                    case enums.BREADCRUMS
                                        .SOFTWARE_PRODUCT_ATTACHMENTS:
                                        let isHeatData =
                                            onboardingOrigin !==
                                                onboardingOriginTypes.NONE ||
                                            candidateOnboardingOrigin ===
                                                onboardingOriginTypes.ZIP;
                                        return isHeatData;
                                    case enums.BREADCRUMS
                                        .SOFTWARE_PRODUCT_COMPONENTS:
                                        return componentsList.length > 0;
                                    case enums.BREADCRUMS
                                        .SOFTWARE_PRODUCT_DEPLOYMENT:
                                        let isManualMode =
                                            onboardingMethod ===
                                            onboardingMethodTypes.MANUAL;
                                        return isManualMode;
                                    case enums.BREADCRUMS
                                        .SOFTWARE_PRODUCT_DEPENDENCIES:
                                        return componentsList.length > 1;
                                    case enums.BREADCRUMS
                                        .SOFTWARE_PRODUCT_VALIDATION:
                                    case enums.BREADCRUMS
                                        .SOFTWARE_PRODUCT_VALIDATION_MONITOR:
                                        return !isVSPValidationDisabled;
                                    default:
                                        return true;
                                }
                            })
                        }
                    ]
                ];
                if (props.componentId) {
                    returnedBreadcrumb = [
                        ...returnedBreadcrumb,
                        {
                            selectedKey: props.componentId,
                            menuItems: componentsList.map(
                                ({ id, displayName }) => ({
                                    key: id,
                                    displayText: displayName
                                })
                            )
                        },
                        ...[
                            {
                                selectedKey:
                                    componentScreenToBreadcrumb[screen],
                                menuItems: [
                                    {
                                        key:
                                            enums.BREADCRUMS
                                                .SOFTWARE_PRODUCT_COMPONENT_GENERAL,
                                        displayText: i18n('General')
                                    },
                                    {
                                        key:
                                            enums.BREADCRUMS
                                                .SOFTWARE_PRODUCT_COMPONENT_COMPUTE,
                                        displayText: i18n('Compute')
                                    },
                                    {
                                        key:
                                            enums.BREADCRUMS
                                                .SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING,
                                        displayText: i18n(
                                            'High Availability & Load Balancing'
                                        )
                                    },
                                    {
                                        key:
                                            enums.BREADCRUMS
                                                .SOFTWARE_PRODUCT_COMPONENT_NETWORK,
                                        displayText: i18n('Networks')
                                    },
                                    {
                                        key:
                                            enums.BREADCRUMS
                                                .SOFTWARE_PRODUCT_COMPONENT_STORAGE,
                                        displayText: i18n('Storage')
                                    },
                                    {
                                        key:
                                            enums.BREADCRUMS
                                                .SOFTWARE_PRODUCT_COMPONENT_IMAGES,
                                        displayText: i18n('Images')
                                    },
                                    {
                                        key:
                                            enums.BREADCRUMS
                                                .SOFTWARE_PRODUCT_COMPONENT_PROCESSES,
                                        displayText: i18n('Process Details')
                                    },
                                    {
                                        key:
                                            enums.BREADCRUMS
                                                .SOFTWARE_PRODUCT_COMPONENT_MONITORING,
                                        displayText: i18n('Monitoring')
                                    }
                                ]
                            }
                        ]
                    ];
                }
                return returnedBreadcrumb;
        }
    }
}

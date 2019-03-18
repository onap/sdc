/*
 * Copyright © 2016-2017 European Support Limited
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

import Onboard from './onboard/Onboard.js';
import VersionsPage from './versionsPage/VersionsPage.js';
import LicenseModel from './licenseModel/LicenseModel.js';
import LicenseModelOverview from './licenseModel/overview/LicenseModelOverview.js';
import ActivityLog from 'sdc-app/common/activity-log/ActivityLog.js';

import LicenseAgreementListEditor from './licenseModel/licenseAgreement/LicenseAgreementListEditor.js';
import FeatureGroupListEditor from './licenseModel/featureGroups/FeatureGroupListEditor.js';
import LicenseKeyGroupsListEditor from './licenseModel/licenseKeyGroups/LicenseKeyGroupsListEditor.js';
import EntitlementPoolsListEditor from './licenseModel/entitlementPools/EntitlementPoolsListEditor.js';
import SoftwareProduct from './softwareProduct/SoftwareProduct.js';
import SoftwareProductLandingPage from './softwareProduct/landingPage/SoftwareProductLandingPage.js';
import SoftwareProductDetails from './softwareProduct/details/SoftwareProductDetails.js';
import SoftwareProductAttachments from './softwareProduct/attachments/SoftwareProductAttachments.js';
import SoftwareProductProcesses from './softwareProduct/processes/SoftwareProductProcesses.js';
import SoftwareProductDeployment from './softwareProduct/deployment/SoftwareProductDeployment.js';
import SoftwareProductNetworks from './softwareProduct/networks/SoftwareProductNetworks.js';
import SoftwareProductValidation from './softwareProduct/validation/SoftwareProductValidation.js';
import SoftwareProductValidationResults from './softwareProduct/validationResults/SoftwareProductValidationResults.js';
import SoftwareProductDependencies from './softwareProduct/dependencies/SoftwareProductDependencies.js';

import SoftwareProductComponentsList from './softwareProduct/components/SoftwareProductComponents.js';
import SoftwareProductComponentProcessesList from './softwareProduct/components/processes/SoftwareProductComponentProcessesList.js';
import SoftwareProductComponentStorage from './softwareProduct/components/storage/SoftwareProductComponentStorage.js';
import SoftwareProductComponentsNetworkList from './softwareProduct/components/network/SoftwareProductComponentsNetworkList.js';
import SoftwareProductComponentsGeneral from './softwareProduct/components/general/SoftwareProductComponentsGeneral.js';
import SoftwareProductComponentsCompute from './softwareProduct/components/compute/SoftwareProductComponentCompute.js';
import SoftwareProductComponentLoadBalancing from './softwareProduct/components/loadBalancing/SoftwareProductComponentLoadBalancing.js';
import SoftwareProductComponentsImageList from './softwareProduct/components/images/SoftwareProductComponentsImageList.js';
import SoftwareProductComponentsMonitoring from './softwareProduct/components/monitoring/SoftwareProductComponentsMonitoring.js';
import objectValues from 'lodash/values.js';
import PropTypes from 'prop-types';

import React from 'react';

import ReactDOM from 'react-dom';
import { enums } from './OnboardingConstants.js';

export default class OnboardingView extends React.Component {
    static propTypes = {
        currentScreen: PropTypes.shape({
            screen: PropTypes.oneOf(objectValues(enums.SCREEN)).isRequired,
            props: PropTypes.object.isRequired,
            itemPermission: PropTypes.object
        }).isRequired
    };

    componentDidMount() {
        let element = ReactDOM.findDOMNode(this);
        element.addEventListener('click', event => {
            if (event.target.tagName === 'A') {
                event.preventDefault();
            }
        });
        ['wheel', 'mousewheel', 'DOMMouseScroll'].forEach(eventType =>
            element.addEventListener(eventType, event =>
                event.stopPropagation()
            )
        );
    }

    render() {
        let { currentScreen, isLoading } = this.props;
        let { screen, props } = currentScreen;
        const preventClicks = isLoading ? 'no-pointer-events' : '';
        return (
            <div
                className={`dox-ui dox-ui-punch-out dox-ui-punch-out-full-page ${preventClicks}`}>
                {(() => {
                    switch (screen) {
                        case enums.SCREEN.ONBOARDING_CATALOG:
                            return <Onboard {...props} />;
                        case enums.SCREEN.VERSIONS_PAGE:
                            return <VersionsPage {...props} />;

                        case enums.SCREEN.LICENSE_AGREEMENTS:
                        case enums.SCREEN.FEATURE_GROUPS:
                        case enums.SCREEN.ENTITLEMENT_POOLS:
                        case enums.SCREEN.LICENSE_KEY_GROUPS:
                        case enums.SCREEN.LICENSE_MODEL_OVERVIEW:
                        case enums.SCREEN.ACTIVITY_LOG:
                            return (
                                <LicenseModel currentScreen={currentScreen}>
                                    {(() => {
                                        switch (screen) {
                                            case enums.SCREEN
                                                .LICENSE_MODEL_OVERVIEW:
                                                return (
                                                    <LicenseModelOverview
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .LICENSE_AGREEMENTS:
                                                return (
                                                    <LicenseAgreementListEditor
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN.FEATURE_GROUPS:
                                                return (
                                                    <FeatureGroupListEditor
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN.ENTITLEMENT_POOLS:
                                                return (
                                                    <EntitlementPoolsListEditor
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .LICENSE_KEY_GROUPS:
                                                return (
                                                    <LicenseKeyGroupsListEditor
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN.ACTIVITY_LOG:
                                                return (
                                                    <ActivityLog {...props} />
                                                );
                                        }
                                    })()}
                                </LicenseModel>
                            );

                        case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
                        case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
                        case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS:
                        case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
                        case enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT:
                        case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
                        case enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION:
                        case enums.SCREEN.SOFTWARE_PRODUCT_VALIDATION_RESULTS:
                        case enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES:
                        case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:
                        case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
                        case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
                        case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
                        case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
                        case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
                        case enums.SCREEN
                            .SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
                        case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES:
                        case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
                        case enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG:
                            return (
                                <SoftwareProduct currentScreen={currentScreen}>
                                    {(() => {
                                        switch (screen) {
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_LANDING_PAGE:
                                                return (
                                                    <SoftwareProductLandingPage
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_DETAILS:
                                                return (
                                                    <SoftwareProductDetails
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_ATTACHMENTS:
                                                return (
                                                    <SoftwareProductAttachments
                                                        className="no-padding-content-area"
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_PROCESSES:
                                                return (
                                                    <SoftwareProductProcesses
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_DEPLOYMENT:
                                                return (
                                                    <SoftwareProductDeployment
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_NETWORKS:
                                                return (
                                                    <SoftwareProductNetworks
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_VALIDATION:
                                                return (
                                                    <SoftwareProductValidation
                                                        className="no-padding-content-area"
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_VALIDATION_RESULTS:
                                                return (
                                                    <SoftwareProductValidationResults
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_DEPENDENCIES:
                                                return (
                                                    <SoftwareProductDependencies
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_COMPONENTS:
                                                return (
                                                    <SoftwareProductComponentsList
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
                                                return (
                                                    <SoftwareProductComponentProcessesList
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_COMPONENT_STORAGE:
                                                return (
                                                    <SoftwareProductComponentStorage
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_COMPONENT_NETWORK:
                                                return (
                                                    <SoftwareProductComponentsNetworkList
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_COMPONENT_GENERAL:
                                                return (
                                                    <SoftwareProductComponentsGeneral
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
                                                return (
                                                    <SoftwareProductComponentsCompute
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
                                                return (
                                                    <SoftwareProductComponentLoadBalancing
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_COMPONENT_IMAGES:
                                                return (
                                                    <SoftwareProductComponentsImageList
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_COMPONENT_MONITORING:
                                                return (
                                                    <SoftwareProductComponentsMonitoring
                                                        {...props}
                                                    />
                                                );
                                            case enums.SCREEN
                                                .SOFTWARE_PRODUCT_ACTIVITY_LOG:
                                                return (
                                                    <ActivityLog {...props} />
                                                );
                                        }
                                    })()}
                                </SoftwareProduct>
                            );
                    }
                })()}
            </div>
        );
    }
}

/*
 * Copyright © 2016-2018 European Support Limited
 * Modifications Copyright (C) 2026 Deutsche Telekom AG
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
import PropTypes from 'prop-types';
import OnboardingCatalogView from './onboardingCatalog/OnboardingCatalogView.jsx';
import WorkspaceView from './workspace/WorkspaceView.jsx';
import {
    tabsMapping,
    onboardTabIds,
    ONBOARD_TABPANEL_ID
} from './OnboardConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import classnames from 'classnames';
import ExpandableInput from 'nfvo-components/input/ExpandableInput.jsx';
import objectValues from 'lodash/values.js';
import { catalogItemTypes } from './onboardingCatalog/OnboardingCatalogConstants.js';
import NotificationsView from 'sdc-app/onboarding/userNotifications/NotificationsView.jsx';
import Filter from 'sdc-app/onboarding/onboard/filter/Filter.jsx';
const onboardTabs = [tabsMapping.WORKSPACE, tabsMapping.CATALOG];

export class OnboardHeaderTabs extends React.Component {
    static propTypes = {
        activeTab: PropTypes.number,
        onTabClick: PropTypes.func.isRequired
    };

    tabButtons = {};

    handleKeyDown(e, index) {
        const last = onboardTabs.length - 1;
        let next;
        switch (e.key) {
            case 'ArrowRight':
                next = index === last ? 0 : index + 1;
                break;
            case 'ArrowLeft':
                next = index === 0 ? last : index - 1;
                break;
            case 'Home':
                next = 0;
                break;
            case 'End':
                next = last;
                break;
            default:
                return;
        }
        e.preventDefault();
        const tab = onboardTabs[next];
        this.tabButtons[tab].focus();
        this.props.onTabClick(tab);
    }

    render() {
        const { activeTab, onTabClick } = this.props;
        const labels = {
            [tabsMapping.WORKSPACE]: i18n('WORKSPACE'),
            [tabsMapping.CATALOG]: i18n('ONBOARD CATALOG')
        };
        return (
            <div
                className="onboard-header-tabs"
                role="tablist"
                aria-label={i18n('Onboarding views')}>
                {onboardTabs.map((tab, index) => {
                    const isActive = activeTab === tab;
                    return (
                        <button
                            key={tab}
                            type="button"
                            role="tab"
                            id={onboardTabIds[tab]}
                            ref={button => (this.tabButtons[tab] = button)}
                            aria-selected={isActive}
                            aria-controls={ONBOARD_TABPANEL_ID}
                            tabIndex={isActive ? 0 : -1}
                            className={classnames('onboard-header-tab', {
                                active: isActive
                            })}
                            onClick={() => onTabClick(tab)}
                            onKeyDown={e => this.handleKeyDown(e, index)}
                            data-test-id={onboardTabIds[tab]}>
                            {labels[tab]}
                        </button>
                    );
                })}
            </div>
        );
    }
}

const OnboardHeader = ({ onSearch, activeTab, onTabClick, searchValue }) => (
    <div className="onboard-header">
        <OnboardHeaderTabs activeTab={activeTab} onTabClick={onTabClick} />
        <ExpandableInput
            onChange={onSearch}
            iconType="search"
            value={searchValue}
        />
        <NotificationsView />
    </div>
);

class OnboardView extends React.Component {
    static propTypes = {
        licenseModelList: PropTypes.array,
        softwareProductList: PropTypes.array,
        finalizedLicenseModelList: PropTypes.array,
        finalizedSoftwareProductList: PropTypes.array,
        archivedSoftwareProductList: PropTypes.array,
        archivedLicenseModelList: PropTypes.array,
        modalToShow: PropTypes.oneOf(objectValues(catalogItemTypes)),
        onSelectLicenseModel: PropTypes.func.isRequired,
        onSelectSoftwareProduct: PropTypes.func.isRequired,
        onAddLicenseModelClick: PropTypes.func.isRequired,
        onAddSoftwareProductClick: PropTypes.func.isRequired,
        closeVspOverlay: PropTypes.func.isRequired,
        onVspOverlayChange: PropTypes.func.isRequired,
        onTabClick: PropTypes.func.isRequired,
        onCatalogTabClick: PropTypes.func.isRequired,
        onSearch: PropTypes.func.isRequired,
        activeTab: PropTypes.number.isRequired,
        catalogActiveTab: PropTypes.number.isRequired,
        searchValue: PropTypes.object.isRequired,
        onMigrate: PropTypes.func.isRequired
    };
    renderViewByTab(activeTab) {
        switch (activeTab) {
            case tabsMapping.WORKSPACE:
                return <WorkspaceView {...this.props} />;
            case tabsMapping.CATALOG:
                return <OnboardingCatalogView {...this.props} />;
            default:
                return <WorkspaceView {...this.props} />;
        }
    }

    render() {
        let {
            activeTab,
            activeTabName,
            onTabClick,
            onSearch,
            searchValue
        } = this.props;
        return (
            <div className="catalog-view">
                <Filter />
                <div className="catalog-parts">
                    <OnboardHeader
                        activeTab={activeTab}
                        onTabClick={onTabClick}
                        searchValue={searchValue[activeTabName]}
                        onSearch={value => onSearch(value, activeTabName)}
                    />
                    {this.renderViewByTab(activeTab)}
                </div>
            </div>
        );
    }
}

export default OnboardView;

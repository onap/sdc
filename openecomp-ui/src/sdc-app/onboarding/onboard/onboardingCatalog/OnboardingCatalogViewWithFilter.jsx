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
import i18n from 'nfvo-utils/i18n/i18n.js';
import DetailsCatalogView from 'sdc-app/onboarding/onboard/DetailsCatalogView.jsx';
import VendorCatalogView from './VendorCatalogView.jsx';
import { tabsMapping } from './OnboardingCatalogConstants.js';
import { tabsMapping as WCTabsMapping } from 'sdc-app/onboarding/onboard/OnboardConstants.js';

const CatalogHeader = () => (
    <div className="catalog-header">
        <div className="catalog-header-tabs">
            <div className="catalog-header-tab active">
                {i18n('ONBOARD CATALOG')}
            </div>
        </div>
    </div>
);

class OnboardingCatalogView extends React.Component {
    renderViewByTab(activeTab) {
        const {
            users,
            vspOverlay,
            onSelectLicenseModel,
            onSelectSoftwareProduct,
            onAddLicenseModelClick,
            onAddSoftwareProductClick,
            onVspOverlayChange,
            onVendorSelect,
            selectedVendor,
            searchValue,
            onMigrate,
            filteredItems
        } = this.props;

        const { vlmList, vspList } = filteredItems;

        switch (activeTab) {
            case tabsMapping.ARCHIVE:
                return (
                    <DetailsCatalogView
                        VLMList={vlmList}
                        VSPList={vspList}
                        users={users}
                        onSelectVLM={(item, users) =>
                            onSelectLicenseModel(
                                item,
                                users,
                                WCTabsMapping.CATALOG
                            )
                        }
                        onSelectVSP={(item, users) =>
                            onSelectSoftwareProduct(
                                item,
                                users,
                                WCTabsMapping.CATALOG
                            )
                        }
                        filter={searchValue}
                        onMigrate={onMigrate}
                    />
                );
            case tabsMapping.ACTIVE:
                return (
                    <DetailsCatalogView
                        VLMList={vlmList}
                        VSPList={vspList}
                        users={users}
                        onAddVLM={onAddLicenseModelClick}
                        onAddVSP={onAddSoftwareProductClick}
                        onSelectVLM={(item, users) =>
                            onSelectLicenseModel(
                                item,
                                users,
                                WCTabsMapping.CATALOG
                            )
                        }
                        onSelectVSP={(item, users) =>
                            onSelectSoftwareProduct(
                                item,
                                users,
                                WCTabsMapping.CATALOG
                            )
                        }
                        filter={searchValue}
                        onMigrate={onMigrate}
                    />
                );
            case tabsMapping.BY_VENDOR:
            default:
                return (
                    <VendorCatalogView
                        licenseModelList={vlmList}
                        users={users}
                        onAddVSP={onAddSoftwareProductClick}
                        onAddVLM={onAddLicenseModelClick}
                        onSelectVSP={(item, users) =>
                            onSelectSoftwareProduct(
                                item,
                                users,
                                WCTabsMapping.CATALOG
                            )
                        }
                        onSelectVLM={(item, users) =>
                            onSelectLicenseModel(
                                item,
                                users,
                                WCTabsMapping.CATALOG
                            )
                        }
                        vspOverlay={vspOverlay}
                        onVendorSelect={onVendorSelect}
                        selectedVendor={selectedVendor}
                        onVspOverlayChange={onVspOverlayChange}
                        onMigrate={onMigrate}
                        filter={searchValue}
                    />
                );
        }
    }

    render() {
        const { selectedVendor, catalogActiveTab: activeTab } = this.props;
        return (
            <div className="catalog-wrapper">
                {!selectedVendor && <CatalogHeader />}
                {this.renderViewByTab(activeTab)}
            </div>
        );
    }
}

export default OnboardingCatalogView;

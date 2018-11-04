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

import { connect } from 'react-redux';
import OnboardView from './OnboardView.jsx';
import OnboardingCatalogActionHelper from './onboardingCatalog/OnboardingCatalogActionHelper.js';
import OnboardActionHelper from './OnboardActionHelper.js';
import LicenseModelCreationActionHelper from '../licenseModel/creation/LicenseModelCreationActionHelper.js';
import SoftwareProductCreationActionHelper from '../softwareProduct/creation/SoftwareProductCreationActionHelper.js';
import sortByStringProperty from 'nfvo-utils/sortByStringProperty.js';
import { tabsMapping } from './onboardingCatalog/OnboardingCatalogConstants.js';
import { tabsMapping as onboardTabsMapping } from './OnboardConstants';
import { itemStatus } from 'sdc-app/common/helpers/ItemsHelperConstants.js';
import { catalogItemStatuses } from './onboardingCatalog/OnboardingCatalogConstants.js';

export const mapStateToProps = ({
    onboard: { onboardingCatalog, activeTab, searchValue, filter },
    licenseModelList,
    users,
    archivedLicenseModelList,
    archivedSoftwareProductList,
    finalizedLicenseModelList,
    softwareProductList,
    finalizedSoftwareProductList,
    filteredItems
}) => {
    const activeTabName = Object.keys(onboardTabsMapping).filter(item => {
        return onboardTabsMapping[item] === activeTab;
    })[0];
    const fullSoftwareProducts = softwareProductList
        .filter(
            vsp =>
                !finalizedSoftwareProductList.find(fvsp => fvsp.id === vsp.id)
        )
        .concat(finalizedSoftwareProductList);

    const reduceLicenseModelList = (accum, vlm) => {
        let currentSoftwareProductList = sortByStringProperty(
            fullSoftwareProducts.filter(vsp => vsp.vendorId === vlm.id),
            'name'
        );
        accum.push({ ...vlm, softwareProductList: currentSoftwareProductList });
        return accum;
    };

    const reduceFilteredLicenseModelList = (accum, vlm) => {
        let currentSoftwareProductList = sortByStringProperty(
            filteredItems.vspList.filter(vsp => vsp.vendorId === vlm.id),
            'name'
        );
        accum.push({ ...vlm, softwareProductList: currentSoftwareProductList });
        return accum;
    };

    const updatedFilteredItems = {
        vspList: [...filteredItems.vspList],
        vlmList: sortByStringProperty(
            filteredItems.vlmList.reduce(reduceFilteredLicenseModelList, []),
            'name'
        )
    };

    licenseModelList = sortByStringProperty(
        licenseModelList.reduce(reduceLicenseModelList, []),
        'name'
    );

    finalizedLicenseModelList = sortByStringProperty(
        finalizedLicenseModelList.reduce(reduceLicenseModelList, []),
        'name'
    );

    const fullLicenseModelList = licenseModelList
        .filter(
            vlm => !finalizedLicenseModelList.find(fvlm => fvlm.id === vlm.id)
        )
        .concat(finalizedLicenseModelList);

    let {
        activeTab: catalogActiveTab,
        vendorCatalog: { vspOverlay, selectedVendor }
    } = onboardingCatalog;
    if (filter.byVendorView) {
        catalogActiveTab = tabsMapping.BY_VENDOR;
    } else if (filter.itemStatus && filter.itemStatus === itemStatus.ARCHIVED) {
        catalogActiveTab = tabsMapping.ARCHIVE;
    }

    return {
        isArchived: filter.itemStatus === catalogItemStatuses.ARCHIVED,
        finalizedLicenseModelList,
        finalizedSoftwareProductList,
        licenseModelList,
        softwareProductList,
        archivedLicenseModelList,
        archivedSoftwareProductList,
        fullLicenseModelList,
        activeTabName,
        activeTab,
        catalogActiveTab,
        searchValue,
        vspOverlay,
        selectedVendor,
        users: users.usersList,
        filteredItems: updatedFilteredItems
    };
};

const mapActionsToProps = dispatch => {
    return {
        onSelectLicenseModel({ id: licenseModelId, name }, users, tab) {
            OnboardActionHelper.loadVLMScreen(
                dispatch,
                { id: licenseModelId, name },
                users,
                tab
            );
        },
        onSelectSoftwareProduct(softwareProduct, users, tab) {
            OnboardActionHelper.loadVSPScreen(
                dispatch,
                softwareProduct,
                users,
                tab
            );
        },
        onAddSoftwareProductClick: vendorId =>
            SoftwareProductCreationActionHelper.open(dispatch, vendorId),
        onAddLicenseModelClick: () =>
            LicenseModelCreationActionHelper.open(dispatch),
        onVspOverlayChange: vendor =>
            OnboardingCatalogActionHelper.changeVspOverlay(dispatch, vendor),
        closeVspOverlay: () =>
            OnboardingCatalogActionHelper.closeVspOverlay(dispatch),
        onCatalogTabClick: tab =>
            OnboardingCatalogActionHelper.changeActiveTab(dispatch, tab),
        onTabClick: tab => OnboardActionHelper.changeActiveTab(dispatch, tab),
        onSearch: (searchValue, activeTab) =>
            OnboardActionHelper.changeSearchValue(
                dispatch,
                searchValue,
                activeTab
            ),
        onVendorSelect: vendor =>
            OnboardingCatalogActionHelper.onVendorSelect(dispatch, { vendor }),
        onMigrate: ({ softwareProduct }) =>
            OnboardingCatalogActionHelper.onMigrate(dispatch, softwareProduct)
    };
};

export default connect(mapStateToProps, mapActionsToProps)(OnboardView);

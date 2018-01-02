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
import {tabsMapping, actionTypes} from './OnboardConstants.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import {enums, screenTypes} from 'sdc-app/onboarding/OnboardingConstants.js';
import VersionsPageActionHelper from 'sdc-app/onboarding/versionsPage/VersionsPageActionHelper.js';
import {catalogItemStatuses} from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogConstants.js';
import {itemTypes} from 'sdc-app/onboarding/versionsPage/VersionsPageConstants.js';
import PermissionsActionHelper from 'sdc-app/onboarding/permissions/PermissionsActionHelper.js';

const OnboardActionHelper = {
	resetOnboardStore(dispatch) {
		dispatch({
			type: actionTypes.RESET_ONBOARD_STORE
		});
	},
	changeActiveTab(dispatch, activeTab) {
		this.clearSearchValue(dispatch);
		dispatch({
			type: actionTypes.CHANGE_ACTIVE_ONBOARD_TAB,
			activeTab
		});
	},
	changeSearchValue(dispatch, searchValue) {
		dispatch({
			type: actionTypes.CHANGE_SEARCH_VALUE,
			searchValue
		});
	},
	clearSearchValue(dispatch) {
		dispatch({
			type: actionTypes.CHANGE_SEARCH_VALUE,
			searchValue: ''
		});
	},

	loadVLMScreen(dispatch, {id: licenseModelId, name}, users, tab) {
		if (tab === tabsMapping.WORKSPACE) {
			VersionsPageActionHelper.fetchVersions(dispatch, {itemId: licenseModelId, itemType: itemTypes.LICENSE_MODEL}).then(({results})=> {
				results = results.filter((version) => version.status === catalogItemStatuses.DRAFT);
				if (results.length !== 1) {
					ScreensHelper.loadScreen(dispatch, {
						screen: enums.SCREEN.VERSIONS_PAGE, screenType: screenTypes.LICENSE_MODEL,
						props: {licenseModelId, licenseModel: {name}, usersList: users}
					});
				}
				else {
					PermissionsActionHelper.fetchItemUsers(dispatch, {itemId: licenseModelId, allUsers: users}).then(() =>
						ScreensHelper.loadLandingScreen(dispatch, {screenType: screenTypes.LICENSE_MODEL, props: {licenseModelId, version: results[0]}})
					);
				}
			});
		}
		if (tab === tabsMapping.CATALOG) {
			ScreensHelper.loadScreen(dispatch, {
				screen: enums.SCREEN.VERSIONS_PAGE, screenType: screenTypes.LICENSE_MODEL,
				props: {licenseModelId, licenseModel: {name}, usersList: users}
			});
		}
	},
	loadVSPScreen(dispatch, softwareProduct, users, tab) {
		let {id: softwareProductId, vendorId: licenseModelId, licensingVersion, name} = softwareProduct;
		if (tab === tabsMapping.WORKSPACE) {
			VersionsPageActionHelper.fetchVersions(dispatch,{itemId: softwareProductId, itemType: itemTypes.SOFTWARE_PRODUCT}).then(({results})=> {
				results = results.filter((version) => version.status === catalogItemStatuses.DRAFT);
				if (results.length !== 1) {
					ScreensHelper.loadScreen(dispatch, {
						screen: enums.SCREEN.SOFTWARE_PRODUCT_VERSIONS_PAGE, screenType: screenTypes.SOFTWARE_PRODUCT,
						props: {
							softwareProductId,
							softwareProduct: {name, vendorId: licenseModelId, licensingVersion},
							usersList: users
						}
					});
				}
				else {
					PermissionsActionHelper.fetchItemUsers(dispatch, {itemId: softwareProductId, allUsers: users}).then(() =>
						ScreensHelper.loadLandingScreen(dispatch, {screenType: screenTypes.SOFTWARE_PRODUCT, props: {softwareProductId, licenseModelId, version: results[0]}})
					);
				}
			});
		}
		if (tab === tabsMapping.CATALOG) {
			ScreensHelper.loadScreen(dispatch, {
				screen: enums.SCREEN.SOFTWARE_PRODUCT_VERSIONS_PAGE, screenType: screenTypes.SOFTWARE_PRODUCT,
				props: {
					softwareProductId,
					softwareProduct: {name, vendorId: licenseModelId, licensingVersion},
					usersList: users
				}
			});
		}
	}
};

export default OnboardActionHelper;

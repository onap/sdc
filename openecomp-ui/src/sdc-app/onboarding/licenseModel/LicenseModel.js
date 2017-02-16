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

import {connect} from 'react-redux';

import i18n from 'nfvo-utils/i18n/i18n.js';
import {statusEnum as versionStatusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import TabulatedEditor from 'src/nfvo-components/editor/TabulatedEditor.jsx';

import {enums} from 'sdc-app/onboarding/OnboardingConstants.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';

import {navigationItems} from './LicenseModelConstants.js';
import LicenseModelActionHelper from './LicenseModelActionHelper.js';
import LicenseAgreementActionHelper from './licenseAgreement/LicenseAgreementActionHelper.js';
import FeatureGroupsActionHelper from './featureGroups/FeatureGroupsActionHelper.js';
import EntitlementPoolsActionHelper from './entitlementPools/EntitlementPoolsActionHelper.js';
import LicenseKeyGroupsActionHelper from './licenseKeyGroups/LicenseKeyGroupsActionHelper.js';


const buildNavigationBarProps = (licenseModel, screen) => {
	const {id, vendorName, version} = licenseModel;
	const meta = {version};

	const groups = [{
		id,
		name: vendorName,
		items: [
			{
				id: navigationItems.LICENSE_AGREEMENTS,
				name: i18n('License Agreements'),
				meta
			},
			{
				id: navigationItems.FEATURE_GROUPS,
				name: i18n('Feature Groups'),
				meta
			},
			{
				id: navigationItems.ENTITLEMENT_POOLS,
				name: i18n('Entitlement Pools'),
				meta
			},
			{
				id: navigationItems.LICENSE_KEY_GROUPS,
				name: i18n('License Key Groups'),
				meta
			}
		]
	}];

	const activeItemId = ({
		[enums.SCREEN.LICENSE_AGREEMENTS]: navigationItems.LICENSE_AGREEMENTS,
		[enums.SCREEN.FEATURE_GROUPS]: navigationItems.FEATURE_GROUPS,
		[enums.SCREEN.ENTITLEMENT_POOLS]: navigationItems.ENTITLEMENT_POOLS,
		[enums.SCREEN.LICENSE_KEY_GROUPS]: navigationItems.LICENSE_KEY_GROUPS
	})[screen];

	return {
		activeItemId, groups
	};
};


const buildVersionControllerProps = (licenseModel) => {
	let {version, viewableVersions, status: currentStatus, lockingUser} = licenseModel;
	let {status, isCheckedOut} = (currentStatus === versionStatusEnum.CHECK_OUT_STATUS) ?
		VersionControllerUtils.getCheckOutStatusKindByUserID(currentStatus, lockingUser) :
		{status: currentStatus, isCheckedOut: false};

	return {
		version,
		viewableVersions,
		status,
		isCheckedOut
	};
};


const mapStateToProps = ({licenseModel: {licenseModelEditor}}, {currentScreen: {screen}}) => {
	return {
		versionControllerProps: buildVersionControllerProps(licenseModelEditor.data),
		navigationBarProps: buildNavigationBarProps(licenseModelEditor.data, screen)
	};
};


const mapActionsToProps = (dispatch, {currentScreen: {screen, props: {licenseModelId}}}) => {
	return {
		onVersionControllerAction: action =>
			LicenseModelActionHelper.performVCAction(dispatch, {licenseModelId, action}).then(() => {
				switch(screen) {
					case enums.SCREEN.LICENSE_AGREEMENTS:
						LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {licenseModelId});
						break;
					case enums.SCREEN.FEATURE_GROUPS:
						FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId});
						break;
					case enums.SCREEN.ENTITLEMENT_POOLS:
						EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {licenseModelId});
						break;
					case enums.SCREEN.LICENSE_KEY_GROUPS:
						LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {licenseModelId});
						break;
				}
			}),
		onVersionSwitching: version => LicenseAgreementActionHelper.switchVersion(dispatch, {licenseModelId, version}),
		onClose: () => OnboardingActionHelper.navigateToOnboardingCatalog(dispatch),

		onNavigate: ({id, meta: {version}}) => {
			switch(id) {
				case navigationItems.LICENSE_AGREEMENTS:
					OnboardingActionHelper.navigateToLicenseAgreements(dispatch, {licenseModelId, version});
					break;
				case navigationItems.FEATURE_GROUPS:
					OnboardingActionHelper.navigateToFeatureGroups(dispatch, {licenseModelId, version});
					break;
				case navigationItems.ENTITLEMENT_POOLS:
					OnboardingActionHelper.navigateToEntitlementPools(dispatch, {licenseModelId, version});
					break;
				case navigationItems.LICENSE_KEY_GROUPS:
					OnboardingActionHelper.navigateToLicenseKeyGroups(dispatch, {licenseModelId, version});
					break;
			}
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(TabulatedEditor);

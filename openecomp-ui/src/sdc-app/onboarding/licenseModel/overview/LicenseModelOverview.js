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
import {connect} from 'react-redux';
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import LicenseModelOverviewView from './LicenseModelOverviewView.jsx';
import {overviewEditorHeaders, selectedButton} from './LicenseModelOverviewConstants.js';
import licenseModelOverviewActionHelper from './licenseModelOverviewActionHelper.js';

export const mapStateToProps = ({licenseModel: {licenseModelEditor, entitlementPool, licenseAgreement, featureGroup, licenseKeyGroup, licenseModelOverview}}) => {

	let modalHeader, licensingDataList;
	let isDisplayModal = false;

	const reduceLicenseKeyGroups = (accum, licenseKeyGroupId) => {
		let curLicenseKeyGroup = licenseKeyGroup.licenseKeyGroupsList.find(item => {return item.id === licenseKeyGroupId;});
		if (curLicenseKeyGroup) {
			accum.push({
				...curLicenseKeyGroup,
				itemType: overviewEditorHeaders.LICENSE_KEY_GROUP
			});
		}
		return accum;
	};

	const reduceEntitlementPools = (accum, entitlementPoolId) => {
		let curEntitlementPool = entitlementPool.entitlementPoolsList.find(item => {return item.id === entitlementPoolId;});
		if (curEntitlementPool) {
			accum.push ({
				...curEntitlementPool,
				itemType: overviewEditorHeaders.ENTITLEMENT_POOL
			});
		}
		return accum;
	};

	const reduceFeatureGroups = (accum, featureGroupId) => {
		let curFeatureGroup = featureGroup.featureGroupsList.find(item => {return item.id === featureGroupId;});
		if (curFeatureGroup) {
			let {entitlementPoolsIds = [], licenseKeyGroupsIds = []} = curFeatureGroup;
			accum.push({
				...curFeatureGroup,
				itemType: overviewEditorHeaders.FEATURE_GROUP,
				children: [
					...entitlementPoolsIds.length ? entitlementPoolsIds.reduce(reduceEntitlementPools, []) : [],
					...licenseKeyGroupsIds.length ? licenseKeyGroupsIds.reduce(reduceLicenseKeyGroups, []) : []
				]
			});
		}
		return accum;
	};


	const checkEP  = (accum, elem) => {
		if (!elem.referencingFeatureGroups || !elem.referencingFeatureGroups.length) {
			accum.push({
				...elem,
				itemType: overviewEditorHeaders.ENTITLEMENT_POOL
			});
		}
		return accum;
	};

	const checkLG = (accum, elem) => {
		if (!elem.referencingFeatureGroups || !elem.referencingFeatureGroups.length) {
			accum.push({
				...elem,
				itemType: overviewEditorHeaders.LICENSE_KEY_GROUP
			});
		}
		return accum;
	};

	const checkFG = (accum, elem) => {
		if (!elem.referencingLicenseAgreements || !elem.referencingLicenseAgreements.length) {
			let {entitlementPoolsIds = [], licenseKeyGroupsIds = []} = elem;
			accum.push({
				...elem,
				itemType: overviewEditorHeaders.FEATURE_GROUP,

				children: [
					...entitlementPoolsIds.length ? entitlementPoolsIds.reduce(reduceEntitlementPools, []) : [],
					...licenseKeyGroupsIds.length ? licenseKeyGroupsIds.reduce(reduceLicenseKeyGroups, []) : []
				]

			});
		}
		return accum;
	};



	const mapLicenseAgreementData = licenseAgreement => {
		let {featureGroupsIds = []} = licenseAgreement;
		return {
			...licenseAgreement,
			itemType: overviewEditorHeaders.LICENSE_AGREEMENT,
			children: featureGroupsIds.length ? featureGroupsIds.reduce(reduceFeatureGroups, []) : []
		};
	};

	if (entitlementPool.entitlementPoolEditor && entitlementPool.entitlementPoolEditor.data) {
		modalHeader = overviewEditorHeaders.ENTITLEMENT_POOL;
		isDisplayModal = true;
	}else
	if (licenseAgreement.licenseAgreementEditor && licenseAgreement.licenseAgreementEditor.data) {
		modalHeader = overviewEditorHeaders.LICENSE_AGREEMENT;
		isDisplayModal = true;
	}else
	if (featureGroup.featureGroupEditor && featureGroup.featureGroupEditor.data) {
		modalHeader = overviewEditorHeaders.FEATURE_GROUP;
		isDisplayModal = true;
	}else
	if (licenseKeyGroup.licenseKeyGroupsEditor && licenseKeyGroup.licenseKeyGroupsEditor.data) {
		modalHeader = overviewEditorHeaders.LICENSE_KEY_GROUP;
		isDisplayModal = true;
	}
	let orphanDataList = [
		...featureGroup.featureGroupsList.reduce(checkFG, []),
		...entitlementPool.entitlementPoolsList.reduce(checkEP, []),
		...licenseKeyGroup.licenseKeyGroupsList.reduce(checkLG, [])
	];

	licensingDataList = licenseAgreement.licenseAgreementList && licenseAgreement.licenseAgreementList.length ? licenseAgreement.licenseAgreementList.map(mapLicenseAgreementData) : [];
	let selectedTab = licenseModelOverview.selectedTab;
	// on first entry, we will decide what tab to open depending on data. if there are no connections, we will open the orphans
	if (selectedTab === null) {
		selectedTab = (licensingDataList.length) ? selectedButton.VLM_LIST_VIEW : selectedButton.NOT_IN_USE;
	}
	return {
		isReadOnlyMode: VersionControllerUtils.isReadOnly(licenseModelEditor.data),
		isDisplayModal,
		modalHeader,
		licenseModelId: licenseModelEditor.data.id,
		version: licenseModelEditor.data.version,
		licensingDataList,
		orphanDataList,
		selectedTab
	};
};

const mapActionsToProps = (dispatch, {licenseModelId}) => {
	return {
		onCallVCAction: action => {
			LicenseModelActionHelper.performVCAction(dispatch, {licenseModelId, action});
		},
		onTabSelect: (buttonTab) => licenseModelOverviewActionHelper.selectVLMListView(dispatch,{buttonTab})
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LicenseModelOverviewView);

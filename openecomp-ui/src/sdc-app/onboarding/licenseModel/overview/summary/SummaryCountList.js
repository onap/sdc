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
import React from 'react';
import {connect} from 'react-redux';

import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';

import OnboardingActionHelper from '../../../OnboardingActionHelper.js';
import EntitlementPoolsActionHelper from '../../entitlementPools/EntitlementPoolsActionHelper.js';
import LicenseAgreementActionHelper from '../../licenseAgreement/LicenseAgreementActionHelper.js';
import LicenseKeyGroupsActionHelper from '../../licenseKeyGroups/LicenseKeyGroupsActionHelper.js';
import FeatureGroupsActionHelper from '../../featureGroups/FeatureGroupsActionHelper.js';

import {overviewItems} from '../LicenseModelOverviewConstants.js';
import SummaryCountItem from './SummaryCountItem.jsx';

export const mapStateToProps = ({licenseModel: {licenseModelEditor, licenseAgreement: {licenseAgreementList},
	featureGroup: {featureGroupsList}, entitlementPool: {entitlementPoolsList}, licenseKeyGroup: {licenseKeyGroupsList}}}) => {

	let {vendorName, description, id, version} = licenseModelEditor.data;

	let isReadOnlyMode = VersionControllerUtils.isReadOnly(licenseModelEditor.data);

	let counts = [
		{name: overviewItems.LICENSE_AGREEMENTS, count: licenseAgreementList.length},
		{name: overviewItems.FEATURE_GROUPS, count: featureGroupsList.length},
		{name: overviewItems.ENTITLEMENT_POOLS, count: entitlementPoolsList.length},
		{name: overviewItems.LICENSE_KEY_GROUPS, count: licenseKeyGroupsList.length},
	];
	return {
		vendorName,
		licenseModelId: id,
		description,
		counts,
		isReadOnlyMode,
		version
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onEditorOpenClick: (name, licenseModelId, version) => {
			switch (name) {
				case overviewItems.ENTITLEMENT_POOLS:
					EntitlementPoolsActionHelper.openEntitlementPoolsEditor(dispatch);
					break;
				case overviewItems.FEATURE_GROUPS:
					FeatureGroupsActionHelper.openFeatureGroupsEditor(dispatch, {licenseModelId, version});
					break;
				case overviewItems.LICENSE_AGREEMENTS:
					LicenseAgreementActionHelper.openLicenseAgreementEditor(dispatch, {licenseModelId, version});
					break;
				case overviewItems.LICENSE_KEY_GROUPS:
					LicenseKeyGroupsActionHelper.openLicenseKeyGroupsEditor(dispatch);
					break;
				default:
					break;
			}
		},
		onNavigateClick: ({name, licenseModelId, version}) => {
			switch (name) {
				case overviewItems.ENTITLEMENT_POOLS:
					OnboardingActionHelper.navigateToEntitlementPools(dispatch, {licenseModelId, version});
					break;
				case overviewItems.FEATURE_GROUPS:
					OnboardingActionHelper.navigateToFeatureGroups(dispatch, {licenseModelId, version});
					break;
				case overviewItems.LICENSE_AGREEMENTS:
					OnboardingActionHelper.navigateToLicenseAgreements(dispatch, {licenseModelId, version});
					break;
				case overviewItems.LICENSE_KEY_GROUPS:
					OnboardingActionHelper.navigateToLicenseKeyGroups(dispatch, {licenseModelId, version});
					break;
				default:
					break;
			}
		}
	};
};

export class SummaryCountList extends React.Component {

	render() {
		let {counts} = this.props;
		return(
			<div className='summary-count-list'>
				{counts.map(item => this.renderItem(item))}
			</div>
		);
	}

	renderItem(item){
		const {name, count} = item;
		const {isReadOnlyMode} = this.props;
		return(
			<SummaryCountItem isReadOnlyMode={isReadOnlyMode} name={name} counter={count} onNavigate={() => this.onNavigate(name)} onAdd={() => this.onAdd(name)} key={name} />
		);
	}

	onAdd(name) {
		let {onEditorOpenClick, licenseModelId, isReadOnlyMode, version} = this.props;
		if (!isReadOnlyMode) {
			onEditorOpenClick(name, licenseModelId, version);
		}
	}

	onNavigate(name) {
		let {onNavigateClick, licenseModelId, version} = this.props;
		onNavigateClick({licenseModelId, name, version});
	}
}

export default connect(mapStateToProps, mapActionsToProps)(SummaryCountList);

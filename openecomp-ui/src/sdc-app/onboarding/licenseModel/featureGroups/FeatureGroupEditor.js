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

import FeatureGroupsActionHelper from './FeatureGroupsActionHelper.js';
import FeatureGroupEditorView from './FeatureGroupEditorView.jsx';

const mapStateToProps = ({licenseModel: {featureGroup, entitlementPool, licenseKeyGroup}}) => {

	const {featureGroupEditor} = featureGroup;

	let {data, selectedTab, selectedEntitlementPoolsButtonTab, selectedLicenseKeyGroupsButtonTab} = featureGroupEditor;

	let previousData;
	const featureGroupId = data ? data.id : null;
	if (featureGroupId) {
		previousData = featureGroup.featureGroupsList.find(featureGroup => featureGroup.id === featureGroupId);
	}
	let {entitlementPoolsList = []} = entitlementPool;
	let {licenseKeyGroupsList = []} = licenseKeyGroup;

	return {
		data,
		previousData,
		selectedTab,
		selectedEntitlementPoolsButtonTab,
		selectedLicenseKeyGroupsButtonTab,
		entitlementPoolsList,
		licenseKeyGroupsList
	};
};


const mapActionsToProps = (dispatch, {licenseModelId}) => {
	return {
		onTabSelect: tab => FeatureGroupsActionHelper.selectEntitlementPoolsEditorTab(dispatch, {tab}),
		onEntitlementPoolsButtonTabSelect: buttonTab => FeatureGroupsActionHelper.selectFeatureGroupsEditorEntitlementPoolsButtonTab(dispatch, {buttonTab}),
		onLicenseKeyGroupsButtonTabSelect: buttonTab => FeatureGroupsActionHelper.selectFeatureGroupsEditorLicenseKeyGroupsButtonTab(dispatch, {buttonTab}),
		onDataChanged: deltaData => FeatureGroupsActionHelper.featureGroupsEditorDataChanged(dispatch, {deltaData}),
		onSubmit: (previousFeatureGroup, featureGroup) => {
			FeatureGroupsActionHelper.closeFeatureGroupsEditor(dispatch);
			FeatureGroupsActionHelper.saveFeatureGroup(dispatch, {licenseModelId, previousFeatureGroup, featureGroup});
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps)(FeatureGroupEditorView);


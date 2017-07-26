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

import FeatureGroupsActionHelper from './FeatureGroupsActionHelper.js';
import FeatureGroupEditorView from './FeatureGroupEditorView.jsx';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';

export const mapStateToProps = ({licenseModel: {featureGroup, entitlementPool, licenseKeyGroup}}) => {
	let {entitlementPoolsList = []} = entitlementPool;
	let {licenseKeyGroupsList = []} = licenseKeyGroup;
	const {featureGroupEditor} = featureGroup;
	let {data, selectedTab, genericFieldInfo, formReady} = featureGroupEditor;
	const featureGroupId = data ? data.id : null;
	const list = featureGroup.featureGroupsList;

	let previousData, FGNames = {}, isFormValid = true, invalidTabs = [];

	if (featureGroupId) {
		previousData = list.find(featureGroup => featureGroup.id === featureGroupId);
	}

	for (let i = 0; i < list.length; i++) {
		FGNames[list[i].name.toLowerCase()] = list[i].id;
	}

	for (let field in genericFieldInfo) {
		if (!genericFieldInfo[field].isValid) {
			isFormValid = false;
			let tabId = genericFieldInfo[field].tabId;
			if (invalidTabs.indexOf(tabId) === -1) {
				invalidTabs[invalidTabs.length] = genericFieldInfo[field].tabId;
			}
		}
	}

	return {
		data,
		previousData,
		selectedTab,
		entitlementPoolsList,
		licenseKeyGroupsList,
		isFormValid,
		formReady,
		genericFieldInfo,
		invalidTabs,
		FGNames
	};
};


const mapActionsToProps = (dispatch, {licenseModelId, version}) => {
	return {
		onDataChanged: (deltaData, formName, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName, customValidations}),
		onTabSelect: tab => FeatureGroupsActionHelper.selectEntitlementPoolsEditorTab(dispatch, {tab}),
		onSubmit: (previousFeatureGroup, featureGroup) => {
			FeatureGroupsActionHelper.closeFeatureGroupsEditor(dispatch);
			FeatureGroupsActionHelper.saveFeatureGroup(dispatch, {licenseModelId, previousFeatureGroup, featureGroup, version});
		},
		onCancel: () => FeatureGroupsActionHelper.closeFeatureGroupsEditor(dispatch),
		onValidateForm: (formName) => ValidationHelper.validateForm(dispatch, formName)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(FeatureGroupEditorView);

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
import keyMirror from 'nfvo-utils/KeyMirror.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import InputOptions, {other as optionInputOther} from 'nfvo-components/input/inputOptions/InputOptions.jsx';

export const actionTypes = keyMirror({

	ENTITLEMENT_POOLS_LIST_LOADED: null,
	ADD_ENTITLEMENT_POOL: null,
	EDIT_ENTITLEMENT_POOL: null,
	DELETE_ENTITLEMENT_POOL: null,

	entitlementPoolsEditor: {
		OPEN: null,
		CLOSE: null,
		DATA_CHANGED: null,
	}

});

export const enums = keyMirror({
	SELECTED_FEATURE_GROUP_TAB: {
		GENERAL: 1,
		ENTITLEMENT_POOLS: 2,
		LICENCE_KEY_GROUPS: 3
	},
	SELECTED_ENTITLEMENT_POOLS_BUTTONTAB: {
		ASSOCIATED_ENTITLEMENT_POOLS: 1,
		AVAILABLE_ENTITLEMENT_POOLS: 2
	}
});

export const defaultState = {
	ENTITLEMENT_POOLS_EDITOR_DATA: {
		entitlementMetric: {choice: '', other: ''},
		aggregationFunction: {choice: '', other: ''},
		operationalScope: {choices: [], other: ''},
		time: {choice: '', other: ''}
	}
};

export const thresholdUnitType = {
	ABSOLUTE: 'Absolute',
	PERCENTAGE: 'Percentage'
};

export const optionsInputValues = {
	OPERATIONAL_SCOPE: [
		{enum: '', title: i18n('please select…')},
		{enum: 'Network_Wide', title: 'Network Wide'},
		{enum: 'Availability_Zone', title: 'Availability Zone'},
		{enum: 'Data_Center', title: 'Data Center'},
		{enum: 'Tenant', title: 'Tenant'},
		{enum: 'VM', title: 'VM'},
		{enum: 'CPU', title: 'CPU'},
		{enum: 'Core', title: 'Core'}
	],
	TIME: [
		{enum: '', title: i18n('please select…')},
		{enum: 'Hour', title: 'Hour'},
		{enum: 'Day', title: 'Day'},
		{enum: 'Month', title: 'Month'}
	],
	AGGREGATE_FUNCTION: [
		{enum: '', title: i18n('please select…')},
		{enum: 'Peak', title: 'Peak'},
		{enum: 'Average', title: 'Average'}
	],
	ENTITLEMENT_METRIC: [
		{enum: '', title: i18n('please select…')},
		{enum: 'Software_Instances_Count', title: 'Software Instances'},
		{enum: 'Core', title: 'Core'},
		{enum: 'CPU', title: 'CPU'},
		{enum: 'Trunks', title: 'Trunks'},
		{enum: 'User', title: 'User'},
		{enum: 'Subscribers', title: 'Subscribers'},
		{enum: 'Tenants', title: 'Tenants'},
		{enum: 'Tokens', title: 'Tokens'},
		{enum: 'Seats', title: 'Seats'},
		{enum: 'Units_TB', title: 'Units-TB'},
		{enum: 'Units_GB', title: 'Units-GB'},
		{enum: 'Units_MB', title: 'Units-MB'}
	],
	THRESHOLD_UNITS: [
		{enum: '', title: i18n('please select…')},
		{enum: thresholdUnitType.ABSOLUTE, title: 'Absolute'},
		{enum: thresholdUnitType.PERCENTAGE, title: '%'}
	]
};

export const extractValue = (item) => {
	if (item === undefined) {return '';} //TODO fix it later
	return  item ? item.choice === optionInputOther.OTHER ? item.other : InputOptions.getTitleByName(optionsInputValues, item.choice) : '';
};

export const extractUnits = (units) => {
	if (units === undefined) {return '';} //TODO fix it later
	return units === 'Absolute' ? '' : '%';
};

export const SP_ENTITLEMENT_POOL_FORM = 'SPENTITLEMENTPOOL';

export const EP_TIME_FORMAT = 'MM/DD/YYYY';

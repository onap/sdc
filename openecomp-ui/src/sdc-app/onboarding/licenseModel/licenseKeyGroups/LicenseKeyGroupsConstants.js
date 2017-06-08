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

	LICENSE_KEY_GROUPS_LIST_LOADED: null,
	DELETE_LICENSE_KEY_GROUP: null,
	EDIT_LICENSE_KEY_GROUP: null,
	ADD_LICENSE_KEY_GROUP: null,
	LICENSE_KEY_GROUPS_DELETE_CONFIRM: null,
	licenseKeyGroupsEditor: {
		OPEN: null,
		CLOSE: null,
		DATA_CHANGED: null,
	}
});

export const defaultState = {
	licenseKeyGroupsEditor: {
		type: '',
		operationalScope: {choices: [], other: ''}
	}
};

export const LKG_FORM_NAME = 'LKGFORM';

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
	TYPE: [
		{enum: '', title: i18n('please select…')},
		{enum: 'Universal', title: 'Universal'},
		{enum: 'Unique', title: 'Unique'},
		{enum: 'One_Time', title: 'One Time'}
	]
};

export const extractValue = (item) => {
	if (item === undefined) {return '';} //TODO fix it later

	return  item ? item === optionInputOther.OTHER ? item : InputOptions.getTitleByName(optionsInputValues, item) : '';
};

export const getOperationalScopes = (operationalScope) => {
	if(operationalScope.choices.toString() === i18n(optionInputOther.OTHER) && operationalScope.other !== '') {
		return operationalScope.other;
	}
	else {
		let allOpScopes = '';
		for (let opScope of operationalScope.choices) {
			allOpScopes += allOpScopes === '' ? InputOptions.getTitleByName(optionsInputValues, opScope) : `, ${InputOptions.getTitleByName(optionsInputValues, opScope)}`;
		}
		return allOpScopes;
	}
};

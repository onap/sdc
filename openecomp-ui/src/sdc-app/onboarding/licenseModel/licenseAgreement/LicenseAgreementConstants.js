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
	LICENSE_AGREEMENT_LIST_LOADED: null,
	ADD_LICENSE_AGREEMENT: null,
	EDIT_LICENSE_AGREEMENT: null,
	DELETE_LICENSE_AGREEMENT: null,

	licenseAgreementEditor: {
		OPEN: null,
		CLOSE: null,
		DATA_CHANGED: null,
		SELECT_TAB: null
	}

});

export const LA_EDITOR_FORM = 'LA_EDITOR_FORM';

export const enums = keyMirror({
	SELECTED_LICENSE_AGREEMENT_TAB: {
		GENERAL: 1,
		FEATURE_GROUPS: 2
	}
});

export const defaultState = {
	LICENSE_AGREEMENT_EDITOR_DATA: {
		licenseTerm: {choice: '', other: ''}
	}
};

export const optionsInputValues = {
	LICENSE_MODEL_TYPE: [
		{enum: '', title: i18n('please select…')},
		{enum: 'Fixed_Term', title: 'Fixed Term'},
		{enum: 'Perpetual', title: 'Perpetual'},
		{enum: 'Unlimited', title: 'Unlimited'}
	]
};

export const extractValue = (item) => {
	if (item === undefined) {
		return '';
	} //TODO fix it later

	return item ? item.choice === optionInputOther.OTHER ? item.other : InputOptions.getTitleByName(optionsInputValues, item.choice) : '';
};

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

export const actionTypes = keyMirror({
	LICENSE_MODEL_LOADED: null,
	LICENSE_MODELS_LIST_LOADED: null,
	FINALIZED_LICENSE_MODELS_LIST_LOADED: null,
	EDIT_LICENSE_MODEL: null
});


export const thresholdUnitType = {
	ABSOLUTE: 'Absolute',
	PERCENTAGE: 'Percentage'
};

export const optionsInputValues = {
	THRESHOLD_UNITS: [
		{enum: '', title: i18n('please select…')},
		{enum: thresholdUnitType.ABSOLUTE, title: 'Absolute'},
		{enum: thresholdUnitType.PERCENTAGE, title: '%'}
	]
};
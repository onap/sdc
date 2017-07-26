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
import i18n from 'nfvo-utils/i18n/i18n.js';
import {thresholdUnitType} from './LicenseModelConstants.js';
import Validator from 'nfvo-utils/Validator.js';

export function validateStartDate(value, state) {
	if (state.data.expiryDate) {
		if (!value) {
			return {isValid: false, errorText: i18n('Start date has to be specified if expiry date is specified')};
		}
	}
	return {isValid: true, errorText: ''};
}

export function thresholdValueValidation(value, state) {
	let  unit = state.data.thresholdUnits;
	if (unit === thresholdUnitType.PERCENTAGE) {
		return Validator.validate('thresholdValue', value, [
			{type: 'numeric', data: true},
			{type: 'maximum', data: 100},
			{type: 'minimum', data: 0}]);
	} else {
		return Validator.validate('thresholdValue', value, [
			{type: 'numeric', data: true},
		]);
	}
}

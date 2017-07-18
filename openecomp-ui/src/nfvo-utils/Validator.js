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

import * as ValidatorJS from 'validator';
import i18n from 'nfvo-utils/i18n/i18n.js';

class Validator {
	static get globalValidationFunctions() {
		return {
			required: value => value !== '',
			requiredChooseOption: value => value !== '',
			maxLength: (value, length) => ValidatorJS.isLength(value, {max: length}),
			minLength: (value, length) => ValidatorJS.isLength(value, {min: length}),
			pattern: (value, pattern) => ValidatorJS.matches(value, pattern),
			numeric: value => {
				if (value === '') {
					// to allow empty value which is not zero
					return true;
				}
				return ValidatorJS.isNumeric(value);
			},
			maximum: (value, maxValue) => {return (value === undefined) ? true : (value <= maxValue);},
			minimum: (value, minValue) => {return (value === undefined) ? true : (value >= minValue);},
			maximumExclusive: (value, maxValue) => {return (value === undefined) ? true : (value < maxValue);},
			minimumExclusive: (value, minValue) => {return (value === undefined) ? true : (value > minValue);},
			alphanumeric: value => ValidatorJS.isAlphanumeric(value),
			alphanumericWithSpaces: value => ValidatorJS.isAlphanumeric(value.replace(/ /g, '')),
			validateName: value => ValidatorJS.isAlphanumeric(value.replace(/\s|\.|\_|\-/g, ''), 'en-US'),
			validateVendorName: value => ValidatorJS.isAlphanumeric(value.replace(/[\x7F-\xFF]|\s/g, ''), 'en-US'),
			freeEnglishText: value => ValidatorJS.isAlphanumeric(value.replace(/\s|\.|\_|\-|\,|\(|\)|\?/g, ''), 'en-US'),
			email: value => ValidatorJS.isEmail(value),
			ip: value => ValidatorJS.isIP(value),
			url: value => ValidatorJS.isURL(value),
			alphanumericWithUnderscores: value => ValidatorJS.isAlphanumeric(value.replace(/_/g, ''))
		};
	}

	static get globalValidationMessagingFunctions() {
		return {
			required: () => i18n('Field is required'),
			requiredChooseOption: () => i18n('Field should have one of these options'),
			maxLength: (value, maxLength) => i18n('Field value has exceeded it\'s limit, {maxLength}. current length: {length}', {
				length: value.length,
				maxLength
			}),
			minLength: (value, minLength) => i18n(`Field value should contain at least ${minLength} characters.`),
			pattern: (value, pattern) => i18n(`Field value should match the pattern: ${pattern}.`),
			numeric: () => i18n('Field value should contain numbers only.'),
			maximum: (value, maxValue) => i18n(`Field value should be less or equal to: ${maxValue}.`),
			minimum: (value, minValue) => i18n(`Field value should be at least: ${minValue.toString()}.`),
			maximumExclusive: (value, maxValue) => i18n(`Field value should be less than: ${maxValue}.`),
			minimumExclusive: (value, minValue) => i18n(`Field value should be more than: ${minValue.toString()}.`),
			alphanumeric: () => i18n('Field value should contain letters or digits only.'),
			alphanumericWithSpaces: () => i18n('Field value should contain letters, digits or spaces only.'),
			validateName: ()=> i18n('Field value should contain English letters, digits , spaces, underscores, dashes and dots only.'),
			validateVendorName: ()=> i18n('Field value should contain English letters digits and spaces only.'),
			freeEnglishText: ()=> i18n('Field value should contain  English letters, digits , spaces, underscores, dashes and dots only.'),
			email: () => i18n('Field value should be a valid email address.'),
			ip: () => i18n('Field value should be a valid ip address.'),
			url: () => i18n('Field value should be a valid url address.'),
			general: () => i18n('Field value is invalid.'),
			alphanumericWithUnderscores: () => i18n('Field value should contain letters, digits or _ only.')
		};
	}

	static validateItem(value, data, type) {
		let validationFunc = this.globalValidationFunctions[type];
		const isValid = validationFunc(value, data);
		let errorText = '';
		if (!isValid) {
			errorText = this.globalValidationMessagingFunctions[type](value, data);
		}
		return {
			isValid,
			errorText
		};
	}

	static validate(fieldName, value, validations, state, customValidations) {
		let result = { isValid: true, errorText: '' };
		for (let validation of validations) {
			result = this.validateItem(value, validation.data, validation.type);
			if (!result.isValid) {
				return result;
			}
		}
		if (customValidations) {
			let validationFunc = customValidations[fieldName];
			if (validationFunc) {
				result = validationFunc(value, state);
			}
		}
		return result;
	}

	static isItemNameAlreadyExistsInList({itemId, itemName, list}) {
		return list[itemName] && list[itemName] !== itemId;
	}
}

export default Validator;

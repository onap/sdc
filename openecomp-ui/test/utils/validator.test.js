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
import Validator from 'src/nfvo-utils/Validator.js';


describe('Validator ', () => {

	it('class does exist', () => {
		expect(Validator).toBeTruthy();
	});

	it('returns global validation functions', () => {
		expect(Validator.globalValidationFunctions).toBeTruthy();
	});

	it('returns global validation messages', () => {
		expect(Validator.globalValidationMessagingFunctions).toBeTruthy();
	});

	it('validates data per specific types list', () => {
		const types = ['required', 'maxLength', 'minLength', 'pattern', 'numeric', 'maximum', 'minimum',
			'alphanumeric', 'alphanumericWithSpaces', 'validateName', 'validateVendorName', 'freeEnglishText', 'email', 'ip', 'url', 'maximumExclusive', 'minimumExclusive'];

		for (let i = 0; i < types.length; i++) {
			expect(Validator.globalValidationFunctions[types[i]]).toBeTruthy();
		}
	});

	it('gives validation messages per specific types list', () => {
		const types = ['required', 'maxLength', 'minLength', 'pattern', 'numeric', 'maximum', 'minimum',
			'alphanumeric', 'alphanumericWithSpaces', 'validateName', 'validateVendorName', 'freeEnglishText', 'email', 'ip', 'url',  'maximumExclusive', 'minimumExclusive'];

		for (let i = 0; i < types.length; i++) {
			expect(Validator.globalValidationFunctions[types[i]]).toBeTruthy();
		}
	});

	it('returns a validation response of {isValid, errorText} when validating only by validator.js', () => {
		const result = Validator.validateItem('a', null, 'required');
		const keys = Object.keys(result);
		expect(keys.length).toBe(2);
		expect(keys).toContain('isValid');
		expect(keys).toContain('errorText');
	});

	it('returns a validation response of {isValid, errorText} when validating with custom functions', () => {
		const errorText = 'ran custom validation';
		const result = Validator.validate('myfield','a', [{type: 'required', data: null}],  {}, { 'myfield' : () => { return { isValid: false, errorText};} });
		const keys = Object.keys(result);
		expect(keys.length).toBe(2);
		expect(keys).toContain('isValid');
		expect(keys).toContain('errorText');
		expect(result.errorText).toBe(errorText);
	});

});

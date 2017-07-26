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
import {actionTypes, defaultState, LKG_FORM_NAME} from './LicenseKeyGroupsConstants.js';
import moment from 'moment';
import {DATE_FORMAT} from 'sdc-app/onboarding/OnboardingConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.licenseKeyGroupsEditor.OPEN:
			let licenseKeyGroupData = {...action.licenseKeyGroup};
			let {startDate, expiryDate} = licenseKeyGroupData;
			if (startDate) {
				licenseKeyGroupData.startDate = moment(startDate, DATE_FORMAT).format(DATE_FORMAT);
			}
			if (expiryDate) {
				licenseKeyGroupData.expiryDate = moment(expiryDate, DATE_FORMAT).format(DATE_FORMAT);
			}
			return {
				...state,
				data: action.licenseKeyGroup ? licenseKeyGroupData : defaultState.licenseKeyGroupsEditor,
				formReady: null,
				formName: LKG_FORM_NAME,
				genericFieldInfo: {
					'description' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'maxLength', data: 1000}]
					},
					'name' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'maxLength', data: 120}]
					},
					'type' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					},
					'operationalScope' : {
						isValid: true,
						errorText: '',
						validations: []
					},
					'thresholdUnits' : {
						isValid: true,
						errorText: '',
						validations: []
					},
					'thresholdValue' : {
						isValid: true,
						errorText: '',
						validations: []
					},
					'increments' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'maxLength', data: 120}]
					},
					'startDate': {
						isValid: true,
						errorText: '',
						validations: []
					},
					'expiryDate': {
						isValid: true,
						errorText: '',
						validations: []
					}
				}
			};
		case actionTypes.licenseKeyGroupsEditor.LIMITS_LIST_LOADED:
			return {
				...state,
				limitsList: action.response.results
			};	
		case actionTypes.licenseKeyGroupsEditor.CLOSE:
			return {};
		default:
			return state;
	}
};

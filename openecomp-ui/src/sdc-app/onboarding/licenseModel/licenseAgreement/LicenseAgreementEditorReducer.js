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
import {actionTypes, defaultState, LA_EDITOR_FORM, enums as LicenseAgreementEnums} from './LicenseAgreementConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.licenseAgreementEditor.OPEN:
			return {
				...state,
				formReady: null,
				formName: LA_EDITOR_FORM,
				genericFieldInfo: {
					'description' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'maxLength', data: 1000}],
						tabId: LicenseAgreementEnums.SELECTED_LICENSE_AGREEMENT_TAB.GENERAL
					},
					'requirementsAndConstrains' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'maxLength', data: 1000}],
						tabId: LicenseAgreementEnums.SELECTED_LICENSE_AGREEMENT_TAB.GENERAL
					},
					'licenseTerm' : {
						isValid: true,
						errorText: '',
						validations: [],
						tabId: LicenseAgreementEnums.SELECTED_LICENSE_AGREEMENT_TAB.GENERAL
					},
					'name' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'maxLength', data: 25}],
						tabId: LicenseAgreementEnums.SELECTED_LICENSE_AGREEMENT_TAB.GENERAL
					}
				},
				data: action.licenseAgreement ? { ...action.licenseAgreement } : defaultState.LICENSE_AGREEMENT_EDITOR_DATA
			};
		case actionTypes.licenseAgreementEditor.CLOSE:
			return {};
		case actionTypes.licenseAgreementEditor.SELECT_TAB:
			return {
				...state,
				selectedTab: action.tab
			};
		default:
			return state;
	}

};

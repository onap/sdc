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
import {actionTypes as licenseAgreementActionTypes} from './LicenseAgreementConstants';

export default (state = [], action) => {
	switch (action.type) {
		case licenseAgreementActionTypes.LICENSE_AGREEMENT_LIST_LOADED:
			return [...action.response.results];
		case licenseAgreementActionTypes.ADD_LICENSE_AGREEMENT:
			return [...state, action.licenseAgreement];
		case licenseAgreementActionTypes.EDIT_LICENSE_AGREEMENT:
			const indexForEdit = state.findIndex(licenseAgreement => licenseAgreement.id === action.licenseAgreement.id);
			return [...state.slice(0, indexForEdit), action.licenseAgreement, ...state.slice(indexForEdit + 1)];
		case licenseAgreementActionTypes.DELETE_LICENSE_AGREEMENT:
			return state.filter(licenseAgreement => licenseAgreement.id !== action.licenseAgreementId);
		default:
			return state;
	}
};

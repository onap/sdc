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
import {actionTypes, defaultState, SP_ENTITLEMENT_POOL_FORM} from './EntitlementPoolsConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.entitlementPoolsEditor.OPEN:
			return {
				...state,
				formReady: null,
				formName: SP_ENTITLEMENT_POOL_FORM,
				genericFieldInfo: {
					'name' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'maxLength', data: 120}]
					},
					'description' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'maxLength', data: 1000}]
					},
					'manufacturerReferenceNumber' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'maxLength', data: 100}]
					},
					'increments' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'maxLength', data: 120}]
					},
					'operationalScope' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					},
					'thresholdUnits' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					},
					'thresholdValue' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					},
					'entitlementMetric' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					},
					'aggregationFunction' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					},
					'time' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					}
				},
				data: action.entitlementPool ? {...action.entitlementPool} : defaultState.ENTITLEMENT_POOLS_EDITOR_DATA
			};
		case actionTypes.entitlementPoolsEditor.DATA_CHANGED:
			return {
				...state,
				data: {
					...state.data,
					...action.deltaData
				}
			};
		case actionTypes.entitlementPoolsEditor.CLOSE:
			return {};
		default:
			return state;
	}

};

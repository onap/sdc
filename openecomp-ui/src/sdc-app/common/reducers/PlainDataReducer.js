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
import {actionTypes} from './PlainDataReducerConstants.js';
import Validator from 'nfvo-utils/Validator.js';
import forOwn from 'lodash/forOwn.js';
import {other as optionInputOther} from 'nfvo-components/input/inputOptions/InputOptions.jsx';

function updateDataAndValidateReducer(state = {}, action) {
	let genericFieldInfoCopy;
	switch (action.type) {
		case actionTypes.DATA_CHANGED:
			let changed = action.deltaData;
			if (!action.formName || (state.formName !== action.formName)) {return {...state};}
			genericFieldInfoCopy = {...state.genericFieldInfo};
			forOwn(changed,(value, key) => {
				if (state.genericFieldInfo[key]) {
					let result = Validator.validate(key, value, state.genericFieldInfo[key].validations, state, action.customValidations);
					genericFieldInfoCopy[key] = {...genericFieldInfoCopy[key], isValid: result.isValid, errorText: result.errorText};
				}
			});
			return {
				...state,
				formReady: null,
				data: {
					...state.data,
					...action.deltaData
				},
				genericFieldInfo: genericFieldInfoCopy
			};
		case actionTypes.VALIDATE_FORM:
			if (!action.formName || (state.formName !== action.formName)) {return {...state};}
			genericFieldInfoCopy = {...state.genericFieldInfo};
			let formReady = true;
			forOwn(state.genericFieldInfo,(value, key) => {
				let val = state.data && state.data[key] ? state.data[key] : '';
				let result = Validator.validate(key, val, state.genericFieldInfo[key].validations, state, {});
				if(val.choice !== undefined) {
					result = Validator.validate(key, val.choice, state.genericFieldInfo[key].validations, state, {});
				}
				if(val.choice !== undefined && val.choice === optionInputOther.OTHER) {
					result = Validator.validate(key, val.other, state.genericFieldInfo[key].validations, state, {});
				}
				genericFieldInfoCopy[key] = {...genericFieldInfoCopy[key], isValid: result.isValid, errorText: result.errorText};
				if (!result.isValid) {
					formReady = false;
				}
			});
			return {
				...state,
				formReady,
				genericFieldInfo: genericFieldInfoCopy
			};
		case actionTypes.VALIDATE_DATA:
			let specificFields = action.data;
			if (!action.formName || (state.formName !== action.formName)) {return {...state};}
			genericFieldInfoCopy = {...state.genericFieldInfo};
			forOwn(specificFields,(value, key) => {
				let result = Validator.validate(key, value, state.genericFieldInfo[key].validations, state, action.customValidations);
				genericFieldInfoCopy[key] = {...genericFieldInfoCopy[key], isValid: result.isValid, errorText: result.errorText};
			});
			return {
				...state,
				formReady: null,
				genericFieldInfo: genericFieldInfoCopy
			};
		default:
			return state;
	}
};

export function createPlainDataReducer(loadReducer) {
	return (state = {}, action) => {
		if(action.type === actionTypes.VALIDATE_DATA ||
			action.type === actionTypes.VALIDATE_FORM ||
			action.type === actionTypes.DATA_CHANGED
		) {
			return updateDataAndValidateReducer(state, action);
		} else {
			return loadReducer(state, action);
		}
	};
};







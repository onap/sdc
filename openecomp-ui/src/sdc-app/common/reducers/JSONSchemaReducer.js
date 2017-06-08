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
import {actionTypes} from './JSONSchemaReducerConstants.js';
import Validator from 'nfvo-utils/Validator.js';
import JSONSchema from 'nfvo-utils/json/JSONSchema.js';
import JSONPointer from 'nfvo-utils/json/JSONPointer.js';
import forOwn from 'lodash/forOwn.js';
import isArray from 'lodash/isArray.js';


function flattenData(data, result, pointer = '') {
	let newPointer = pointer;
	if (typeof data === 'object' && !isArray(data)) {
		for (let i in data) {
			newPointer = newPointer ? newPointer + '/' + i : i;
			flattenData(data[i], result, newPointer);
			newPointer = pointer;
		}
	} else {
		result[newPointer] = data;
	}
}

function updateSchemaDataAndValidateReducer (state = {}, action, questionnaireName) {
	let genericFieldInfoClone;
	switch (action.type) {
		case actionTypes.DATA_LOADED:
			if (questionnaireName !== action.qName) {return {...state};}
			const schema = action.payload.qschema;
			let schemaLoader = new JSONSchema();
			schemaLoader.setSchema(schema);
			schemaLoader.setSupportedValidationFunctions(Object.keys(Validator.globalValidationFunctions));
			let {genericFieldInfo} = schemaLoader.flattenSchema();

			let data = action.payload.qdata;
			let dataMap =  {};
			flattenData(data, dataMap);

			return {
				...state,
				qdata: action.payload.qdata, // the original hierarchical data. to be used for submit and save
				qgenericFieldInfo : genericFieldInfo, // information about the fields that the view will require and reducer will need, such as validations, enum to use, etc.
				dataMap //  flattened schema data for ease of use
			};

		case actionTypes.DATA_CHANGED:
			let changedData = action.deltaData;
			if (questionnaireName !== action.qName)  {return {...state};}

			genericFieldInfoClone = {...state.qgenericFieldInfo};
			let qDataClone = {...state.qdata};
			let dataMapClone = {...state.dataMap};

			forOwn(changedData,(value, key) => {
				if (state.qgenericFieldInfo[key]) {
					let result = Validator.validate(key, value, state.qgenericFieldInfo[key].validations, state, action.customValidations);
					genericFieldInfoClone[key] = {...genericFieldInfoClone[key], isValid: result.isValid, errorText: result.errorText};
					qDataClone = JSONPointer.setValue(state.qdata, '/' + key, value);
					dataMapClone[key] = value;
				}
			});

			return {
				...state,
				qdata: qDataClone,
				dataMap: dataMapClone,
				qgenericFieldInfo: genericFieldInfoClone
			};

		case actionTypes.VALIDATE_DATA:
			let specificFields = action.data;
			if (questionnaireName !== action.qName)  {return {...state};}
			genericFieldInfoClone = {...state.qgenericFieldInfo};
			forOwn(specificFields,(value, key) => {
				let result = Validator.validate(key, value, state.qgenericFieldInfo[key].validations, state, action.customValidations);
				genericFieldInfoClone[key] = {...genericFieldInfoClone[key], isValid: result.isValid, errorText: result.errorText};
			});
			return {
				...state,
				formReady: null,
				qgenericFieldInfo: genericFieldInfoClone
			};

		case actionTypes.VALIDATE_FORM:
			if (questionnaireName !== action.qName)  {return {...state};}
			genericFieldInfoClone = {...state.qgenericFieldInfo};
			let formReady = true;
			forOwn(state.qgenericFieldInfo,(value, key) => {
				let val = state.data[key] ? state.data[key] : '';
				let result = Validator.validate(key, val, state.qgenericFieldInfo[key].validations, state, {});
				genericFieldInfoClone[key] = {...genericFieldInfoClone[key], isValid: result.isValid, errorText: result.errorText};
				if (!result.isValid) {
					formReady = false;
				}
			});
			return {
				...state,
				formReady,
				qgenericFieldInfo: genericFieldInfoClone
			};

		default:
			return state;
	}
};

export function createJSONSchemaReducer(questionnaireName) {
	return (state = {}, action) => {
		return updateSchemaDataAndValidateReducer(state, action, questionnaireName);
	};
};

export function createComposedJSONSchemaReducer(questionnaireName, additionalActionsReducer) {
	return (state = {}, action) => {
		if(action.type === actionTypes.VALIDATE_DATA ||
			action.type === actionTypes.VALIDATE_FORM ||
			action.type === actionTypes.DATA_CHANGED ||
			action.type === actionTypes.DATA_LOADED
		) {
			return updateSchemaDataAndValidateReducer(state, action, questionnaireName);
		} else {
			return additionalActionsReducer(state, action);
		}
	};
};








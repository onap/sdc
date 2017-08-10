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
import {actionTypes as commonActionTypes} from 'sdc-app/common/reducers/PlainDataReducerConstants.js';
import {actionTypes as qcommonActionTypes} from 'sdc-app/common/reducers/JSONSchemaReducerConstants.js';

class ValidationHelper {

	static dataChanged(dispatch, {deltaData, formName, customValidations = {}}){
		dispatch({
			type: commonActionTypes.DATA_CHANGED,
			deltaData,
			formName,
			customValidations
		});
	}

	static validateForm(dispatch, formName){
		dispatch({
			type: commonActionTypes.VALIDATE_FORM,
			formName
		});
	}

	static validateData(dispatch, {formName, data}) {
		dispatch({
			type: commonActionTypes.VALIDATE_DATA,
			formName,
			data
		});
	}

	static qValidateData(dispatch, {data, qName, customValidations = {}}) {
		dispatch({
			type: qcommonActionTypes.VALIDATE_DATA,
			data,
			qName,
			customValidations
		});
	}

	static qValidateForm(dispatch, qName, customValidations){
		dispatch({
			type: qcommonActionTypes.VALIDATE_FORM,
			qName,
			customValidations
		});
	}

	static qDataChanged(dispatch, {deltaData, qName, customValidations = {}}){
		dispatch({
			type: qcommonActionTypes.DATA_CHANGED,
			deltaData,
			qName,
			customValidations
		});
	}

	static qDataLoaded(dispatch, {qName, response: {qdata, qschema}}) {
		dispatch({
			type: qcommonActionTypes.DATA_LOADED,
			payload: {
				qdata,
				qschema
			},
			qName
		});
	}

	static checkFormValid(genericFieldInfo) {
		for (let field in genericFieldInfo) {
			if (!genericFieldInfo[field].isValid) {
				return false;
			}
		}
		return true;
	}
}

export default ValidationHelper;

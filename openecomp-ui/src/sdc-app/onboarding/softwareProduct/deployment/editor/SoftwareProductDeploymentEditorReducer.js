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
import {actionTypes, DEPLOYMENT_FLAVORS_FORM_NAME} from '../SoftwareProductDeploymentConstants.js';;

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.deploymentFlavorEditor.SOFTWARE_PRODUCT_DEPLOYMENT_FILL_DATA:
			return {
				...state,
				data: action.deploymentFlavor,
				formReady: null,
				formName: DEPLOYMENT_FLAVORS_FORM_NAME,
				genericFieldInfo: {
					'description' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'maxLength', data: 500}]
					},
					'model' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					}
				}
			};
		case actionTypes.deploymentFlavorEditor.SOFTWARE_PRODUCT_DEPLOYMENT_CLEAR_DATA:
			return {};
		default:
			return state;
	}
};

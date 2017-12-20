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
import {actionTypes, VERSION_CREATION_FORM_NAME, defaultState} from './VersionsPageCreationConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.OPEN:
			return {
				...state,
				formReady: null,
				formName: VERSION_CREATION_FORM_NAME,
				data: {...defaultState},
				genericFieldInfo: {
					description: {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'maxLength', data: 120}]
					},
					creationMethod: {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					}
				}
			};
		case actionTypes.CLOSE:
			return {};
		default:
			return state;
	}
};

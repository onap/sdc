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
import keyMirror from 'nfvo-utils/KeyMirror.js';

export const actionTypes = keyMirror({
	ADD_SOFTWARE_PRODUCT_COMPONENTS_PROCESS: null,
	EDIT_SOFTWARE_PRODUCT_COMPONENTS_PROCESS: null,
	DELETE_SOFTWARE_PRODUCT_COMPONENTS_PROCESS: null,
	SOFTWARE_PRODUCT_PROCESS_COMPONENTS_EDITOR_OPEN: null,
	SOFTWARE_PRODUCT_PROCESS_COMPONENTS_EDITOR_CLOSE: null,
	FETCH_SOFTWARE_PRODUCT_COMPONENTS_PROCESSES: null,
	SOFTWARE_PRODUCT_PROCESS_DELETE_COMPONENTS_CONFIRM: null
});

export const optionsInputValues = {
	PROCESS_TYPE: [
		{title: 'Select...', enum: ''},
		{title: 'Lifecycle Operations', enum: 'Lifecycle_Operations'},
		{title: 'Other', enum: 'Other'}
	]
};

export const SOFTWARE_PRODUCT_PROCESS_COMPONENTS_EDITOR_FORM = 'SOFTWAREPRODUCTPROCESSCOMPONENTSEDITORFORM';

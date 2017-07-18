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
	COMPONENTS_LIST_UPDATE: null,
	COMPONENTS_LIST_EDIT: null,
	COMPONENT_UPDATE: null,
	COMPONENT_DATA_CHANGED: null,
	COMPONENT_DATA_CLEAR: null,
	COMPONENT_QUESTIONNAIRE_UPDATE: null,
	COMPONENT_DELETE: null,
	COMPONENT_LOAD: null,
	COMPONENT_CREATE_OPEN: null
});

export const storageConstants  = keyMirror({
	backupType: {
		ON_SITE: 'OnSite',
		OFF_SITE: 'OffSite'
	}
});

export const forms = keyMirror({
	ALL_SPC_FORMS: null,
	NIC_EDIT_FORM: null,
	CREATE_FORM: null,
	IMAGE_EDIT_FORM: null
});

export const COMPONENTS_QUESTIONNAIRE = 'component';
export const COMPONENTS_COMPUTE_QUESTIONNAIRE = 'compute';

export const navigationItems = keyMirror({
	STORAGE: 'Storage',
	PROCESS_DETAILS: 'Process Details',
	MONITORING: 'Monitoring',
	NETWORK: 'Network',
	IMAGES: 'Images',
	COMPUTE: 'Compute',
	LOAD_BALANCING: 'High Availability & Load Balancing'
});

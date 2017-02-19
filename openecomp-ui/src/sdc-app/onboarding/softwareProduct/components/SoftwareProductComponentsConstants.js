/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import keyMirror from 'nfvo-utils/KeyMirror.js';

export const actionTypes = keyMirror({
	COMPONENTS_LIST_UPDATE: null,
	COMPONENTS_LIST_EDIT: null,
	COMPONENT_UPDATE: null,
	COMPONENT_DATA_CHANGED: null,
	COMPONENT_QUESTIONNAIRE_UPDATE: null
});

export const storageConstants  = keyMirror({
	backupType: {
		ON_SITE: 'OnSite',
		OFF_SITE: 'OffSite'
	}
});

export const navigationItems = keyMirror({
	STORAGE: 'Storage',
	PROCESS_DETAILS: 'Process Details',
	MONITORING: 'Monitoring',
	NETWORK: 'Network',
	COMPUTE: 'Compute',
	NETWORK: 'Network',
	LOAD_BALANCING: 'High Availability & Load Balancing'
});

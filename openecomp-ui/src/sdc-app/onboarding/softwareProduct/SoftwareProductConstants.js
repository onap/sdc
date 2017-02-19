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
	SOFTWARE_PRODUCT_LOADED: null,
	SOFTWARE_PRODUCT_LIST_LOADED: null,
	SOFTWARE_PRODUCT_LIST_EDIT: null,
	SOFTWARE_PRODUCT_CATEGORIES_LOADED: null,
	SOFTWARE_PRODUCT_QUESTIONNAIRE_UPDATE: null,
	ADD_SOFTWARE_PRODUCT: null,
	TOGGLE_NAVIGATION_ITEM: null,

	softwareProductEditor: {
		OPEN: null,
		CLOSE: null,
		DATA_CHANGED: null,
		IS_VALIDITY_DATA_CHANGED: null,
		UPLOAD_CONFIRMATION: null
	}
});

export const navigationItems = keyMirror({
	VENDOR_SOFTWARE_PRODUCT: 'Vendor Software Product',
	GENERAL: 'General',
	PROCESS_DETAILS: 'Process Details',
	NETWORKS: 'Networks',
	ATTACHMENTS: 'Attachments',
	COMPONENTS: 'Components',

	COMPUTE: 'Compute',
	LOAD_BALANCING: 'Load Balancing',
	STORAGE: 'Storage',
	MONITORING: 'Monitoring'
});

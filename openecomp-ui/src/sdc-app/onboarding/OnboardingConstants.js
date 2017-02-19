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
	SET_CURRENT_SCREEN: null,
	SET_CURRENT_LICENSE_MODEL: null
});

export const enums = keyMirror({

	BREADCRUMS: {
		LICENSE_MODEL: 'LICENSE_MODEL',
		LICENSE_AGREEMENTS: 'LICENSE_AGREEMENTS',
		FEATURE_GROUPS: 'FEATURE_GROUPS',
		ENTITLEMENT_POOLS: 'ENTITLEMENT_POOLS',
		LICENSE_KEY_GROUPS: 'LICENSE_KEY_GROUPS',

		SOFTWARE_PRODUCT: 'SOFTWARE_PRODUCT',
		SOFTWARE_PRODUCT_DETAILS: 'SOFTWARE_PRODUCT_DETAILS',
		SOFTWARE_PRODUCT_ATTACHMENTS: 'SOFTWARE_PRODUCT_ATTACHMENTS',
		SOFTWARE_PRODUCT_PROCESSES: 'SOFTWARE_PRODUCT_PROCESSES',
		SOFTWARE_PRODUCT_NETWORKS: 'SOFTWARE_PRODUCT_NETWORKS',
		SOFTWARE_PRODUCT_COMPONENTS: 'SOFTWARE_PRODUCT_COMPONENTS',
		SOFTWARE_PRODUCT_COMPONENT_PROCESSES: 'SOFTWARE_PRODUCT_COMPONENT_PROCESSES',
		SOFTWARE_PRODUCT_COMPONENT_STORAGE: 'SOFTWARE_PRODUCT_COMPONENT_STORAGE',
		SOFTWARE_PRODUCT_COMPONENT_GENERAL: 'SOFTWARE_PRODUCT_COMPONENT_GENERAL',
		SOFTWARE_PRODUCT_COMPONENT_COMPUTE: 'SOFTWARE_PRODUCT_COMPONENT_COMPUTE',
		SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING: 'SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING',
		SOFTWARE_PRODUCT_COMPONENT_MONITORING: 'SOFTWARE_PRODUCT_COMPONENT_MONITORING'
	},

	SCREEN: {
		ONBOARDING_CATALOG: null,
		LICENSE_AGREEMENTS: null,
		FEATURE_GROUPS: null,
		ENTITLEMENT_POOLS: null,
		LICENSE_KEY_GROUPS: null,

		SOFTWARE_PRODUCT_LANDING_PAGE: null,
		SOFTWARE_PRODUCT_DETAILS: null,
		SOFTWARE_PRODUCT_ATTACHMENTS: null,
		SOFTWARE_PRODUCT_PROCESSES: null,
		SOFTWARE_PRODUCT_NETWORKS: null,
		SOFTWARE_PRODUCT_COMPONENTS: null,
		SOFTWARE_PRODUCT_COMPONENT_PROCESSES: null,
		SOFTWARE_PRODUCT_COMPONENT_COMPUTE: null,
		SOFTWARE_PRODUCT_COMPONENT_STORAGE: null,
		SOFTWARE_PRODUCT_COMPONENT_NETWORK: null,
		SOFTWARE_PRODUCT_COMPONENT_GENERAL: null,
		SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING: null,
		SOFTWARE_PRODUCT_COMPONENT_MONITORING: null
	}
});

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
import {enums} from 'sdc-app/onboarding/OnboardingConstants.js';

export const actionTypes = keyMirror({
	SOFTWARE_PRODUCT_LOADED: null,
	SOFTWARE_PRODUCT_LIST_LOADED: null,
	FINALIZED_SOFTWARE_PRODUCT_LIST_LOADED: null,
	SOFTWARE_PRODUCT_LIST_EDIT: null,
	SOFTWARE_PRODUCT_CATEGORIES_LOADED: null,
	SOFTWARE_PRODUCT_QUESTIONNAIRE_UPDATE: null,
	ADD_SOFTWARE_PRODUCT: null,
	TOGGLE_NAVIGATION_ITEM: null,

	softwareProductEditor: {
		OPEN: null,
		CLOSE: null,
		DATA_CHANGED: null,
		IS_VALIDITY_DATA_CHANGED: null
	}
});

export const navigationItems = keyMirror({
	VENDOR_SOFTWARE_PRODUCT: 'vendor-software-product',
	GENERAL: 'general',
	PROCESS_DETAILS: 'process-details',
	DEPLOYMENT_FLAVORS: 'deployment-flavor',
	NETWORKS: 'networks',	
	IMAGES: 'images',
	ATTACHMENTS: 'attachments',
	ACTIVITY_LOG: 'activity-log',
	COMPONENTS: 'components',
	DEPENDENCIES: 'dependencies',

	COMPUTE: 'compute',
	LOAD_BALANCING: 'load-balancing',
	STORAGE: 'storage',
	MONITORING: 'monitoring'
});

export const onboardingMethod = {
	MANUAL: 'Manual',
	HEAT: 'HEAT'
};

export const forms = keyMirror({
	VENDOR_SOFTWARE_PRODUCT_DETAILS: 'vendor-software-product-details',
});

export const PRODUCT_QUESTIONNAIRE = 'product';

export const mapScreenToNavigationItem = {
	[enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE]: navigationItems.VENDOR_SOFTWARE_PRODUCT,
	[enums.SCREEN.SOFTWARE_PRODUCT_DETAILS]: navigationItems.GENERAL,
	[enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS]: navigationItems.ATTACHMENTS,
	[enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES]: navigationItems.PROCESS_DETAILS,
	[enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT]: navigationItems.DEPLOYMENT_FLAVORS,
	[enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS]: navigationItems.NETWORKS,
	[enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG]: navigationItems.ACTIVITY_LOG,
	[enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES]: navigationItems.DEPENDENCIES,
	[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS]: navigationItems.COMPONENTS,
	[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL]: navigationItems.GENERAL,
	[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE]: navigationItems.COMPUTE,
	[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING]: navigationItems.LOAD_BALANCING,
	[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK]: navigationItems.NETWORKS,
	[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES]: navigationItems.IMAGES,
	[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE]: navigationItems.STORAGE,
	[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES]: navigationItems.PROCESS_DETAILS,
	[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING]: navigationItems.MONITORING,
};

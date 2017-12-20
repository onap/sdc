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

export const catalogItemTypes = Object.freeze({
	LICENSE_MODEL: 'license-model',
	SOFTWARE_PRODUCT: 'software-product'
});

export const catalogItemTypeClasses = {
	LICENSE_MODEL: 'license-model-type',
	SOFTWARE_PRODUCT: 'software-product-type',
	VENDOR: 'vendor-type'
};

export const catalogItemStatuses = {
	DRAFT: 'Draft',
	CERTIFIED: 'Certified'
};

export const modalMapper = {
	'license-model': 'LICENSE_MODEL',
	'software-product': 'SOFTWARE_PRODUCT'
};

export const tabsMapping = {
	'BY_VENDOR': 1,
	'ALL': 2
};

export const migrationStatusMapper = {
	OLD_VERSION: 'True',
};

export const actionTypes = keyMirror({
	ONBOARDING_CATALOG_OPEN_VENDOR_PAGE: null,
	CHANGE_ACTIVE_CATALOG_TAB: null,
	CHANGE_SEARCH_VALUE: null,
	CHANGE_VSP_OVERLAY: null,
	CLOSE_VSP_OVERLAY: null
});

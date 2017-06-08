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
	LICENSE_MODEL_LOADED: null,
	LICENSE_MODELS_LIST_LOADED: null,
	FINALIZED_LICENSE_MODELS_LIST_LOADED: null,
	ADD_LICENSE_MODEL: null,
	EDIT_LICENSE_MODEL: null
});

export const navigationItems = keyMirror({
	LICENSE_MODEL_OVERVIEW: 'overview',
	LICENSE_AGREEMENTS: 'license-agreements',
	FEATURE_GROUPS: 'feature-groups',
	ENTITLEMENT_POOLS: 'entitlement-pools',
	LICENSE_KEY_GROUPS: 'license-key-groups',
	ACTIVITY_LOG: 'activity-log'
});

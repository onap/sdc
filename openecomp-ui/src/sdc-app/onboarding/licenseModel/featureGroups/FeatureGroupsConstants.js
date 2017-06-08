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
	FEATURE_GROUPS_LIST_LOADED: null,
	ADD_FEATURE_GROUPS: null,
	EDIT_FEATURE_GROUPS: null,
	DELETE_FEATURE_GROUPS: null,

	ENTITLEMENT_POOLS_LIST_LOADED: null,

	featureGroupsEditor: {
		OPEN: null,
		CLOSE: null,
		DATA_CHANGED: null,
		SELECT_TAB: null
	}
});

export const FG_EDITOR_FORM = 'FG_EDITOR_FORM';

export const state = keyMirror({
	SELECTED_FEATURE_GROUP_TAB: {
		GENERAL: 1,
		ENTITLEMENT_POOLS: 2,
		LICENSE_KEY_GROUPS: 3
	}
});

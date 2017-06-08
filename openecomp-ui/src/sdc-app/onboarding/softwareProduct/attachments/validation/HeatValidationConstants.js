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
import i18n from 'nfvo-utils/i18n/i18n.js';

export const actionTypes = keyMirror({
	TOGGLE_EXPANDED: null,
	SELECTED_NODE: null,
	UNSELECTED_NODE: null
});

export const errorTypes = keyMirror({
	MISSING_FILE_IN_ZIP: i18n('missing file in zip'),
	MISSING_FILE_IN_MANIFEST: i18n('missing file in manifest'),
	MISSING_OR_ILLEGAL_FILE_TYPE_IN_MANIFEST: i18n('missing or illegal file type in manifest'),
	FILE_IS_YML_WITHOUT_YML_EXTENSION: i18n('file is defined as a heat file but it doesn\'t have .yml or .yaml extension'),
	FILE_IS_ENV_WITHOUT_ENV_EXTENSION: i18n('file is defined as an env file but it doesn\'t have .env extension'),
	ILLEGAL_YAML_FILE_CONTENT: i18n('illegal yaml file content'),
	ILLEGAL_HEAT_YAML_FILE_CONTENT: i18n('illegal HEAT yaml file content'),
	MISSING_FILE_NAME_IN_MANIFEST: i18n('a file is written in manifest without file name'),
	MISSING_ENV_FILE_IN_ZIP: i18n('missing env file in zip'),
	ARTIFACT_NOT_IN_USE: i18n('artifact not in use')
});

export const errorLevels = keyMirror({
	WARNING: 'WARNING',
	ERROR: 'ERROR'
});
export const nodeFilters = keyMirror({
	ALL: 'All'
});
export const nodeTypes = keyMirror({
	heat: i18n('Heat'),
	volume: i18n('Volume'),
	network: i18n('Network'),
	artifact: i18n('Artifact'),
	env: i18n('Environment'),
	other: i18n('')
});

export const mouseActions = keyMirror({
	MOUSE_BUTTON_CLICK: 0
});


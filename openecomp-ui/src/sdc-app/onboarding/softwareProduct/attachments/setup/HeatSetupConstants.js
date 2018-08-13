/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
import keyMirror from 'nfvo-utils/KeyMirror.js';

export const actionTypes = keyMirror(
    {
        ARTIFACT_LIST_CHANGE: null,
        ADD_ALL_UNASSIGNED_TO_ARTIFACTS: null,
        ADD_ALL_ARTIFACTS_TO_UNASSIGNED: null,

        ADD_MODULE: null,
        REMOVE_MODULE: null,
        RENAME_MODULE: null,
        FILL_HEAT_SETUP_CACHE: null,
        FILE_ASSIGN_CHANGED: null,

        MANIFEST_LOADED: null,

        GO_TO_VALIDATION: null,
        IN_VALIDATION: null,
        TOGGLE_VOL_DISPLAY: null
    },
    'heatSetup'
);

export const fileTypes = {
    YAML: { label: 'yaml', regex: /(yaml|yml)/g },
    ENV: { label: 'env', regex: /env/g },
    VOL: { label: 'vol', regex: /(yaml|yml)/g },
    VOL_ENV: { label: 'volEnv', regex: /env/g }
};

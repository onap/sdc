/**
 * Copyright (c) 2019 Vodafone Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import keyMirror from 'nfvo-utils/KeyMirror.js';

export const tabsMapping = {
    SETUP: 1,
    INPUTS: 2
};

export const actionTypes = keyMirror(
    {
        POST_VSP_TESTS: null,
        FETCH_VSP_CHECKS: null,
        SET_ACTIVE_TAB: null,
        SET_VSP_TESTS_MAP: null,
        SET_COMPLIANCE_CHECKED: null,
        SET_CERTIFICATION_CHECKED: null,
        SET_TESTS_REQUEST: null,
        SET_GENERAL_INFO: null,
        SET_VSP_VALIDATION_DISABLED: null
    },
    'SoftwareProductValidation'
);

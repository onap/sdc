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

export const overviewItems = keyMirror({
    LICENSE_AGREEMENTS: 'License Agreements',
    FEATURE_GROUPS: 'Feature Groups',
    ENTITLEMENT_POOLS: 'Entitlement Pools',
    LICENSE_KEY_GROUPS: 'License Key Groups'
});

export const overviewEditorHeaders = keyMirror({
    LICENSE_AGREEMENT: 'License Agreement',
    FEATURE_GROUP: 'Feature Group',
    ENTITLEMENT_POOL: 'Entitlement Pool',
    LICENSE_KEY_GROUP: 'License Key Group'
});

export const actionTypes = keyMirror({
    LICENSE_MODEL_OVERVIEW_TAB_SELECTED: null,
    LM_DATA_CHANGED: null
});

export const selectedButton = {
    VLM_LIST_VIEW: 'VLM_LIST_VIEW',
    NOT_IN_USE: 'NOT_IN_USE'
};

export const VLM_DESCRIPTION_FORM = 'VLMDEWSCRIPTIONFORM';

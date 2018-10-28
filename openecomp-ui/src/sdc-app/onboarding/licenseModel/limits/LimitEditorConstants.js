/*!
 * Copyright © 2016-2018 European Support Limited
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

export const actionTypes = keyMirror(
    {
        OPEN: null,
        CLOSE: null,
        DATA_CHANGED: null
    },
    'limitEditor'
);

export const LIMITS_FORM_NAME = 'LIMITSFORM';

export const selectValues = {
    METRIC: [
        { enum: '', title: i18n('please select…') },
        { enum: 'BWTH', title: 'BWTH' },
        { enum: 'Country', title: 'Country' },
        { enum: 'Session', title: 'Session' },
        { enum: 'LoB', title: 'LoB' },
        { enum: 'Site', title: 'Site' },
        { enum: 'Usage', title: 'Usage' }
    ],
    UNIT: [
        { enum: '', title: i18n('please select…') },
        { enum: 'trunk', title: 'Trunks' },
        { enum: 'user', title: 'Users' },
        { enum: 'subscriber', title: 'Subscribers' },
        { enum: 'session', title: 'Sessions' },
        { enum: 'tenant', title: 'Tenants' },
        { enum: 'token', title: 'Tokens' },
        { enum: 'seats', title: 'Seats' },
        { enum: 'TB', title: 'TB' },
        { enum: 'GB', title: 'GB' },
        { enum: 'MB', title: 'MB' }
    ],
    AGGREGATION_FUNCTION: [
        { enum: '', title: i18n('please select…') },
        { enum: 'Peak', title: 'Peak' },
        { enum: 'Average', title: 'Average' }
    ],
    TIME: [
        { enum: '', title: i18n('please select…') },
        { enum: 'Day', title: 'Day' },
        { enum: 'Month', title: 'Month' },
        { enum: 'Hour', title: 'Hour' },
        { enum: 'Minute', title: 'Minute' },
        { enum: 'Second', title: 'Second' },
        { enum: 'Milli-Second', title: 'Milli-Second' }
    ]
};

export const limitType = {
    SERVICE_PROVIDER: 'ServiceProvider',
    VENDOR: 'Vendor'
};

export const defaultState = {
    LIMITS_EDITOR_DATA: {
        metric: { choice: '', other: '' }
    }
};

export const NEW_LIMIT_TEMP_ID = 'NEW_LIMIT_TEMP_ID';

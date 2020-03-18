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
import InputOptions, {
    other as optionInputOther
} from 'nfvo-components/input/validation/InputOptions.jsx';

export const actionTypes = keyMirror(
    {
        ENTITLEMENT_POOLS_LIST_LOADED: null,
        entitlementPoolsEditor: {
            OPEN: null,
            CLOSE: null,
            DATA_CHANGED: null,
            LIMITS_LIST_LOADED: null
        }
    },
    'entitlementPoolsEditor'
);

export const enums = keyMirror({
    SELECTED_FEATURE_GROUP_TAB: {
        GENERAL: 1,
        ENTITLEMENT_POOLS: 2,
        LICENCE_KEY_GROUPS: 3
    },
    SELECTED_ENTITLEMENT_POOLS_BUTTONTAB: {
        ASSOCIATED_ENTITLEMENT_POOLS: 1,
        AVAILABLE_ENTITLEMENT_POOLS: 2
    }
});

export const defaultState = {
    ENTITLEMENT_POOLS_EDITOR_DATA: {
        entitlementMetric: { choice: '', other: '' },
        aggregationFunction: { choice: '', other: '' },
        time: { choice: '', other: '' }
    }
};

export const thresholdUnitType = {
    ABSOLUTE: 'Absolute',
    PERCENTAGE: 'Percentage'
};

export const optionsInputValues = {
    TYPE: [
        { enum: '', title: i18n('please select…') },
        { enum: 'Universal', title: 'Universal' },
        { enum: 'Unique', title: 'Unique' },
        { enum: 'One_Time', title: 'One Time' }
    ],
    TIME: [
        { enum: '', title: i18n('please select…') },
        { enum: 'Hour', title: 'Hour' },
        { enum: 'Day', title: 'Day' },
        { enum: 'Month', title: 'Month' }
    ],
    AGGREGATE_FUNCTION: [
        { enum: '', title: i18n('please select…') },
        { enum: 'Peak', title: 'Peak' },
        { enum: 'Average', title: 'Average' }
    ],
    ENTITLEMENT_METRIC: [
        { enum: '', title: i18n('please select…') },
        { enum: 'Software_Instances_Count', title: 'Software Instances' },
        { enum: 'Core', title: 'Core' },
        { enum: 'CPU', title: 'CPU' },
        { enum: 'Trunks', title: 'Trunks' },
        { enum: 'User', title: 'User' },
        { enum: 'Subscribers', title: 'Subscribers' },
        { enum: 'Tenants', title: 'Tenants' },
        { enum: 'Tokens', title: 'Tokens' },
        { enum: 'Seats', title: 'Seats' },
        { enum: 'Units_TB', title: 'Units-TB' },
        { enum: 'Units_GB', title: 'Units-GB' },
        { enum: 'Units_MB', title: 'Units-MB' }
    ]
};

export const extractValue = item => {
    if (item === undefined) {
        return '';
    } //TODO fix it later
    return item
        ? item === optionInputOther.OTHER
          ? item
          : InputOptions.getTitleByName(optionsInputValues, item)
        : '';
};

export const extractUnits = units => {
    if (units === undefined) {
        return '';
    } //TODO fix it later
    return units === 'Absolute' ? '' : '%';
};

export const tabIds = {
    GENERAL: 'GENERAL',
    SP_LIMITS: 'SP_LIMITS',
    VENDOR_LIMITS: 'VENDOR_LIMITS',
    ADD_LIMIT_BUTTON: 'ADD_LIMIT_BUTTON'
};

export const SP_ENTITLEMENT_POOL_FORM = 'SPENTITLEMENTPOOL';

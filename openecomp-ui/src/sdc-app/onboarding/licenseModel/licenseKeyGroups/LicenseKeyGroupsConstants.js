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

export const actionTypes = keyMirror({
    LICENSE_KEY_GROUPS_LIST_LOADED: 'LICENSE_KEY_GROUPS_LIST_LOADED',
    LICENSE_KEY_GROUPS_DELETE_CONFIRM: 'LICENSE_KEY_GROUPS_DELETE_CONFIRM',
    licenseKeyGroupsEditor: {
        OPEN: 'licenseKeyGroupsEditor/OPEN',
        CLOSE: 'licenseKeyGroupsEditor/CLOSE',
        DATA_CHANGED: 'licenseKeyGroupsEditor/DATA_CHANGED',
        LIMITS_LIST_LOADED: 'licenseKeyGroupsEditor/LIMITS_LIST_LOADED'
    }
});

export const defaultState = {
    licenseKeyGroupsEditor: {
        type: ''
    }
};

export const LKG_FORM_NAME = 'LKGFORM';

export const optionsInputValues = {
    TYPE: [
        { enum: '', title: i18n('please select…') },
        { enum: 'Universal', title: 'Universal' },
        { enum: 'Unique', title: 'Unique' },
        { enum: 'One_Time', title: 'One Time' }
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

export const tabIds = {
    GENERAL: 'GENERAL',
    SP_LIMITS: 'SP_LIMITS',
    VENDOR_LIMITS: 'VENDOR_LIMITS',
    ADD_LIMIT_BUTTON: 'ADD_LIMIT_BUTTON'
};

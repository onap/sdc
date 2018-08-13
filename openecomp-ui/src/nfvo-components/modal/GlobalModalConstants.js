/*!
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
        GLOBAL_MODAL_SHOW: null,
        GLOBAL_MODAL_CLOSE: null,
        GLOBAL_MODAL_ERROR: null,
        GLOBAL_MODAL_WARNING: null,
        GLOBAL_MODAL_SUCCESS: null
    },
    'globalModal'
);

export const typeEnum = {
    DEFAULT: 'custom',
    ERROR: 'error',
    WARNING: 'alert',
    SUCCESS: 'info'
};

export const modalSizes = {
    LARGE: 'large',
    SMALL: 'small',
    XLARGE: 'extraLarge',
    MEDIUM: 'medium'
};

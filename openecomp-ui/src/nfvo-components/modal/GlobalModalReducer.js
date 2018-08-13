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

import { actionTypes, typeEnum } from './GlobalModalConstants.js';

export default (state = null, action) => {
    switch (action.type) {
        case actionTypes.GLOBAL_MODAL_SHOW:
            return {
                ...action.data
            };
        case actionTypes.GLOBAL_MODAL_ERROR:
            return {
                type: typeEnum.ERROR,
                ...action.data
            };
        case actionTypes.GLOBAL_MODAL_WARNING:
            return {
                type: typeEnum.WARNING,
                ...action.data
            };

        case actionTypes.GLOBAL_MODAL_SUCCESS:
            return {
                type: typeEnum.SUCCESS,
                ...action.data
            };

        case actionTypes.GLOBAL_MODAL_CLOSE:
            return null;
        default:
            return state;
    }
};

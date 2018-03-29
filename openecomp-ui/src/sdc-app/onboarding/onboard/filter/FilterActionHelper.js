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

import { default as ItemsHelper } from 'sdc-app/common/helpers/ItemsHelper.js';
import { actionTypes, itemType } from './FilterConstants.js';

const FilterActionHelper = {
    onDataChanged(dispatch, { deltaData }) {
        dispatch({
            type: actionTypes.FILTER_DATA_CHANGED,
            deltaData
        });
    },

    async updateFilteredItems(dispatch, filter) {
        const items = await ItemsHelper.fetchItems(filter);
        let vspList = [];
        let vlmList = [];
        items.results.map(item => {
            if (item.type === itemType.VSP) {
                const { properties, ...all } = item;
                vspList.push({ ...all, ...properties });
            } else {
                vlmList.push(item);
            }
        });

        dispatch({
            type: actionTypes.UPDATE_FILTERED_LIST,
            data: {
                vspList,
                vlmList
            }
        });
    }
};

export default FilterActionHelper;

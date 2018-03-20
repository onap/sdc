/*
 * Copyright © 2016-2017 European Support Limited
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
import { actionTypes } from './LoaderConstants.js';

export default (
    state = { fetchingRequests: 0, currentlyFetching: [], isLoading: false },
    action
) => {
    let fetchingRequests = state.fetchingRequests;
    let newArray;
    switch (action.type) {
        case actionTypes.SEND_REQUEST:
            fetchingRequests++;
            newArray = state.currentlyFetching.slice();
            newArray.splice(0, 0, action.url);
            if (DEBUG) {
                console.log('Loader SEND REQUEST url: ' + action.url);
                console.log(
                    'Loader SEND REQUEST number of fetching requests: ' +
                        fetchingRequests
                );
            }
            return {
                fetchingRequests: fetchingRequests,
                currentlyFetching: newArray,
                isLoading: true
            };
        case actionTypes.RECEIVE_RESPONSE:
            fetchingRequests--;

            newArray = state.currentlyFetching.filter(item => {
                return item !== action.url;
            });
            if (DEBUG) {
                console.log('Loader RECEIVE_RESPONSE url: ' + action.url);
                console.log(
                    'Loader RECEIVE_RESPONSE: number of fetching requests: ' +
                        fetchingRequests
                );
            }
            return {
                currentlyFetching: newArray,
                fetchingRequests: fetchingRequests,
                isLoading: fetchingRequests !== 0
            };
        case actionTypes.SHOW:
            return { isLoading: true };
        case actionTypes.HIDE:
            return { isLoading: false };
        default:
            return state;
    }
};

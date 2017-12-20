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

import {combineReducers, createStore, applyMiddleware, compose} from 'redux';
import onBoardingReducersMap from './onboarding/OnboardingReducersMap.js';
import flowsReducersMap from './flows/FlowsReducersMap.js';
import loaderReducer from 'nfvo-components/loader/LoaderReducer.js';
import globalModalReducer from 'nfvo-components/modal/GlobalModalReducer.js';
import notificationsReducer from 'sdc-app/onboarding/userNotifications/NotificationsReducer.js';
const thunk = store => next => action =>
	typeof action === 'function' ?
		action(store.dispatch, store.getState) :
		next(action);


const composeEnhancers = window.__REDUX_DEVTOOLS_EXTENSION_COMPOSE__ || compose;
export const storeCreator = (initialState) => createStore(combineReducers({
	// on-boarding reducers
	...onBoardingReducersMap,

	// flows reducers
	...flowsReducersMap,
	modal: globalModalReducer,
	loader: loaderReducer,
	notifications: notificationsReducer
}), initialState, composeEnhancers(applyMiddleware(thunk)));


const store = storeCreator();

export default store;

/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

import {combineReducers, createStore} from 'redux';
import onBoardingReducersMap from './onboarding/OnboardingReducersMap.js';
import flowsReducersMap from './flows/FlowsReducersMap.js';
import notificationReducer from 'nfvo-components/notifications/NotificationReducer.js';
import loaderReducer from 'nfvo-components/loader/LoaderReducer.js';
import uploadScreenReducer from 'sdc-app/heatvalidation/UploadScreenReducer.js';
import SoftwareProductAttachmentsReducer from 'sdc-app/heatvalidation/attachments/AttachmentsReducer';

export const storeCreator = (initialState) => createStore(combineReducers({
	// on-boarding reducers
	...onBoardingReducersMap,

	// flows reducers
	...flowsReducersMap,

	// heat validation stand-alone app
	uploadScreen: combineReducers({
		upload: uploadScreenReducer,
		attachments: SoftwareProductAttachmentsReducer
	}),
	notification: notificationReducer,
	loader: loaderReducer
}), initialState, window.devToolsExtension ? window.devToolsExtension() : undefined);

const store = storeCreator();

export default store;

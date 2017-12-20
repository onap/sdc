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

import store from 'sdc-app/AppStore.js';
import Configuration from 'sdc-app/config/Configuration.js';
import {actionTypes} from 'sdc-app/onboarding/userNotifications/UserNotificationsConstants.js';


export const websocketUrl = 'ws://' + window.location.hostname + ':' + Configuration.get('websocketPort')
	+ '/'  + Configuration.get('websocketPath');

/***
 * Websocket is treated like a singleton. only need one for the application.
 */
var websocket;


export default {

	open(url, {lastScanned}) {
		if (websocket === undefined || websocket.readyState === websocket.CLOSED) {
			websocket = new WebSocket(`${url}?LAST_DELIVERED_EVENT_ID=${lastScanned}`);
			websocket.onmessage = event => store.dispatch({
				type: actionTypes.NOTIFICATION,
				data: JSON.parse(event.data)
			});
			websocket.onclose = event => {
				if(event.code && event.code === 1001) { // - Idle Timeout
					const {lastScanned} = store.getState().notifications;
					console.log('Reconnecting to Websocket');
					this.open(websocketUrl, {lastScanned});
				}
			};
			websocket.onerror = event => console.log(event);
		}
	},

	close() {
		if (websocket !== undefined) {
			websocket.close();
		}
	}
};

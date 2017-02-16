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

import NotificationConstants from './NotificationConstants.js';

export default (state = null, action) => {
	switch (action.type) {
		case NotificationConstants.NOTIFY_INFO:
			return {type: 'default', title: action.data.title, msg: action.data.msg, timeout: action.data.timeout};

		case NotificationConstants.NOTIFY_ERROR:
			return {
				type: 'error',
				title: action.data.title,
				msg: action.data.msg,
				validationResponse: action.data.validationResponse,
				timeout: action.data.timeout
			};

		case NotificationConstants.NOTIFY_WARNING:
			return {type: 'warning', title: action.data.title, msg: action.data.msg, timeout: action.data.timeout};

		case NotificationConstants.NOTIFY_SUCCESS:
			return {
				type: 'success', title: action.data.title, msg: action.data.msg, timeout: action.data.timeout
			};
		case NotificationConstants.NOTIFY_CLOSE:
			return null;

		default:
			return state;
	}

};

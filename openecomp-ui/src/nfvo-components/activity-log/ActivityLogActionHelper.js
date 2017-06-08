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
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import ActivityLogConstants from './ActivityLogConstants.js';


function baseUrl(itemId, versionId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/activity-logs/${itemId}/versions/${versionId}`;
}

export default {

	fetchActivityLog(dispatch, {itemId, versionId}){
		return RestAPIUtil.fetch(baseUrl(itemId, versionId)).then(response => dispatch({type: ActivityLogConstants.ACTIVITY_LOG_UPDATED, response}));
	}
};

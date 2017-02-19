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

import Configuration from 'sdc-app/config/Configuration.js';
import {statusEnum} from './VersionControllerConstants.js';


const VersionControllerUtils = {

	getCheckOutStatusKindByUserID(status, lockingUser) {
		let currentLoginUserID = Configuration.get('ATTUserID');
		let isCheckedOut = currentLoginUserID === lockingUser;

		return {
			status: isCheckedOut ? status : statusEnum.LOCK_STATUS,
			isCheckedOut
		};
	},

	isCheckedOutByCurrentUser(resource) {
		let currentLoginUserID = Configuration.get('ATTUserID');
		return resource.lockingUser !== undefined && resource.lockingUser === currentLoginUserID;
	},

	isReadOnly(resource) {
		const {version, viewableVersions = []} = resource;
		const latestVersion = viewableVersions[viewableVersions.length - 1];
		return version !== latestVersion || !VersionControllerUtils.isCheckedOutByCurrentUser(resource);
	}
};

export default VersionControllerUtils;

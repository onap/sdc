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
import UUID from 'uuid-js';

let toCustomUUID = (uuid) => {
	return 'U' + uuid.replace(/-/g, '');
};

let getUUID = function(num, isSync) {
	if (isSync) {
		let uuid;
		if (!num) {
			uuid = toCustomUUID(UUID.create().toString());
		} else {
			uuid = [];
			for (var i = 0; i < num; i++) {
				uuid[i] = toCustomUUID(UUID.create().toString());
			}
		}
		if (num === 1) {
			return uuid[0];
		} else {
			return uuid;
		}
	}
	return new Promise(resolve => {
		let uuid;
		if (!num) {
			uuid = toCustomUUID(UUID.create().toString());
		} else {
			uuid = [];
			for (var i = 0; i < num; i++) {
				uuid[i] = toCustomUUID(UUID.create().toString());
			}
		}
		setTimeout(() => resolve(uuid), 100);
	});
};

export default getUUID;

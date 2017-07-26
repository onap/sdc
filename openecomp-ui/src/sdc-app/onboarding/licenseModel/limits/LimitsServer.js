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

// items/{itemId}/users

let list = [
	
];

export default {
	fetch() {
		return Promise.resolve({
			listCount: list.length,
			results: list
		});
	},

	put(url, payload) {
		// let {removedUsers, addedUsers} = payload;
		// users = users.filter(user => !removedUsers.map(user => user.userId).includes(user.userId)).concat(addedUsers);
		payload.id = Math.random() * (1000 - 1) + 1;
		list.push(payload);
		return Promise.resolve();
	},

	destroy(url) {
		const parts = url.split('/');
		const id = parts[parts.length - 1];
		let newList  = list.filter(item => item.id !== id);
		list = newList;
		return Promise.resolve();
	}
};
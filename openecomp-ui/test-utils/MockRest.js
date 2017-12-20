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
const queue = {
	fetch: [],
	put: [],
	post: [],
	destroy: []
};

const initQueue = () => {
	queue['fetch'] = [];
	queue['put'] = [];
	queue['post'] = [];
	queue['destroy'] = [];
};

const handleOperation = (handler, options) => {
	if(typeof handler === 'function') {
		return Promise.resolve(handler(options));
	}
	else {
		return Promise.resolve(handler);
	}
};

export default {

	fetch(baseUrl, options) {
		const {fetch} = queue;
		if(!fetch.length) {
			throw new Error(`Fetch operation was called without proper handler. baseUrl: '${baseUrl}' options: '${options}'`);
		}
		return handleOperation(fetch.shift(), {options, baseUrl});
	},

	put(baseUrl, data, options) {
		const {put} = queue;
		if(!put.length) {
			throw new Error(`put operation was called without proper handler. baseUrl: '${baseUrl}' options: '${options}'`);
		}
		return handleOperation(put.shift(), {data, options, baseUrl});
	},

	post(baseUrl, data, options) {
		const {post} = queue;
		if(!post.length) {
			throw new Error(`post operation was called without proper handler. baseUrl: '${baseUrl}' options: '${options}'`);
		}
		return handleOperation(post.shift(), {data, options, baseUrl});
	},

	destroy(baseUrl, options) {
		const {destroy} = queue;
		if(!destroy.length) {
			throw new Error(`Destroy operation was called without proper handler. baseUrl: '${baseUrl}' options: '${options}'`);
		}
		return handleOperation(destroy.shift(), {options, baseUrl});
	},

	addHandler(operation, handler) {
		queue[operation].push(handler);
	},

	resetQueue() {
		initQueue();
	},

	checkEmptyQueue() {
		let isEmpty = true;
		let message = 'Check following calls: ';
		for (let operationType in queue) {
			if (queue[operationType].length > 0) {
				isEmpty = false;
				message += operationType;
			}
		}
		if (!isEmpty) {
			throw new Error('Queue is not empty, ' + message);
		}
	}
};

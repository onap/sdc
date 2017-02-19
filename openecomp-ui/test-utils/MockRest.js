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

const queue = {
	fetch: [],
	save: [],
	create: [],
	destroy: []
};

const initQueue = () => {
	queue['fetch'] = [];
	queue['save'] = [];
	queue['create'] = [];
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

	save(baseUrl, data, options) {
		const {save} = queue;
		if(!save.length) {
			throw new Error(`Save operation was called without proper handler. baseUrl: '${baseUrl}' options: '${options}'`);
		}
		return handleOperation(save.shift(), {data, options, baseUrl});
	},

	create(baseUrl, data, options) {
		const {create} = queue;
		if(!create.length) {
			throw new Error(`Create operation was called without proper handler. baseUrl: '${baseUrl}' options: '${options}'`);
		}
		return handleOperation(create.shift(), {data, options, baseUrl});
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
	}
};

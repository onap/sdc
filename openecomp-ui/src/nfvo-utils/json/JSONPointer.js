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
const JSONPointer = {

	extractParentPointer(pointer) {
		return pointer.replace(/\/[^\/]+$/, '');
	},

	extractLastPart(pointer) {
		const [,lastPart] = pointer.match(/\/([^\/]+)$/) || [];
		return lastPart;
	},

	extractParts(pointer = '') {
		return pointer.split('/').slice(1)
			.map(part => part.replace(/~1/g, '/'))
			.map(part => part.replace(/~0/g, '~'));
	},

	getValue(object, pointer) {
		let parts = JSONPointer.extractParts(pointer);
		return parts.reduce((object, part) => object && object[part], object);
	},

	setValue(object, pointer, value) {
		let clone = obj => Array.isArray(obj) ? [...obj] : {...obj};

		let parts = JSONPointer.extractParts(pointer),
			newObject = clone(object),
			subObject = object,
			subNewObject = newObject;

		for(let i = 0, n = parts.length - 1; i < n; ++i) {
			let nextSubObject = subObject && subObject[parts[i]];
			subNewObject = subNewObject[parts[i]] = nextSubObject ? clone(nextSubObject) : {};
			subObject = nextSubObject;
		}
		subNewObject[parts[parts.length - 1]] = value;

		return newObject;
	}
};

export default JSONPointer;

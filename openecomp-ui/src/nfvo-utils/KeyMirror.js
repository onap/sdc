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

var keyMirror = function (obj) {
	var ret = {};
	var key;
	var val;
	if (!(obj instanceof Object && !Array.isArray(obj))) {
		throw new Error('keyMirror(...): Argument must be an object.');
	}
	for (key in obj) {
		if (obj.hasOwnProperty(key)) {
			val = obj[key];
			if (val instanceof Object) {
				ret[key] = keyMirror(obj[key]);
			} else if(val !== null && val !== undefined){
				ret[key] = val;
			}
			else {
				ret[key] = Symbol(key);
			}
		}
	}
	return Object.freeze(ret);
};

export default keyMirror;

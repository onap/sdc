/*!
 * Copyright © 2016-2018 European Support Limited
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
const keyMirror = (obj, namespace) => {
    let ret = {};
    let key;
    let val;
    if (!(obj instanceof Object && !Array.isArray(obj))) {
        throw new Error('keyMirror(...): Argument must be an object.');
    }
    for (key in obj) {
        if (obj.hasOwnProperty(key)) {
            val = obj[key];
            if (val instanceof Object) {
                ret[key] = keyMirror(obj[key], namespace);
            } else if (val !== null && val !== undefined) {
                ret[key] = val;
            } else {
                ret[key] = `${namespace ? namespace : UUID.create()}/${key}`;
            }
        }
    }
    return Object.freeze(ret);
};

export default keyMirror;

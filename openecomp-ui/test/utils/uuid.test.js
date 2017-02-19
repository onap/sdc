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

import uuid from 'src/nfvo-utils/UUID.js';
import expect from 'expect';

describe('UUID', () => {

	it('function does exist', () => {
		expect(uuid).toExist();
	});

	it('generate UUID synchronously', () => {
		let result = uuid(undefined, true);
		expect(result).toExist();
	});

	it('generate UUID synchronously with number', () => {
		let result = uuid(5, true);
		expect(result).toExist();
	});

	it('generate UUID synchronously with number', () => {
		let result = uuid(1, true);
		expect(result).toExist();
	});

	it('generate UUID asynchronously', done => {
		uuid().then(result => {
			expect(result).toExist();
			done();
		});
	});

});

/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
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
import {Factory} from 'rosie';
import randomstring from 'randomstring';

Factory.define('UserFactory')
	.attrs(
	{
		firstName: () => randomstring.generate(5),
		lastName: () => randomstring.generate(6),
		userId: () => randomstring.generate(11),
		email: randomstring.generate(5) + '@' + randomstring.generate(6) + '.com',
		role: 'OPS',
		lastLoginTime: 0,
		status: 'ACTIVE',
		fullName: () => randomstring.generate(5) + ' ' + randomstring.generate(6),
	}
);


export const UserFactory = new Factory()
	.extend('UserFactory');
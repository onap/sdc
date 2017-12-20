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

import {Factory} from 'rosie';
import randomstring from 'randomstring';
import IdMixin from 'test-utils/factories/mixins/IdMixin.js';


export const RevisionsPagePropsFactory = new Factory()
	.sequence('user')
	.attr('message', 'message')
	.attr('date', new Date().getTime())
	.extend(IdMixin).after(function(revisions) {
		let longMessage = revisions.user % 2;
		revisions.user = 'Carlos Santana';
		revisions.message = (longMessage) ? randomstring.generate(60) : randomstring.generate(10);
	});

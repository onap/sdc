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

export const VersionsPageCreationFactory = new Factory()
	.attrs({
		name: () => randomstring.generate(),
		description: () => randomstring.generate(),
		creationMethod: () => ['major', 'minor'][Math.round(Math.random())]
	});

export const VersionsPageCreationFieldInfoFactory = new Factory()
	.attrs({
		description: () => ({
			isValid: true,
			errorText: '',
			validations: [{type: 'required', data: true}, {type: 'maxLength', data: 1000}]
		}),
		creationMethod: () => ({
			isValid: true,
			errorText: '',
			validations: [{type: 'required', data: true}]
		})
	});

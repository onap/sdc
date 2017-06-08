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

export const SubmitErrorMessageFactory = new Factory()
	.attrs({
		vspErrors: [
			{message: randomstring.generate(5)},
			{message: randomstring.generate(5)},
		],
		licensingDataErrors: [
			{message: randomstring.generate(5)},
			{message: randomstring.generate(5)},
		],
		questionnaireValidationResult: {
			validationData: [
				{
					entityName: randomstring.generate(5),
					errors: ['1212', '232323']
				}
			]
		},
		uploadDataErrors: {
			eca_oam: [
				{
					level: 'ERROR',
					message: randomstring.generate(25)
				},
				{
					level: 'ERROR',
					message: randomstring.generate(25)
				}
			],
			cmaui_env: [
				{
					level: 'ERROR',
					message: randomstring.generate(25)
				}
			]
		}
	});

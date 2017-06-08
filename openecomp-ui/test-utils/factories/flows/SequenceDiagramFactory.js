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
import UUID from 'uuid-js';

Factory.define('LifeLineFactory')
	.attr('name', 'participantName')
	.sequence('id', index => index.toString())
	.sequence('index')
	.sequence('x', index => 175 + (index - 1) * 400);

Factory.define('StepMessageFactory')
	.attrs({
		name: '[Unnamed Message]',
		type: 'request',
		from: '1',
		to: '2',
	})
	.attr('id', () => UUID.create(4))
	.sequence('index');

Factory.define('StepFactory')
	.attr('message', () => Factory.build('StepMessageFactory'));

export default new Factory()
	.option('participants', [])
	.option('stepsCount', 2)
	.attr('diagram', ['participants', 'stepsCount'], (participants, stepsCount) => ({
		metadata: {
			'id': '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b.zizizi',
			'name': 'zizizi',
			'ref': 'BLANK'
		},
		lifelines: participants.map(participant => Factory.build('LifeLineFactory', participant)),
		steps: Factory.buildList('StepFactory', stepsCount)
	})
);

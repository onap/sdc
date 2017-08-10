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
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import {Factory} from 'rosie';
import {limitType} from 'sdc-app/onboarding/licenseModel/limits/LimitEditorConstants.js';
import IdMixin from 'test-utils/factories/mixins/IdMixin.js';

Factory.define('LimitBaseFactory')
	.attrs({
		name: 'SpLimit1',
		type: limitType.SERVICE_PROVIDER,
		description: 'fgfg',
		value: 45,
		aggregationFunction: 'Peak',
		time: 'Day'
	});

export const LimitPostFactory = new Factory()
	.attrs({metric: {choice: 'BWDT', other: ''}, unit: {choice: 'GB', other: ''}})
	.extend('LimitBaseFactory');

export const LimitItemFactory = new Factory()
	.attrs({metric: 'BWDT', unit:  'GB'})
	.extend('LimitBaseFactory')
	.extend(IdMixin);

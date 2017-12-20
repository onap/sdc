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
import {type2Name, fileTypes} from 'sdc-app/onboarding/softwareProduct/components/monitoring/SoftwareProductComponentsMonitoringConstants.js';

export const trap = type2Name[fileTypes.SNMP_TRAP];
export const poll = type2Name[fileTypes.SNMP_POLL];
export const ves = type2Name[fileTypes.VES_EVENT];

export const VSPComponentsMonitoringRestFactory = new Factory()
	.option('createTrap', false)
	.option('createPoll', false)
	.option('createVes', false)

	.attr(trap, ['createTrap'], (createTrap) => createTrap ? randomstring.generate() : undefined)
	.attr(poll, ['createPoll'], (createPoll) => createPoll ? randomstring.generate() : undefined)
	.attr(ves, ['createVes'], (createVes) => createVes ? randomstring.generate() : undefined);


export const VSPComponentsMonitoringViewFactory = new Factory()
	.extend(VSPComponentsMonitoringRestFactory);

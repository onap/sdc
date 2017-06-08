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

export const VSPComponentsMonitoringRestFactory = new Factory()
	.option('snmpTrapFlag', false)
	.option('snmpPollFlag', false)
	.attr('snmpTrap', ['snmpTrapFlag'], snmpTrapFlag => snmpTrapFlag ? randomstring.generate() : undefined)
	.attr('snmpPoll', ['snmpPollFlag'], snmpPollFlag => snmpPollFlag ? randomstring.generate() : undefined);

export const VSPComponentsMonitoringViewFactory = new Factory()
	.extend(VSPComponentsMonitoringRestFactory)
	.after(monitoring => {
		monitoring['trapFilename'] = monitoring['snmpTrap'];
		monitoring['pollFilename'] = monitoring['snmpPoll'];
		delete monitoring['snmpTrap'];
		delete monitoring['snmpPoll'];
	});

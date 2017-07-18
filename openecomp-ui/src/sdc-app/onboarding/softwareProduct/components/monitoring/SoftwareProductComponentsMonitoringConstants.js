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
import keyMirror from 'nfvo-utils/KeyMirror.js';
import i18n from 'nfvo-utils/i18n/i18n.js';


export const actionTypes = keyMirror({
	MONITOR_FILES_DATA_CHANGE: null,
	MONITOR_UPLOADED: null,
	MONITOR_DELETED: null
});

export const fileTypes = {
	SNMP_TRAP: 'SNMP_TRAP',
	SNMP_POLL: 'SNMP_POLL',
	VES_EVENT: 'VES_EVENTS'
};

export const type2Name = {
	SNMP_TRAP: 'snmpTrap',
	SNMP_POLL: 'snmpPoll',
	VES_EVENTS: 'vesEvent'
};


export const type2Title = {
	SNMP_TRAP : i18n('SNMP Trap'),
	SNMP_POLL : i18n('SNMP Poll'),
	VES_EVENTS: i18n('VES')
};


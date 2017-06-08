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

export default new Factory()
	.attrs({
		'general': {
			'regionsData': {
				'regions': '',
				'multiRegion': ''
			},
			'storageDataReplication': {
				'storageReplicationAcrossRegion': '',
				'storageReplicationSize': '',
				'storageReplicationFrequency': '',
				'storageReplicationSource': '',
				'storageReplicationDestination': ''
			},
			'availability': {
				'useAvailabilityZonesForHighAvailability': ''
			},
			'affinityData': {
				'affinityGrouping': '',
				'antiAffinityGrouping': ''
			}
		}
	});

export const VspDataMapFactory = new Factory()
	.attrs({
		'general/regionsData/regions': '',
		'general/regionsData/multiRegion': '',
		'general/storageDataReplication/storageReplicationAcrossRegion' : '',
		'general/storageDataReplication/storageReplicationSize' : '',
		'general/storageDataReplication/storageReplicationFrequency' : '',
		'general/storageDataReplication/storageReplicationSource' : '',
		'general/storageDataReplication/storageReplicationDestination' : '',
		'general/availability/useAvailabilityZonesForHighAvailability' : '',
		'general/affinityData/affinityGrouping' : '',
		'general/affinityData/antiAffinityGrouping' : ''
	});


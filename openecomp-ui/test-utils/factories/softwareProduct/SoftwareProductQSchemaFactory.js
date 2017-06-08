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

const parsePropertiesForSchema = (obj) => {
	let schemaProperties = {};

	Object.keys(obj).forEach(prop => {
		let type = typeof obj[prop];
		if (type === undefined){throw new Error('Schema property cannot be undefined');}
		if (type === 'object'){
			if (Array.isArray(obj[prop])) {
				schemaProperties[prop] = {type: 'array'};
			} else if (Object.is(obj[prop], null)){
				throw new Error('Schema property cannot be null');
			} else {
				schemaProperties[prop] = {properties: parsePropertiesForSchema(obj[prop]), type: 'object'};
			}
		} else {
			schemaProperties[prop] = {type};
		}
	});

	return schemaProperties;
};

export default new Factory()
	.after(schema => {
		const propertiesForSchema = parsePropertiesForSchema(schema);
		for (let attribute in schema) {
			delete schema[attribute];
		}
		schema.$schema = 'http://json-schema.org/draft-04/schema#';
		schema.type = 'object';
		schema.properties = propertiesForSchema;
	});


export const SchemaGenericFieldInfoFactory = new Factory()
	.attrs({
		'general/affinityData': {
			'hasErrors': false,
			'errorText': '',
			'type': 'string',
			'enum': [{'enum': 'Affinity', 'title': 'Affinity'}, {
				'enum': 'Anti Affinity',
				'title': 'Anti Affinity'
			}, {'enum': 'None', 'title': 'None'}],
			'default': '',
			'validations': []
		},
		'general/availability/useAvailabilityZonesForHighAvailability': {
			'hasErrors': false,
			'errorText': '',
			'type': 'boolean',
			'default': false,
			'validations': []
		},
		'general/regionsData/multiRegion': {
			'hasErrors': false,
			'errorText': '',
			'type': 'boolean',
			'default': false,
			'validations': []
		},
		'general/regionsData/regions': {
			'hasErrors': false,
			'errorText': '',
			'enum': [{'enum': 'Alphareta', 'title': 'Alphareta'}, {
				'enum': 'Birmingham',
				'title': 'Birmingham'
			}, {'enum': 'Dallas', 'title': 'Dallas'}, {
				'enum': 'Fairfield CA',
				'title': 'Fairfield CA'
			}, {'enum': 'Hayward CA', 'title': 'Hayward CA'}, {'enum': 'Lisle', 'title': 'Lisle'}, {
				'enum': 'Mission',
				'title': 'Mission'
			}, {'enum': 'San Diego', 'title': 'San Diego'}, {'enum': 'Secaucus', 'title': 'Secaucus'}],
			'items': {
				'type': 'string',
				'enum': ['', 'Alphareta', 'Birmingham', 'Dallas', 'Fairfield CA', 'Hayward CA', 'Lisle', 'Mission', 'San Diego', 'Secaucus'],
				'default': ''
			},
			'validations': []
		},
		'general/storageDataReplication/storageReplicationAcrossRegion': {
			'hasErrors': false,
			'errorText': '',
			'type': 'boolean',
			'default': false,
			'validations': []
		},
		'general/storageDataReplication/storageReplicationSize': {
			'hasErrors': false,
			'errorText': '',
			'type': 'number',
			'exclusiveMaximum': true,
			'validations': [{'type': 'maximumExclusive', 'data': 100}]
		},
		'general/storageDataReplication/storageReplicationFrequency': {
			'hasErrors': false,
			'errorText': '',
			'type': 'number',
			'validations': [{'type': 'minimum', 'data': 5}]
		},
		'general/storageDataReplication/storageReplicationSource': {
			'hasErrors': false,
			'errorText': '',
			'type': 'string',
			'validations': [{'type': 'maxLength', 'data': 300}]
		},
		'general/storageDataReplication/storageReplicationDestination': {
			'hasErrors': false,
			'errorText': '',
			'type': 'string',
			'validations': [{'type': 'maxLength', 'data': 300}]
		}
	});

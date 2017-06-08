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

export const VSPComponentsNicFactory = new Factory()
	.attrs({
		name: () => randomstring.generate(),
		description: () => randomstring.generate(),
		networkId: 'network',
	})
	.attr('networkName', ['name'], name => `n${name}`);

export const VSPComponentsNicWithIdFactory = new Factory()
	.extend(VSPComponentsNicFactory)
	.extend(IdMixin);

export const VSPComponentsNetworkFactory = new Factory()
	.attrs({
		nicEditor: {},
		nicList: []
	});

export const VSPComponentsNetworkQDataFactory = new Factory()
	.attrs({
		protocols: {
			protocolWithHighestTrafficProfile: 'UDP',
			protocols: ['UDP']
		},
		ipConfiguration: {
			ipv4Required: true
		}
	});

export const VSPComponentsNetworkDataMapFactory = new Factory()
	.attrs({
		'protocols/protocolWithHighestTrafficProfile' : 'UDP',
		'protocols/protocols' : ['UDP'],
		'ipConfiguration/ipv4Required' : true
	});

export const VSPComponentsNicFactoryGenericFieldInfo = new Factory()
	.attrs({
		'description' : {
			isValid: true,
				errorText: '',
				validations: []
		},
		'name' : {
			isValid: true,
				errorText: '',
				validations: []
		}
	});

export const VSPComponentsNicFactoryQGenericFieldInfo = new Factory()
	.attrs({
			'protocols/protocols': {
				isValid: true,
				errorText: '',
				'enum': [
					{
						'enum': 'TCP',
						title: 'TCP'
					},
					{
						'enum': 'UDP',
						title: 'UDP'
					},
					{
						'enum': 'SCTP',
						title: 'SCTP'
					},
					{
						'enum': 'IPsec',
						title: 'IPsec'
					}
				],
				items: {
					type: 'string',
					'enum': [
						'',
						'TCP',
						'UDP',
						'SCTP',
						'IPsec'
					],
					'default': ''
				},
				minItems: 1,
				validations: []
			},
			'protocols/protocolWithHighestTrafficProfile': {
				isValid: true,
				errorText: '',
				validations: []
			},
			'ipConfiguration/ipv4Required': {
				isValid: true,
				errorText: '',
				type: 'boolean',
				'default': true,
				validations: []
			},
			'ipConfiguration/ipv6Required': {
				isValid: true,
				errorText: '',
				type: 'boolean',
				'default': false,
				validations: []
			},
			'network/networkDescription': {
				isValid: true,
				errorText: '',
				type: 'string',
				validations: [
					{
						type: 'pattern',
						data: '[A-Za-z]+'
					},
					{
						type: 'maxLength',
						data: 300
					}
				]
			},
			'sizing/describeQualityOfService': {
				isValid: true,
				errorText: '',
				type: 'string',
				validations: []
			},
			'sizing/inflowTrafficPerSecond/packets/peak': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/inflowTrafficPerSecond/packets/avg': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/inflowTrafficPerSecond/bytes/peak': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/inflowTrafficPerSecond/bytes/avg': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/outflowTrafficPerSecond/packets/peak': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/outflowTrafficPerSecond/packets/avg': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/outflowTrafficPerSecond/bytes/peak': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/outflowTrafficPerSecond/bytes/avg': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/flowLength/packets/peak': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/flowLength/packets/avg': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/flowLength/bytes/peak': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/flowLength/bytes/avg': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/acceptableJitter/mean': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/acceptableJitter/max': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/acceptableJitter/variable': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: []
			},
			'sizing/acceptablePacketLoss': {
				isValid: true,
				errorText: '',
				type: 'number',
				validations: [
					{
						type: 'minimum',
						data: 0
					},
					{
						type: 'maximum',
						data: 100
					}
				]
			}
	});

export const VSPComponentsVersionControllerFactory = new Factory()
	.attrs({
		version: { id: '1.1', label: '1.1'},
		viewableVersions: [{id: '1.0', label: '1.0'}, {id: '1.1', label: '1.1'}, {id: '1.2', label: '1.2'}],
		status: 'locked',
		isCheckedOut: true
	});

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
import SequenceDiagramFactory from './SequenceDiagramFactory.js';
import ParticipantFactory from './ParticipantFactory.js';
import UUID from 'uuid-js';

const serviceIDByArtifactType = {
	NETWORK_CALL_FLOW: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b',
	WORKFLOW: '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b',
	PUPPET: '0280b577-2c7b-426e-b7a2-f0dc16508c37'
};

const defaultServiceId = '338d75f0-aec8-4eb4-89c9-8733fcd9bf3b';

Factory.define('FlowBaseFactory')
	.attrs({
		artifactName: 'zizizi',
		artifactType: 'WORKFLOW',
		description: 'aslkjdfl asfdasdf',
	});

Factory.define('FlowBaseWithServiceIdFactory')
	.extend('FlowBaseFactory')
	.attr(
		'serviceID', ['artifactType'], artifactType => serviceIDByArtifactType[artifactType] || defaultServiceId
	);

Factory.define('FlowBaseWithUniqueIdFactory')
	.extend('FlowBaseFactory')
	.attr('uniqueId', ['artifactType', 'artifactName'], (artifactType, artifactName) => `${serviceIDByArtifactType[artifactType] || defaultServiceId}.${artifactName}`);

Factory.define('FlowBaseFetchAndDeleteFactory')
	.extend('FlowBaseWithServiceIdFactory')
	.extend('FlowBaseWithUniqueIdFactory')
	.attrs({
		participants: []
	});

Factory.define('FlowBaseWithEsIdFactory')
	.extend('FlowBaseWithUniqueIdFactory')
	.attr('esId', ['uniqueId'], uniqueId => uniqueId);

export const FlowBasicFactory = new Factory()
	.extend('FlowBaseFactory').attrs({uniqueId: () => UUID.create(4)});

export const FlowCreateFactory = new Factory()
	.extend('FlowBaseWithServiceIdFactory');

export const FlowPostRequestFactory = new Factory()
	.extend('FlowBaseFactory')
	.attrs({
		artifactGroupType: 'INFORMATIONAL',
		payloadData: 'eyJWRVJTSU9OIjp7Im1ham9yIjoxLCJtaW5vciI6MH0sImRlc2NyaXB0aW9uIjoiYXNsa2pkZmwgYXNmZGFzZGYifQ=='
	})
	.attr('artifactLabel', ['artifactName'], name => name);

Factory.define('FlowPostResponeAndUpdateBaseFactory')
	.extend('FlowBaseFactory')
	.extend('FlowBaseWithUniqueIdFactory')
	.extend('FlowBaseWithEsIdFactory')
	.attrs({
		artifactGroupType: 'INFORMATIONAL',
		artifactUUID: () => UUID.create(4),
		artifactVersion: '1',
		creationDate: 1470144601623,
		lastUpdateDate: 1470144601623,
		mandatory: false,
		timeout: 0,
		artifactChecksum: 'NjBmYjc4NGM5MWIwNmNkMDhmMThhMDAwYmQxYjBiZTU='
	})
	.attr('artifactLabel', ['artifactName'], name => name)
	.attr('artifactDisplayName', ['artifactName'], name => name);

export const FlowPostResponseFactory = new Factory()
	.extend('FlowPostResponeAndUpdateBaseFactory')
	.attrs({
		attUidLastUpdater: 'cs0008',
		payloadUpdateDate: 1470144602131,
		serviceApi: false,
		updaterFullName: 'Carlos Santana',
		artifactCreator: 'cs0008'
	});

export const FlowUpdateRequestFactory = new Factory()
	.extend('FlowPostResponeAndUpdateBaseFactory')
	.extend('FlowBaseFetchAndDeleteFactory')
	.extend('FlowBaseWithEsIdFactory')
	.attrs({
		heatParameters: [],
		participants: ParticipantFactory.buildList(10)
	})
	.attr('sequenceDiagramModel', ['participants'], participants => SequenceDiagramFactory.build({}, {participants}));

export const FlowFetchRequestFactory = new Factory()
	.extend('FlowBaseFetchAndDeleteFactory');

export const FlowDeleteRequestFactory = new Factory()
	.extend('FlowBaseFetchAndDeleteFactory');

export const FlowFetchResponseFactory = new Factory()
	.extend('FlowBaseFactory')
	.attrs({
		base64Contents: 'eyJWRVJTSU9OIjp7Im1ham9yIjoxLCJtaW5vciI6MH0sImRlc2NyaXB0aW9uIjoiYXNsa2pkZmwgYXNmZGFzZGYifQ=='
	});

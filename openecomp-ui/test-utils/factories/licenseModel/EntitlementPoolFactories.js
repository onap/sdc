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
import {overviewEditorHeaders} from 'sdc-app/onboarding/licenseModel/overview/LicenseModelOverviewConstants.js';

Factory.define('EntitlementPoolBaseFactory')
	.attrs({
		name: 'EntitlementPoolName',
		description: 'description',
		entitlementMetric: {'choice': 'User', 'other': ''},
		manufacturerReferenceNumber: '123'
	});

Factory.define('EntitlementPoolExtendedBaseFactory')
	.extend('EntitlementPoolBaseFactory')
	.attrs({
		thresholdValue: 75,
		thresholdUnits: '%',
		increments: 'string',
		aggregationFunction: {'choice': 'Average', 'other': ''},
		operationalScope: {'choices': ['Other'], 'other': 'blabla'},
		time: {'choice': 'Hour', 'other': ''}
	});

export const EntitlementPoolListItemFactory = new Factory()
	.extend('EntitlementPoolBaseFactory')
	.attrs({
		id: () => Math.floor(Math.random() * (1000 - 1) + 1),
		itemType: overviewEditorHeaders.ENTITLEMENT_POOL
	});

export const EntitlementPoolStoreFactory = new Factory()
	.extend('EntitlementPoolExtendedBaseFactory')
	.attrs({
		id: () => Math.floor(Math.random() * (1000 - 1) + 1),
		referencingFeatureGroups: []
	});

export const EntitlementPoolDataListFactory = new Factory()
	.extend('EntitlementPoolExtendedBaseFactory')
	.attrs({
		id: () => Math.floor(Math.random() * (1000 - 1) + 1),
		referencingFeatureGroups: [],
		itemType: overviewEditorHeaders.ENTITLEMENT_POOL
	});

export const EntitlementPoolPostFactory = new Factory()
	.extend('EntitlementPoolExtendedBaseFactory');

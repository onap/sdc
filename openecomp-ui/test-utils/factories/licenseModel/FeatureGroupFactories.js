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
import { Factory } from 'rosie';
import {overviewEditorHeaders} from 'sdc-app/onboarding/licenseModel/overview/LicenseModelOverviewConstants.js';
import IdMixin from 'test-utils/factories/mixins/IdMixin.js';

Factory.define('FeatureGroupBaseFactory')
	.attrs({
		'name': 'featureGroup',
		'description': 'description'
	});

Factory.define('FeatureGroupExtendedBaseFactory')
	.extend('FeatureGroupBaseFactory')
	.attrs({
		'partNumber': '1212'
	});

export const FeatureGroupListItemFactory = new Factory()
	.extend('FeatureGroupBaseFactory')
	.extend(IdMixin)
	.attrs({
		children: [],
		isCollapsed: true,
		itemType: overviewEditorHeaders.FEATURE_GROUP
	});

export const FeatureGroupDispatchFactory = new Factory()
	.extend('FeatureGroupExtendedBaseFactory')
	.attrs({
		'licenseKeyGroupsIds': [],
		'entitlementPoolsIds': []
	});

export const FeatureGroupStoreFactory = new Factory()
	.extend('FeatureGroupExtendedBaseFactory')
	.extend(IdMixin)
	.attrs({
		licenseKeyGroupsIds: [],
		entitlementPoolsIds: [],
		referencingLicenseAgreements: []
	});

export const FeatureGroupDataListFactory = new Factory()
	.extend('FeatureGroupExtendedBaseFactory')
	.extend(IdMixin)
	.attrs({
		licenseKeyGroupsIds: [],
		entitlementPoolsIds: [],
		referencingLicenseAgreements: [],
		children: [],
		itemType: overviewEditorHeaders.FEATURE_GROUP
	});

export const FeatureGroupPostFactory = new Factory()
	.extend('FeatureGroupExtendedBaseFactory')
	.attrs({
		addedLicenseKeyGroupsIds: [],
		addedEntitlementPoolsIds: []
	});

export const FeatureGroupPutFactory = new Factory()
	.extend('FeatureGroupExtendedBaseFactory')
	.attrs({
		addedLicenseKeyGroupsIds: [],
		addedEntitlementPoolsIds: [],
		removedLicenseKeyGroupsIds: [],
		removedEntitlementPoolsIds: []
	});

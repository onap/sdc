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

Factory.define('LicenseKeyGroupBaseFactory')
	.attrs({
		name: 'License Key Group',
		description: 'wewe',
		type: 'Unique',
		operationalScope: {
			choices: ['Data_Center']
		}
	});

export const LicenseKeyGroupListItemFactory = new Factory()
	.extend('LicenseKeyGroupBaseFactory')
	.attrs({
		id: () => Math.floor(Math.random() * (1000 - 1) + 1),
		itemType: overviewEditorHeaders.LICENSE_KEY_GROUP
	});

export const LicenseKeyGroupStoreFactory = new Factory()
	.extend('LicenseKeyGroupBaseFactory')
	.attrs({
		id: () => Math.floor(Math.random() * (1000 - 1) + 1),
		referencingFeatureGroups: []
	});

export const LicenseKeyGroupDataListFactory = new Factory()
	.extend('LicenseKeyGroupBaseFactory')
	.attrs({
		id: () => Math.floor(Math.random() * (1000 - 1) + 1),
		referencingFeatureGroups: [],
		itemType: overviewEditorHeaders.LICENSE_KEY_GROUP
	});

export const LicenseKeyGroupPostFactory = new Factory()
	.extend('LicenseKeyGroupBaseFactory');

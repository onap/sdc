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
import { selectedButton } from 'sdc-app/onboarding/licenseModel/overview/LicenseModelOverviewConstants.js';
import IdMixin from 'test-utils/factories/mixins/IdMixin.js';
import randomstring from 'randomstring';
import VersionControllerUtilsFactory from 'test-utils/factories/softwareProduct/VersionControllerUtilsFactory.js';

Factory.define('LicenseModelBaseFactory')
	.attrs({
		vendorName: 'vlm1',
		description: 'string',
		iconRef: 'icon'
	});

export const LicenseModelCreationFactory = new Factory()
	.attrs({
		data: {
			vendorName: () => randomstring.generate(),
			description: () => randomstring.generate()
		}
	});

export const LicenseModelPostFactory = new Factory()
	.extend('LicenseModelBaseFactory');

export const LicenseModelDispatchFactory = new Factory()
	.extend('LicenseModelBaseFactory');

export const LicenseModelStoreFactory = new Factory()
	.extend('LicenseModelBaseFactory')
	.extend(IdMixin);


export const FinalizedLicenseModelFactory = new Factory()
	.extend(IdMixin)
	.attrs({
		vendorName: randomstring.generate(),
		description: randomstring.generate(),
		iconRef: 'iconRef_lBpEgzhuiY1',
		version: {id: '1.0', label: '1.0'},
		status: 'Final',
		viewableVersion: [{id: '1.0', label: '1.0'}],
		finalVersions: [{id: '1.0', label: '1.0'}]
	});

export const LicenseModelOverviewFactory = new Factory()
.attrs({
	licenseModelEditor: {
		data: {
			...Factory.attributes('LicenseModelBaseFactory'),
			id: () => Math.floor(Math.random() * (1000 - 1) + 1),
			...VersionControllerUtilsFactory.build()
		}
	},
	entitlementPool: {},
	licenseAgreement: {},
	featureGroup: {},
	licenseKeyGroup: {},
	licenseModelOverview: {
		descriptionEditor : { data : { description : undefined}},
		selectedTab: selectedButton.VLM_LIST_VIEW
	}
});

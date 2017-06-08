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

Factory.define('LicenseAgreementBaseFactory')
	.attrs({
		name: 'License Agreement',
		description: 'sdsd',
		licenseTerm: {
			choice: 'Fixed_Term'
		}
	});

Factory.define('LicenseAgreementExtendedBaseFactory')
	.extend('LicenseAgreementBaseFactory')
	.attrs({
		requirementsAndConstrains: 'req_and_constraints_ADDED_LA'
	});

export const LicenseAgreementListItemFactory = new Factory()
	.extend('LicenseAgreementExtendedBaseFactory')
	.extend(IdMixin)
	.attrs({
		children: [],
		isCollapsed: true,
		itemType: overviewEditorHeaders.LICENSE_AGREEMENT
	});

export const LicenseAgreementDispatchFactory = new Factory()
	.extend('LicenseAgreementExtendedBaseFactory')
	.attrs({
		featureGroupsIds: []
	});

export const LicenseAgreementStoreFactory = new Factory()
	.extend('LicenseAgreementExtendedBaseFactory')
	.extend(IdMixin)
	.attrs({
		featureGroupsIds: []
	});

export const LicenseAgreementDataListFactory = new Factory()
	.extend('LicenseAgreementExtendedBaseFactory')
	.extend(IdMixin)
	.attrs({
		featureGroupsIds: [],
		children:[],
		itemType: overviewEditorHeaders.LICENSE_AGREEMENT
	});

export const LicenseAgreementPostFactory = new Factory()
	.extend('LicenseAgreementExtendedBaseFactory')
	.attrs({
		addedFeatureGroupsIds: []
	});

export const LicenseAgreementPutFactory = new Factory()
	.extend('LicenseAgreementExtendedBaseFactory')
	.attrs({
		addedFeatureGroupsIds: [],
		removedFeatureGroupsIds: []
	});

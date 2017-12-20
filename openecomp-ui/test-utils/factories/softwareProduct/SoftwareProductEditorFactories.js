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
import IdMixin from 'test-utils/factories/mixins/IdMixin.js';
import randomstring from 'randomstring';

Factory.define('VSPBaseFactory')
	.attrs(
	{
		name: 'VSP2',
		description: 'sdsd',
		category: 'resourceNewCategory.application l4+',
		subCategory: 'resourceNewCategory.application l4+.media servers',
		vendorName: 'V1 ',
		vendorId: () => randomstring.generate(33),
		licensingVersion: {id: '1', label: '1'},
		licensingData: {},
		icon: 'icon',
		version: {id: '123'}
	}
);

Factory.define('LicensingDataMixin')
	.attrs({
		licensingData: {
			licenseAgreement: () => randomstring.generate(33),
			featureGroups: [
				() => randomstring.generate(33)
			]
		}
	});

export const VSPEditorFactory = new Factory()
	.extend('VSPBaseFactory')
	.extend(IdMixin);

export const VSPEditorPostFactory = new Factory()
	.extend('VSPBaseFactory');

export const VSPEditorFactoryWithLicensingData = new Factory()
	.extend('VSPBaseFactory')
	.extend('LicensingDataMixin')
	.extend(IdMixin);

export const VSPEditorPostFactoryWithLicensingData = new Factory()
	.extend('VSPBaseFactory')
	.extend('LicensingDataMixin');

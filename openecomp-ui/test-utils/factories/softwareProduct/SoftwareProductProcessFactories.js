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

Factory.define('VSPProcessBaseFactory')
	.attrs({
		name: 'Pr1',
		description: 'string',
		type: ''
	});

Factory.define('VSPProcessBaseFactoryWithType')
	.attrs({
		name: 'Pr1',
		description: 'string',
		type: 'Other'
	});


Factory.define('VSPProcessBaseFactoryWithNullType')
	.attrs({
		name: 'Pr1',
		description: 'string',
		type: null
	});

Factory.define('FormDataMixin')
	.option('artifactName', 'artifact')
	.attr('formData', ['artifactName'], artifactName => ({name: artifactName}));


export const VSPProcessPostFactory = new Factory()
	.extend('VSPProcessBaseFactory');

export const VSPProcessStoreFactory = new Factory()
	.extend('VSPProcessBaseFactoryWithNullType')
	.extend(IdMixin);


export const VSPProcessPostFactoryWithType = new Factory()
	.extend('VSPProcessBaseFactoryWithType');

export const VSPProcessStoreFactoryWithType = new Factory()
	.extend('VSPProcessBaseFactoryWithType')
	.extend(IdMixin);


export const VSPProcessPostWithFormDataFactory = new Factory()
	.extend(VSPProcessPostFactoryWithType)
	.extend('FormDataMixin');

export const VSPProcessStoreWithFormDataFactory = new Factory()
	.extend(VSPProcessStoreFactoryWithType)
	.extend('FormDataMixin');


export const VSPProcessStoreWithArtifactNameFactory = new Factory()
	.extend(VSPProcessStoreFactoryWithType)
	.attr('artifactName', 'artifact');

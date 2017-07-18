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

export const VSPComponentsFactory =  new Factory()
	.option('componentName', 'comp')
	.option('componentType', 'server')
	.attr('displayName', ['componentName', 'componentType'], (componentName, componentType) => `${componentName}_${componentType}`)
	.attr('name', ['displayName'], displayName => `com.ecomp.d2.resource.vfc.nodes.heat.${displayName}`)
	.attr('id', () => randomstring.generate())
	.attr('vfcCode', 'code')
	.attr('nfcFunction', 'function')
	.attr('description', 'description');

export const VSPComponentsGeneralFactory = new Factory()
	.attrs({
		hypervisor: {
			containerFeatureDescription: 'aaaUpdated',
			drivers: 'bbbUpdated',
			hypervisor: 'cccUpdated'
		}
	});

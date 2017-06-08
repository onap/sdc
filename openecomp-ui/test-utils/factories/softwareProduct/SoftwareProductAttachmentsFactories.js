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

export const VSPAttachmentsErrorFactory = new Factory()
	.attrs({
		level: 'WARNING'
	})
	.sequence('message', index => `error no. ${index}`);

export const VSPHeatFactory = new Factory()
	.attrs({
		fileName: () => `${randomstring.generate()}.yaml`,
		env: {
			fileName: () => `${randomstring.generate()}.env`
		},
		errors: Factory.buildList(VSPAttachmentsErrorFactory)
	});

export const VSPAttachmentTreeNodeFactory = new Factory()
	.attrs({
		name: 'HEAT',
		type: 'heat'
	});

export const VSPAttachmentTreeNodeWithChildrenFactory = new Factory()
	.extend(VSPAttachmentTreeNodeFactory)
	.attrs({
		expanded: true,
		children: []
	});

export const VSPAttachmentDetailedError = new Factory()
	.attrs({
		level: 'WARNING',
		errorMessage: 'Resource is not defined as output and thus cannot be Shared. resource id - network_4',
		name: () => `${randomstring.generate()}.yaml`,
		hasParent: false,
		parentName: 'HEAT',
		type: 'heat'
	});

export const HeatSetupModuleBase = new Factory()
	.option('name', 0)
	.attr('name', ['name'], (name) => {
		return name ? randomstring.generate(5) : '';
	})
	.attrs({
		isBase: false,
		yaml: () => {return 'yaml_' + randomstring.generate(5) + '.yaml';},
		env: () => {return 'env_' + randomstring.generate(5) + '.env';},
		vol: () => {return 'vol_' + randomstring.generate(5) + '.vol';},
		volEnv: () => {return 'volEnv_' + randomstring.generate(5) + '.env';}
	});

export const heatSetupManifest = new Factory()
	.attrs({
		modules: [
			{
				name: 'BASE_sdjflsjldfsd',
				isBase: true,
				yaml: 'yaml_filename9.yaml',
				env: 'env_filename8.env',
				vol: 'vol_filename5.vol',
				volEnv: 'vol_env_filename1.8.vol',

			},
			{
				name: 'MODULE_asdkjfhkajsf',
				isBase: false,
				yaml: 'yaml_filename.yaml',
				env: 'env_filename.env',
				vol: 'vol_filename.vol',
				volEnv: 'vol_env_filename.vol',

			}
		],
		unassigned: [
			'hot-nimbus-oam-volumes_v1.0.env',
			'hot-nimbus-oam_v1.0.env',
			'hot-nimbus-oam-volumes_v1.1.env',
			'hot-nimbus-oam-volumes_v2.0.env',
			'vol_filename2.vol',
			'hot-nimbus-oam-volumes_v4.0.env',
			'hot-nimbus-oam-volumes_v5.0.env',
			'hot-nimbus-oam_v6.0.env',
			'hot-nimbus-oam-volumes_v7.0.env',
			'vol_filename1.vol'
		],
		artifacts: ['hot-nimbus-oam_v3.0.env'],
		nested: ['nested-ppd_v1.1.yaml', 'nested-ppd_v1.0.yaml', 'nested-ppd_v8.0.yaml']
	});

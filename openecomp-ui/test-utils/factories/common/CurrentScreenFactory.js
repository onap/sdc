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
import VersionFactory from './VersionFactory.js';

export const InitializedItemPermissionFactory = new Factory()
	.attrs({
		'isCertified': false,
		'inMerge': false,
		'isCollaborator': true
	});

export const ItemPermissionFactory = new Factory()
	.extend(InitializedItemPermissionFactory)
	.attrs({
		'isDirty': false,
		'isOutOfSync': false,
		'isUpToDate': true
	});


export const CurrentScreenPropsFactory = new Factory()
	.option('versionId', () => randomstring.generate())
	.option('versionBaseId', () => randomstring.generate())
	.attrs({
		softwareProductId: () => randomstring.generate(),
		licenseModelId: () => randomstring.generate(),
		isReadOnlyMode: false
	})
	.attr('version', [
		'versionId', 'versionBaseId'
	], (id, baseId) =>
		VersionFactory.build({id, baseId})
	);


Factory.define('InitializedCurrentScreenFactory')
	.option('isCertified', false)
	.option('inMerge', false)
	.option('isCollaborator', true)
	.option('isReadOnlyMode', ['isCertified', 'inMerge', 'isCollaborator'], (isCertified, inMerge, isCollaborator) =>
		isCertified || inMerge || !isCollaborator
	)
	.attr('itemPermission', ['isCertified', 'inMerge', 'isCollaborator'], (isCertified, inMerge, isCollaborator) =>
		InitializedItemPermissionFactory.build({isCollaborator, isCertified, inMerge})
	)
	.attr('props', ['isReadOnlyMode'], (isReadOnlyMode) => {
		return {isReadOnlyMode};
	});
export const InitializedCurrentScreenFactory = new Factory().extend('InitializedCurrentScreenFactory');


Factory.define('CurrentScreenFactory')
	.extend('InitializedCurrentScreenFactory')
	.option('isDirty', false)
	.option('isOutOfSync', false)
	.option('isUpToDate', true)
	.option('version', ['isCertified'], (isCertified) => VersionFactory.build({isCertified}))
	.attr('itemPermission', [
		'isCertified', 'inMerge', 'isCollaborator', 'isDirty', 'isOutOfSync', 'isUpToDate'
	], (isCertified, inMerge, isCollaborator, isDirty, isOutOfSync, isUpToDate) =>
		ItemPermissionFactory.build({isCollaborator, isCertified, inMerge, isDirty, isOutOfSync, isUpToDate})
	)
	.attr('props', ['isReadOnlyMode', 'version'], (isReadOnlyMode, version) => {
		return {isReadOnlyMode, version};
	});
export default new Factory().extend('CurrentScreenFactory');


export const CurrentScreenFactoryWithProps = new Factory()
	.extend('CurrentScreenFactory')
	.option('versionId')
	.option('versionBaseId')
	.option('version')
	.attr('props', [
		'isReadOnlyMode', 'versionId', 'versionBaseId', 'version'
	], (isReadOnlyMode, id, baseId, version) => {
		let attrs = {isReadOnlyMode};
		let options = {};

		if (version !== undefined) { attrs['version'] = version; }
		if (id !== undefined) { options['versionId'] = id; }
		if (baseId !== undefined) { options['versionBaseId'] = baseId; }

		return CurrentScreenPropsFactory.build(attrs, options);
	});

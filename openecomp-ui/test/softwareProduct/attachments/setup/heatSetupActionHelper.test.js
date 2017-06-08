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

import HeatSetupActionHelper from 'sdc-app/onboarding/softwareProduct/attachments/setup/HeatSetupActionHelper.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import deepFreeze from 'deep-freeze';
import {heatSetupManifest} from 'test-utils/factories/softwareProduct/SoftwareProductAttachmentsFactories.js';
import {actionTypes as HeatSetupActions, fileTypes as HeatSetupFileTypes} from 'sdc-app/onboarding/softwareProduct/attachments/setup/HeatSetupConstants.js';

describe('Heat Setup Action Helper test', () => {

	it('function does exist', () => {
		expect(HeatSetupActionHelper).toBeTruthy();
	});

	it('manifest load test', () => {

		const store = storeCreator();

		const manifest = heatSetupManifest.build();
		store.dispatch({
			type: HeatSetupActions.MANIFEST_LOADED,
			response: manifest
		});

		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.modules.length).toBe(manifest.modules.length);
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.nested.length).toBe(manifest.nested.length);
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.unassigned.length).toBe(manifest.unassigned.length);
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.artifacts.length).toBe(manifest.artifacts.length);

	});

	it('add module action test', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const manifest = heatSetupManifest.build();
		store.dispatch({
			type: HeatSetupActions.MANIFEST_LOADED,
			response: manifest
		});
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.modules.length).toBe(manifest.modules.length);
		HeatSetupActionHelper.addModule(store.dispatch);
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.modules.length).toBe(manifest.modules.length + 1);

	});

	it('delete module action test', () => {

		const store = storeCreator();

		const manifest = heatSetupManifest.build();
		store.dispatch({
			type: HeatSetupActions.MANIFEST_LOADED,
			response: manifest
		});
		HeatSetupActionHelper.deleteModule(store.dispatch, manifest.modules[0].name);
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.modules.length).toBe(manifest.modules.length - 1);

	});

	it('rename module action test', () => {

		const store = storeCreator();

		const manifest = heatSetupManifest.build();
		store.dispatch({
			type: HeatSetupActions.MANIFEST_LOADED,
			response: manifest
		});
		const newName = 'newName';
		HeatSetupActionHelper.renameModule(store.dispatch, {oldName: manifest.modules[0].name, newName});
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.modules[0].name).toBe(newName);

	});

	it('change module type action test', () => {

		const store = storeCreator();

		const manifest = heatSetupManifest.build();
		store.dispatch({
			type: HeatSetupActions.MANIFEST_LOADED,
			response: manifest
		});
		const newValue = 'newvalue.env';
		HeatSetupActionHelper.changeModuleFileType(store.dispatch,
			{
				module: manifest.modules[0],
				value: {value: newValue},
				type: HeatSetupFileTypes.ENV.label
			});
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.modules[0].env).toBe(newValue);
	});

	it('change artifacts list action test', () => {

		const store = storeCreator();

		const manifest = heatSetupManifest.build();
		store.dispatch({
			type: HeatSetupActions.MANIFEST_LOADED,
			response: manifest
		});
		const artifacts = store.getState().softwareProduct.softwareProductAttachments.heatSetup.artifacts;
		const newArtifacts = [...artifacts,  manifest.unassigned[0]].map(str => (typeof str === 'string' ? {value: str, label: str} : str));;
		HeatSetupActionHelper.changeArtifactList(store.dispatch, newArtifacts);
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.artifacts[1]).toBe(manifest.unassigned[0]);
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.unassigned.length).toBe(manifest.unassigned.length - 1);
	});

	it('add All Unassigned Files To Artifacts action test', () => {

		const store = storeCreator();

		const manifest = heatSetupManifest.build();
		store.dispatch({
			type: HeatSetupActions.MANIFEST_LOADED,
			response: manifest
		});
		const artifacts = store.getState().softwareProduct.softwareProductAttachments.heatSetup.artifacts;
		const unassigned = store.getState().softwareProduct.softwareProductAttachments.heatSetup.unassigned;
		const newArtifacts = [...artifacts, ...unassigned];
		HeatSetupActionHelper.addAllUnassignedFilesToArtifacts(store.dispatch, true);
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.artifacts).toEqual(newArtifacts);
		expect(store.getState().softwareProduct.softwareProductAttachments.heatSetup.unassigned).toEqual([]);
	});

});

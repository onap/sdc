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
import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import {storeCreator} from 'sdc-app/AppStore.js';
import {cloneAndSet} from 'test-utils/Util.js';
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';
import LicenseModelCreationActionHelper from 'sdc-app/onboarding/licenseModel/creation/LicenseModelCreationActionHelper.js';
import {LicenseModelPostFactory, LicenseModelDispatchFactory, LicenseModelStoreFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import {default as CurrentScreenFactory} from 'test-utils/factories/common/CurrentScreenFactory.js';
import {actionsEnum as VersionControllerActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import {SyncStates} from 'sdc-app/common/merge/MergeEditorConstants.js';
import {itemTypes} from 'sdc-app/onboarding/versionsPage/VersionsPageConstants.js';

describe('License Model Module Tests', function () {
	it('Add License Model', () => {
		const store = storeCreator();
		deepFreeze(store.getState());

		const licenseModelPostRequest = LicenseModelPostFactory.build();

		const licenseModelToAdd = LicenseModelDispatchFactory.build();

		const licenseModelIdFromResponse = 'ADDED_ID';
		
		mockRest.addHandler('post', ({options, data, baseUrl}) => {
			expect(baseUrl).toEqual('/onboarding-api/v1.0/vendor-license-models/');
			expect(data).toEqual(licenseModelPostRequest);
			expect(options).toEqual(undefined);
			return {
				value: licenseModelIdFromResponse
			};
		});

		return LicenseModelCreationActionHelper.createLicenseModel(store.dispatch, {
			licenseModel: licenseModelToAdd
		}).then((response) => {
			expect(response.value).toEqual(licenseModelIdFromResponse);
		});
	});

	it('Validating readonly screen after submit', () => {
		const version = VersionFactory.build({}, {isCertified: false});
		const itemPermissionAndProps = CurrentScreenFactory.build({}, {version});
		const licenseModel = LicenseModelStoreFactory.build();
		deepFreeze(licenseModel);

		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			licenseModel: {
				licenseModelEditor: {data: licenseModel},
			}
		});
		deepFreeze(store.getState());

		const certifiedVersion = {
			...itemPermissionAndProps.props.version,
			status: 'Certified'
		};

		const expectedCurrentScreenProps = {
			itemPermission: {
				...itemPermissionAndProps.itemPermission,
				isCertified: true
			},
			props: {
				isReadOnlyMode: true,
				version: certifiedVersion
			}
		};
		const expectedSuccessModal = {
			cancelButtonText: 'OK',
			modalClassName: 'notification-modal',
			msg: 'This license model successfully submitted',
			timeout: 2000,
			title: 'Submit Succeeded',
			type: 'success'
		};

		const versionsList = {
			itemType: itemTypes.LICENSE_MODEL,
			itemId: licenseModel.id,
			versions: [{...certifiedVersion}]
		};

		let expectedStore = store.getState();
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.props', expectedCurrentScreenProps.props);
		expectedStore = cloneAndSet(expectedStore, 'modal', expectedSuccessModal);
		expectedStore = cloneAndSet(expectedStore, 'versionsPage.versionsList', versionsList );

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${licenseModel.id}/versions/${version.id}/actions`);
			expect(data).toEqual({action: VersionControllerActionsEnum.SUBMIT});
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-license-models/${licenseModel.id}/versions/${version.id}/actions`);
			expect(data).toEqual({action: VersionControllerActionsEnum.CREATE_PACKAGE});
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${licenseModel.id}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...certifiedVersion, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: false}};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${licenseModel.id}/versions`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: [{...certifiedVersion}]};
		});

		return LicenseModelActionHelper.performSubmitAction(store.dispatch, {
			licenseModelId: licenseModel.id,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});
});

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
import {cloneAndSet} from 'test-utils/Util.js';
import {storeCreator} from 'sdc-app/AppStore.js';

import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import {VSPEditorFactoryWithLicensingData} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';
import {default as CurrentScreenFactory} from 'test-utils/factories/common/CurrentScreenFactory.js';
import {actionsEnum as VersionControllerActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import {SyncStates} from 'sdc-app/common/merge/MergeEditorConstants.js';
import {itemTypes} from 'sdc-app/onboarding/versionsPage/VersionsPageConstants.js';

describe('Software Product Module Tests', function () {
	it('Validating readonly screen after submit', () => {
		const version = VersionFactory.build({}, {isCertified: false});
		const itemPermissionAndProps = CurrentScreenFactory.build({}, {version});
		const softwareProduct = VSPEditorFactoryWithLicensingData.build();
		deepFreeze(softwareProduct);

		const store = storeCreator({
			currentScreen: {...itemPermissionAndProps},
			softwareProduct: {
				softwareProductEditor: {data: softwareProduct},
				softwareProductQuestionnaire: {qdata: 'test', qschema: {type: 'string'}}
			}
		});
		deepFreeze(store.getState());

		const certifiedVersion = {
			...itemPermissionAndProps.props.version,
			status: 'Certified'
		};

		const versionsList = {
			itemType: itemTypes.SOFTWARE_PRODUCT,
			itemId: softwareProduct.id,
			versions: [{...certifiedVersion}]
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
			msg: 'This software product successfully submitted',
			timeout: 2000,
			title: 'Submit Succeeded',
			type: 'success'
		};

		let expectedStore = store.getState();
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.itemPermission', expectedCurrentScreenProps.itemPermission);
		expectedStore = cloneAndSet(expectedStore, 'currentScreen.props', expectedCurrentScreenProps.props);
		expectedStore = cloneAndSet(expectedStore, 'modal', expectedSuccessModal);
		expectedStore = cloneAndSet(expectedStore, 'versionsPage.versionsList', versionsList );

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProduct.id}/versions/${version.id}/actions`);
			expect(data).toEqual({action: VersionControllerActionsEnum.SUBMIT});
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('put', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/vendor-software-products/${softwareProduct.id}/versions/${version.id}/actions`);
			expect(data).toEqual({action: VersionControllerActionsEnum.CREATE_PACKAGE});
			expect(options).toEqual(undefined);
			return {returnCode: 'OK'};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${softwareProduct.id}/versions/${version.id}`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {...certifiedVersion, state: {synchronizationState: SyncStates.UP_TO_DATE, dirty: false}};
		});

		mockRest.addHandler('fetch', ({data, options, baseUrl}) => {
			expect(baseUrl).toEqual(`/onboarding-api/v1.0/items/${softwareProduct.id}/versions`);
			expect(data).toEqual(undefined);
			expect(options).toEqual(undefined);
			return {results: [{...certifiedVersion}]};
		});

		return SoftwareProductActionHelper.performSubmitAction(store.dispatch, {
			softwareProductId: softwareProduct.id,
			version
		}).then(() => {
			expect(store.getState()).toEqual(expectedStore);
		});
	});
});
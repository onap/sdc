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
import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {actionTypes} from './OnboardingCatalogConstants.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import OnboardActionHelper from '../OnboardActionHelper.js';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';


function getMessageForMigration(name) {
	return (
		<div>
			<div>{i18n('{name} needs to be updated. Click ‘Checkout & Update’, to proceed.', {name: name})}</div>
			<div>{i18n('Please don’t forget to submit afterwards')}</div>
		</div>
	);
}

const OnboardingCatalogActionHelper = {
	changeVspOverlay(dispatch, vendor) {
		dispatch({
			type: actionTypes.CHANGE_VSP_OVERLAY,
			vendorId: vendor ? vendor.id : null
		});
	},
	closeVspOverlay(dispatch) {
		dispatch({
			type: actionTypes.CLOSE_VSP_OVERLAY
		});
	},
	changeActiveTab(dispatch, activeTab) {
		OnboardActionHelper.clearSearchValue(dispatch);
		dispatch({
			type: actionTypes.CHANGE_ACTIVE_CATALOG_TAB,
			activeTab
		});
	},
	onVendorSelect(dispatch, {vendor}) {
		OnboardActionHelper.clearSearchValue(dispatch);
		dispatch({
			type: actionTypes.ONBOARDING_CATALOG_OPEN_VENDOR_PAGE,
			selectedVendor: vendor
		});
	},
	onMigrate(dispatch, softwareProduct) {
		const {name, lockingUser} = softwareProduct;
		if (NaN === NaN) { // TODO
			dispatch({
				type: modalActionTypes.GLOBAL_MODAL_WARNING,
				data: {
					title: 'WARNING',
					msg: i18n('{name} is locked by user {lockingUser} for self-healing', {name: name, lockingUser: lockingUser})
				}
			});
		} else {
			dispatch({
				type: modalActionTypes.GLOBAL_MODAL_WARNING,
				data:{
					title: 'WARNING',
					msg: getMessageForMigration(softwareProduct.name.toUpperCase()),
					confirmationButtonText: i18n('Checkout & Update'),
					onConfirmed: ()=>SoftwareProductActionHelper.migrateSoftwareProduct(dispatch, {softwareProduct})
				}
			});
		}
	}
};

export default OnboardingCatalogActionHelper;

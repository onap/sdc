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
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';

import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import {actionTypes as modalActionTypes, modalSizes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {actionTypes} from './SoftwareProductCreationConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

function baseUrl() {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/`;
}

function createSoftwareProduct(softwareProduct) {
	return RestAPIUtil.post(baseUrl(), {
		...softwareProduct,
		icon: 'icon',
		licensingData: {}
	});
}

const SoftwareProductCreationActionHelper = {

	open(dispatch, vendorId) {
		SoftwareProductActionHelper.loadSoftwareProductAssociatedData(dispatch);
		dispatch({
			type: actionTypes.OPEN,
			selectedVendorId: vendorId
		});

		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_SHOW,
			data: {
				modalComponentName: modalContentMapper.SOFTWARE_PRODUCT_CREATION,
				title: i18n('New Software Product'),
				modalComponentProps: {
					vendorId,
					size: modalSizes.LARGE
				}
			}
		});

	},

	resetData(dispatch) {

		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_CLOSE
		});

		dispatch({
			type: actionTypes.RESET_DATA
		});
	},

	createSoftwareProduct(dispatch, {softwareProduct}) {
		return createSoftwareProduct(softwareProduct).then(result => {
			dispatch({
				type: actionTypes.SOFTWARE_PRODUCT_CREATED,
				result
			});
			return result;
		});
	}

};

export default SoftwareProductCreationActionHelper;

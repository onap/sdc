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
import {actionTypes} from './LicenseModelCreationConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

function baseUrl() {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-license-models/`;
}

function createLicenseModel(licenseModel) {
	return RestAPIUtil.post(baseUrl(), {
		vendorName: licenseModel.vendorName,
		description: licenseModel.description,
		iconRef: 'icon'
	});
}


export default {

	open(dispatch) {
		dispatch({
			type: actionTypes.OPEN
		});

		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_SHOW,
			data: {
				modalComponentName: modalContentMapper.LICENSE_MODEL_CREATION,
				title: i18n('New License Model')
			}
		});
	},

	close(dispatch){
		dispatch({
			type: actionTypes.CLOSE
		});

		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_CLOSE
		});
	},

	createLicenseModel(dispatch, {licenseModel}){
		return createLicenseModel(licenseModel).then(result => {
			dispatch({
				type: actionTypes.LICENSE_MODEL_CREATED,
				result
			});
			return result;
		});
	}

};

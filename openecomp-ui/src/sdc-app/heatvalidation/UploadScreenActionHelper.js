/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import NotificationConstants from 'nfvo-components/notifications/NotificationConstants.js';
import {actionTypes} from './UploadScreenConstants.js';
import {actionTypes as softwareProductsActionTypes} from '../onboarding/softwareProduct/SoftwareProductConstants.js';

function uploadFile(formData) {
	return RestAPIUtil.create('/sdc1/feProxy/onboarding-api/v1.0/validation/HEAT/validate', formData);
}

const UploadScreenActionHelper = {
	uploadFile(dispatch, formData) {


		Promise.resolve()
			.then(() => uploadFile(formData))
			.then(response => {
				dispatch({
					type: softwareProductsActionTypes.SOFTWARE_PRODUCT_LOADED,
					response
				});

				dispatch({
					type: actionTypes.OPEN_UPLOAD_SCREEN
				});
			})
			.catch(error => {
				dispatch({
					type: NotificationConstants.NOTIFY_ERROR,
					data: {title: 'File Upload Failed', msg: error.responseJSON.message}
				});
			});
	},
	openMainScreen(dispatch) {
		dispatch({
			type: actionTypes.OPEN_MAIN_SCREEN
		});
	}
};

export default UploadScreenActionHelper;

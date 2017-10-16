/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
import RestApi from 'restful-js';
import Configuration from 'sdc-app/config/Configuration.js';
import {actionTypes as modalActionTypes, modalSizes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import SoftwareProductActionHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductActionHelper.js';
import {actionTypes} from './VNFImportConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {apiResponseKeys} from './VNFImportConstants.js';

function baseUrl() {
	const marketPlaceUrl = Configuration.get('marketPlaceUrl');
	return `${marketPlaceUrl}/openoapi/vnfsdk-marketplace/v1/PackageResource/csars`;
}

function getVNFMarketplace(dispatch) {

	return RestApi.fetch(baseUrl())
		.then(response => dispatch({
			type: actionTypes.OPEN,
			response
		}))
		.catch((error) => {
			let errMessage = error.responseJSON ? error.responseJSON.message : i18n('VNF import failed msg');
			
			dispatch({					
				type: modalActionTypes.GLOBAL_MODAL_ERROR,
				data: {
					title: i18n('VNF import failed title'),
					msg: errMessage,
					cancelButtonText: i18n('Ok')
				}
			});
		});
}

function downloadCSARFile(csarId) {
	let url = `${baseUrl()}/${csarId}/files`;

	var xhr = new XMLHttpRequest();

	xhr.onreadystatechange = function() {
		if(xhr.readyState === 4) {	//The operation is complete. (0: UNSENT, 1: OPENED, 2: HEADERS_RECEIVED, 3: LOADING, 4: DONE)
			if(xhr.status === 200) {
				window.location.href = url;
			}
			else {
				// Todo : show Error dialog
				console.error('Error in downloading file');
			}
		}
	};

	xhr.open('head',url);
	xhr.send(null);
}

function uploadVNFData(selectedVendor, csarId, dispatch) {
	
	let url = `${baseUrl()}/${csarId}/files`;

	const options = {
		method: 'GET',
		headers:{'Content-Type': 'text/plain; charset=utf-8'}
	};

	RestApi.fetch(url, options)
		.then(response => {
			var blob = new Blob([response], { type: 'application/octet-stream'});
			let formData = new FormData();
			let fileName = selectedVendor.networkPackageName + '.' + selectedVendor.onboardingOrigin;
			formData.append('upload', blob, fileName);
			let softwareProductId = selectedVendor.id;
			let version = selectedVendor.version;

			SoftwareProductActionHelper.uploadFile(dispatch, {
				softwareProductId,
				formData,
				failedNotificationTitle: i18n('Upload validation failed'),
				version
			});
		})
		.catch((error) => {

			if(error.status === apiResponseKeys.SUCCESS) {
				var blob = new Blob([error.responseText]);
				let formData = new FormData();
				let fileName = 'MyNewCSAR.csar';//selectedVendor.networkPackageName + '.' + selectedVendor.onboardingOrigin;
				formData.append('upload', blob, fileName);

				let softwareProductId = selectedVendor.id;
				let version = selectedVendor.version;

				SoftwareProductActionHelper.uploadFile(dispatch, {
					softwareProductId,
					formData,
					failedNotificationTitle: i18n('Upload validation failed'),
					version
				});
			}
			else {
				let errMessage = error.responseJSON ? error.responseJSON.message : i18n('VNF import failed msg') + ' status code: ' + error.status;
			
				dispatch({					
					type: modalActionTypes.GLOBAL_MODAL_ERROR,
					data: {
						title: i18n('VNF import failed title'),
						msg: errMessage,
						cancelButtonText: i18n('Ok')
					}
				});
			}
		});
}


const VNFImportActionHelper = {

	open(dispatch, currentSoftwareProduct) {

		getVNFMarketplace(dispatch);

		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_SHOW,
			data: {
				modalComponentName: modalContentMapper.VNF_IMPORT,
				title: i18n('Browse VNF'),				
				modalComponentProps: {
					currentSoftwareProduct,
					size: modalSizes.LARGE					
				}
			}
		});

	},

	download(id) {
		downloadCSARFile(id);
	},

	resetData(dispatch) {

		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_CLOSE
		});

		dispatch({
			type: actionTypes.RESET_DATA
		});
	},

	getVNFMarketplace(dispatch) {
		return getVNFMarketplace(dispatch);
	},

	uploadData(selectedVendor, csarId, dispatch) {
		this.resetData(dispatch);
		uploadVNFData(selectedVendor, csarId, dispatch);
	}


};

export default VNFImportActionHelper;

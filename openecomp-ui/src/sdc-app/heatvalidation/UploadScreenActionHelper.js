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
import i18n from 'nfvo-utils/i18n/i18n.js';
import isEqual from 'lodash/isEqual.js';
import cloneDeep from 'lodash/cloneDeep.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {actionTypes as softwareProductsActionTypes} from '../onboarding/softwareProduct/SoftwareProductConstants.js';
import {actionTypes as HeatSetupActions} from '../onboarding/softwareProduct/attachments/setup/HeatSetupConstants.js';



const options = {
	headers: {
		USER_ID: 'validationOnlyVspUser'
	}
};


function getTimestampString() {
	let date = new Date();
	let z = n => n < 10 ? '0' + n : n;
	return `${date.getFullYear()}-${z(date.getMonth())}-${z(date.getDate())}_${z(date.getHours())}-${z(date.getMinutes())}`;
}

function fetchVspIdAndVersion() {

	let vspId = sessionStorage.getItem('validationAppVspId');
	let versionId = sessionStorage.getItem('validationAppVersionId');
	if (vspId) {
		return  Promise.resolve({value: vspId, versionId});
	}else {
		return RestAPIUtil.fetch('/sdc1/feProxy/onboarding-api/v1.0/vendor-software-products/validation-vsp', options)
			.then(response => {
				sessionStorage.setItem('validationAppVspId', response.itemId);
				sessionStorage.setItem('validationAppVersionId', response.version.id);
				return Promise.resolve({value: response.itemId, versionId: response.version.id});
			});
	}

}


function showFileSaveDialog({blob, xhr, defaultFilename, addTimestamp}) {
	let filename;
	let contentDisposition = xhr.getResponseHeader('content-disposition');
	let match = contentDisposition ? contentDisposition.match(/filename=(.*?)(;|$)/) : false;
	if (match) {
		filename = match[1];
	} else {
		filename = defaultFilename;
	}

	if (addTimestamp) {
		filename = filename.replace(/(^.*?)\.([^.]+$)/, `$1_${getTimestampString()}.$2`);
	}

	let link = document.createElement('a');
	let url = URL.createObjectURL(blob);
	link.href = url;
	link.download = filename;
	link.style.display = 'none';
	document.body.appendChild(link);
	link.click();
	setTimeout(function(){
		document.body.removeChild(link);
		URL.revokeObjectURL(url);
	}, 0);
}


function uploadFile(formData) {
	return fetchVspIdAndVersion()
		.then(response => {
			return RestAPIUtil.post(`/sdc1/feProxy/onboarding-api/v1.0/vendor-software-products/${response.value}/versions/${response.versionId}/orchestration-template-candidate`, formData, options);
		});
}

function loadSoftwareProductHeatCandidate(dispatch){
	return fetchVspIdAndVersion()
		.then(response => {
			return RestAPIUtil.fetch(`/sdc1/feProxy/onboarding-api/v1.0/vendor-software-products/${response.value}/versions/${response.versionId}/orchestration-template-candidate/manifest`, options)
				.then(response => dispatch({
					type: HeatSetupActions.MANIFEST_LOADED,
					response
				}));
		});
}

function updateHeatCandidate(dispatch, heatCandidate) {
	return fetchVspIdAndVersion()
		.then(response => {
			return RestAPIUtil.put(`/sdc1/feProxy/onboarding-api/v1.0/vendor-software-products/${response.value}/versions/${response.versionId}/orchestration-template-candidate/manifest`,
				heatCandidate.heatData, options)
				.then(null, error => {
					dispatch({
						type: modalActionTypes.GLOBAL_MODAL_ERROR,
						data: {
							title: i18n('Save Failed'), 
							modalComponentName: modalContentMapper.SUMBIT_ERROR_RESPONSE,							
							modalComponentProps: {
								validationResponse: error.responseJSON
							},					
							cancelButtonText: i18n('Ok')
						}
					});
					return Promise.reject(error);
				});
		});
}

function fetchSoftwareProduct() {
	return fetchVspIdAndVersion()
		.then(response => {
			return RestAPIUtil.fetch(`/sdc1/feProxy/onboarding-api/v1.0/vendor-software-products/${response.value}/versions/${response.versionId}`, options);
		});
}

function downloadHeatFile() {
	return fetchVspIdAndVersion()
		.then(response => {
			RestAPIUtil.fetch(`/sdc1/feProxy/onboarding-api/v1.0/vendor-software-products/${response.value}/versions/${response.versionId}/orchestration-template-candidate`, {
				...options,
				dataType: 'binary'
			})
				.done((blob, statusText, xhr) => showFileSaveDialog({
					blob,
					xhr,
					defaultFilename: 'HEAT_file.zip',
					addTimestamp: true
				}));
		});
}

function processAndValidateHeatCandidate(dispatch) {
	return fetchVspIdAndVersion()
		.then(response => {
			return RestAPIUtil.put(`/sdc1/feProxy/onboarding-api/v1.0/vendor-software-products/${response.value}/versions/${response.versionId}/orchestration-template-candidate/process`, {}, options)
				.then(response => {
					if (response.status === 'Success') {
						fetchSoftwareProduct().then(response => {
							dispatch({
								type: softwareProductsActionTypes.SOFTWARE_PRODUCT_LOADED,
								response
							});
						});
					}
				});
		});
}

const UploadScreenActionHelper = {
	uploadFile(dispatch, formData) {

		return Promise.resolve()
			.then(() => uploadFile(formData))
			.then(response => {
				dispatch({
					type: softwareProductsActionTypes.SOFTWARE_PRODUCT_LOADED,
					response
				});
				dispatch({
					type: HeatSetupActions.FILL_HEAT_SETUP_CACHE,
					payload:{}
				});
				loadSoftwareProductHeatCandidate(dispatch);
			})
			.catch(error => {
				dispatch({					
					type: modalActionTypes.GLOBAL_MODAL_ERROR,
					data: {
						title: i18n('File Upload Failed'), 													
						msg: error.responseJSON.message,					
						cancelButtonText: i18n('Ok')
					}
				});
			});
	},

	processAndValidateHeat(dispatch, heatData, heatDataCache){
		return isEqual(heatData, heatDataCache) ? Promise.resolve() :
			updateHeatCandidate(dispatch, heatData)
				.then(() => processAndValidateHeatCandidate(dispatch))
				.then(() => dispatch({type: HeatSetupActions.FILL_HEAT_SETUP_CACHE, payload: cloneDeep(heatData)}));
	},

	downloadHeatFile(){
		return downloadHeatFile();
	},
};

export default UploadScreenActionHelper;

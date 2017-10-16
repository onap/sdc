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

function baseUrl() {
	const marketPlaceUrl = Configuration.get('marketPlaceUrl');
	return `http://${marketPlaceUrl}/openoapi/vnfsdk-marketplace/v1/PackageResource/csars`;
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
	return RestApi.fetch(url, {dataType: 'binary'});
}

function getFileName(xhr, defaultFilename) {
	let filename = '';
	let contentDisposition = xhr.getResponseHeader('Content-Disposition') ? xhr.getResponseHeader('Content-Disposition') : '';
	let match = contentDisposition.match(/filename=(.*?)(;|$)/);
	if (match) {
		filename = match[1].replace(/['"]/g, '');;
	}
	else {
		filename = defaultFilename;
	}
	return filename;
}

function uploadVNFData({blob, xhr}, selectedVendor, dispatch) {
	let formData = new FormData();
	let fileName = getFileName(xhr, 'MyNewCSAR.csar');
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

function getTimestampString() {
	let date = new Date();
	let z = n => n < 10 ? '0' + n : n;
	return `${date.getFullYear()}-${z(date.getMonth())}-${z(date.getDate())}_${z(date.getHours())}-${z(date.getMinutes())}`;
}

function showFileSaveDialog({blob, xhr, defaultFilename, addTimestamp}) {
	let filename = getFileName(xhr, defaultFilename);

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

	download(csarId) {
		downloadCSARFile(csarId)
			.then((blob, statusText, xhr) => showFileSaveDialog({blob, xhr, defaultFilename: 'MyNewCSAR.csar', addTimestamp: true}));
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
		downloadCSARFile(csarId)
			.then((blob, statusText, xhr) => uploadVNFData({blob, xhr}, selectedVendor, dispatch));
	}


};

export default VNFImportActionHelper;

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
 
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import {actionTypes as modalActionTypes, modalSizes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {actionTypes} from './VNFImportConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
// import $ from 'jquery';

function baseUrl() {
	const marketPlaceUrl = Configuration.get('marketPlaceUrl');
	return `${marketPlaceUrl}/openoapi/vnfsdk-marketplace/v1/PackageResource/csars`;
}

function getVNFMarketplace(dispatch) {
	
	const options = {
		method: 'GET',
		headers:{'content-type': 'application/json', HTTP_CSP_ATTUID: 'validationOnlyVspUser'}
	};
	

	console.log('Calling API http://192.168.4.42:8072/openoapi/vnfsdk-marketplace/v1/PackageResource/csars');
	/*$.ajax({ 
		type: 'GET',
		dataType: 'json',
		url: 'http://192.168.4.42:8072/openoapi/vnfsdk-marketplace/v1/PackageResource/csars',
		
		success: function(response) {
			//this.setState({entries: data});
			console.log(response);
			dispatch({type: actionTypes.OPEN, response});
		},
		error: function(status, err) {
			//console.error(this.props.url, status, err.toString());
			console.log('fail', status, err);
		}

	});*/

	return RestAPIUtil.fetch(baseUrl(), options)
		.then(response => dispatch({
			type: actionTypes.OPEN,
			response
		}));
}

const VNFImportActionHelper = {

	open(dispatch) {

		getVNFMarketplace(dispatch);

		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_SHOW,
			data: {
				modalComponentName: modalContentMapper.VNF_IMPORT,
				title: i18n('Browse VNF'),				
				modalComponentProps: {
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

	getVNFMarketplace(dispatch) {
		return getVNFMarketplace(dispatch);
	}

};

export default VNFImportActionHelper;

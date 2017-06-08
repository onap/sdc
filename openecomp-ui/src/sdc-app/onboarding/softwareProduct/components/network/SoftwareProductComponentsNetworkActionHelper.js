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

import {actionTypes} from './SoftwareProductComponentsNetworkConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {NIC_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNetworkConstants.js';

function baseUrl(softwareProductId, version, componentId) {
	const versionId = version.id;
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/components/${componentId}/nics`;
}


function fetchNICQuestionnaire({softwareProductId, version, componentId, nicId}) {
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version, componentId)}/${nicId}/questionnaire`);
}

function fetchNIC({softwareProductId, version, componentId, nicId}) {
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version, componentId)}/${nicId}`);
}

function fetchNICsList({softwareProductId, version, componentId}) {
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version, componentId)}`);
}

function saveNIC({softwareProductId, version, componentId, nic: {id, name, description, networkId}}) {
	return RestAPIUtil.put(`${baseUrl(softwareProductId, version, componentId)}/${id}`,{
		name,
		description,
		networkId
	});
}

function saveNICQuestionnaire({softwareProductId, version, componentId, nicId, qdata}) {
	return RestAPIUtil.put(`${baseUrl(softwareProductId, version, componentId)}/${nicId}/questionnaire`, qdata);
}

const SoftwareProductComponentNetworkActionHelper = {

	fetchNICsList(dispatch, {softwareProductId, version, componentId}) {
		return fetchNICsList({softwareProductId, version, componentId}).then((response) => {
			dispatch({
				type: actionTypes.NIC_LIST_UPDATE,
				response: response.results
			});
		});
	},

	openNICEditor(dispatch, {nic = {}, data = {}}) {
		dispatch({
			type: actionTypes.NICEditor.OPEN,
			nic: {...data, id: nic.id}
		});
	},

	closeNICEditor(dispatch) {
		dispatch({
			type: actionTypes.NICEditor.CLOSE
		});
	},

	loadNICData({softwareProductId, version, componentId, nicId}) {
		return fetchNIC({softwareProductId, version, componentId, nicId});
	},

	loadNICQuestionnaire(dispatch, {softwareProductId, version, componentId, nicId}) {
		return fetchNICQuestionnaire({softwareProductId, version, componentId, nicId}).then((response) => {
			ValidationHelper.qDataLoaded(dispatch, {qName: NIC_QUESTIONNAIRE ,response: {
				qdata: response.data ? JSON.parse(response.data) : {},
				qschema: JSON.parse(response.schema)
			}});
		});
	},

	saveNICDataAndQuestionnaire(dispatch, {softwareProductId, version, componentId, data, qdata}) {
		SoftwareProductComponentNetworkActionHelper.closeNICEditor(dispatch);
		return Promise.all([
			saveNICQuestionnaire({softwareProductId, version, componentId, nicId: data.id, qdata}),
			saveNIC({softwareProductId, version, componentId, nic: data}).then(() => {
				dispatch({
					type: actionTypes.NIC_LIST_EDIT,
					nic: data
				});
			})
		]);
	}
};

export default SoftwareProductComponentNetworkActionHelper;

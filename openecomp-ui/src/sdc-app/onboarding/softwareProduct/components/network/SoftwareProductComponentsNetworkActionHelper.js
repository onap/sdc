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
import Configuration from 'sdc-app/config/Configuration.js';

import {actionTypes} from './SoftwareProductComponentsNetworkConstants.js';

function baseUrl(softwareProductId, componentId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/nics`;
}


function fetchNICQuestionnaire({softwareProductId, componentId, nicId, version}) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, componentId)}/${nicId}/questionnaire${versionQuery}`);
}

function fetchNIC({softwareProductId, componentId, nicId, version}) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, componentId)}/${nicId}${versionQuery}`);
}

function fetchNICsList({softwareProductId, componentId, version}) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, componentId)}${versionQuery}`);
}

function saveNIC({softwareProductId, componentId, nic: {id, name, description, networkId}}) {
	return RestAPIUtil.save(`${baseUrl(softwareProductId, componentId)}/${id}`,{
		name,
		description,
		networkId
	});
}

function saveNICQuestionnaire({softwareProductId, componentId, nicId, qdata}) {
	return RestAPIUtil.save(`${baseUrl(softwareProductId, componentId)}/${nicId}/questionnaire`, qdata);
}

const SoftwareProductComponentNetworkActionHelper = {

	fetchNICsList(dispatch, {softwareProductId, componentId, version}) {
		return fetchNICsList({softwareProductId, componentId, version}).then((response) => {
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

	loadNICData({softwareProductId, componentId, nicId, version}) {
		return fetchNIC({softwareProductId, componentId, nicId, version});
	},

	loadNICQuestionnaire(dispatch, {softwareProductId, componentId, nicId, version}) {
		return fetchNICQuestionnaire({softwareProductId, componentId, nicId, version}).then((response) => {
			dispatch({
				type: actionTypes.NICEditor.NIC_QUESTIONNAIRE_UPDATE,
				payload: {
					qdata: response.data ? JSON.parse(response.data) : {},
					qschema: JSON.parse(response.schema)
				}
			});
		});
	},

	updateNICData(dispatch, {deltaData}) {
		dispatch({
			type: actionTypes.NICEditor.DATA_CHANGED,
			deltaData
		});
	},

	updateNICQuestionnaire(dispatch, {data}) {
		dispatch({
			type: actionTypes.NICEditor.NIC_QUESTIONNAIRE_UPDATE,
			payload: {
				qdata: data
			}
		});
	},

	saveNICDataAndQuestionnaire(dispatch, {softwareProductId, componentId, data, qdata}) {
		SoftwareProductComponentNetworkActionHelper.closeNICEditor(dispatch);
		return Promise.all([
			saveNICQuestionnaire({softwareProductId, componentId, nicId: data.id, qdata}),
			saveNIC({softwareProductId, componentId, nic: data}).then(() => {
				dispatch({
					type: actionTypes.NIC_LIST_EDIT,
					nic: data
				});
			})
		]);
	}
};

export default SoftwareProductComponentNetworkActionHelper;

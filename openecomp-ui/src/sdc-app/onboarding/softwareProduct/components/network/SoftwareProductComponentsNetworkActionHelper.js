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
import i18n from 'nfvo-utils/i18n/i18n.js';

import {actionTypes} from './SoftwareProductComponentsNetworkConstants.js';
import {actionTypes as GlobalModalActions} from 'nfvo-components/modal/GlobalModalConstants.js';
import {modalContentMapper as modalPagesMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {NIC_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNetworkConstants.js';

function baseUrl(softwareProductId, version, componentId) {
	const versionId = version.id;
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/components/${componentId}/nics`;
}

function createNIC({nic, vspId, componentId, version}) {
	return RestAPIUtil.post(baseUrl(vspId, version, componentId), {
		name: nic.name,
		description: nic.description,
		networkDescription: nic.networkDescription,
		networkType: nic.networkType
	});
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

function deleteNIC({softwareProductId, componentId, nicId, version}) {
	return RestAPIUtil.destroy(`${baseUrl(softwareProductId, version, componentId)}/${nicId}`);
}
function saveNIC({softwareProductId, version, componentId, nic: {id, name, description, networkId, networkType, networkDescription}}) {
	return RestAPIUtil.put(`${baseUrl(softwareProductId, version, componentId)}/${id}`,{
		name,
		description,
		networkId,
		networkDescription,
		networkType
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

	openNICEditor(dispatch, {nic = {}, data = {}, softwareProductId, componentId, isReadOnlyMode, modalClassName, version}) {
		dispatch({
			type: actionTypes.NICEditor.FILL_DATA,
			nic: {...data, id: nic.id}
		});
		dispatch({
			type: GlobalModalActions.GLOBAL_MODAL_SHOW,
			data: {
				modalClassName,
				modalComponentProps: {softwareProductId, componentId, isReadOnlyMode, version},
				modalComponentName: modalPagesMapper.NIC_EDITOR,
				title: i18n('Edit NIC')
			}
		});
	},

	closeNICEditor(dispatch) {
		dispatch({
			type: GlobalModalActions.GLOBAL_MODAL_CLOSE
		});
		dispatch({
			type: actionTypes.NICEditor.CLEAR_DATA
		});
	},

	createNIC(dispatch, {nic, softwareProductId, componentId, version}){
		return createNIC({nic, vspId: softwareProductId, componentId, version}).then(() => {
			return SoftwareProductComponentNetworkActionHelper.fetchNICsList(dispatch, {softwareProductId, componentId, version});
		});
	},
	loadNICData({softwareProductId, version, componentId, nicId}) {
		return fetchNIC({softwareProductId, version, componentId, nicId});
	},

	deleteNIC(dispatch, {softwareProductId, componentId, nicId, version}) {
		return deleteNIC({softwareProductId, componentId, nicId, version}).then(() => {
			return SoftwareProductComponentNetworkActionHelper.fetchNICsList(dispatch, {softwareProductId, componentId, version});
		});
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

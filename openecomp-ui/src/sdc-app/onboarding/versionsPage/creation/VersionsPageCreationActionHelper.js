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
import {actionTypes} from './VersionsPageCreationConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';
import {actionTypes as VersionsPageActionTypes} from '../VersionsPageConstants.js';

function baseUrl({itemId, baseVersion}) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/items/${itemId}/versions/${baseVersion.id}/`;
}

function createVersion({itemId, baseVersion, payload: {description, creationMethod} }) {
	return RestAPIUtil.post(baseUrl({itemId, baseVersion}), {description, creationMethod});
}


export default {

	open(dispatch, {itemType, itemId, additionalProps, baseVersion}) {
		dispatch({
			type: actionTypes.OPEN
		});

		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_SHOW,
			data: {
				modalComponentName: modalContentMapper.VERSION_CREATION,
				modalComponentProps: {itemType, itemId, additionalProps, baseVersion},
				title: i18n('New Version - From {name}', {name: baseVersion.name})
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

	createVersion(dispatch, {itemId, baseVersion, payload}){
		return createVersion({itemId, baseVersion, payload}).then(result => {
			return ItemsHelper.fetchVersions({itemId}).then(response => {
				dispatch({
					type: VersionsPageActionTypes.VERSIONS_LOADED,
					versions: response.results,
					itemId
				});
				dispatch({
					type: actionTypes.VERSION_CREATED,
					result
				});
				return result;
			});
		});
	}

};

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

import i18n from 'nfvo-utils/i18n/i18n.js';
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {actionsEnum as vcActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';

import Configuration from 'sdc-app/config/Configuration.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import {enums, screenTypes} from 'sdc-app/onboarding/OnboardingConstants.js';

import {actionTypes} from './RevisionsConstants.js';

function baseUrl(itemId, version) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/items/${itemId}/versions/${version.id}`;
}

function fetchRevisions(itemId, version){
	let fetchUrl = `${baseUrl(itemId, version)}/revisions`;
	return RestAPIUtil.fetch(fetchUrl);
}

function revertToRevision(itemId, version, revisionId) {
	let putUrl = `${baseUrl(itemId, version)}/actions`;
	let requestBody = {
		action: vcActionsEnum.REVERT,
		revisionRequest: {
			revisionId: revisionId
		}
	};
	return RestAPIUtil.put(putUrl, requestBody);
}

const RevisionaActionHelper = {
	openRevisionsView(dispatch, {itemId, version, itemType}) {
		this.fetchRevisions(dispatch, {itemId, version}).then(() => {
			dispatch({
				type: modalActionTypes.GLOBAL_MODAL_SHOW,
				data: {
					modalComponentName: modalContentMapper.REVISIONS_LIST,
					modalClassName: 'manage-revisions-modal',
					title: i18n('Revert'),
					modalComponentProps: {
						itemId: itemId,
						version: version,
						itemType
					}
				}
			});
		});
	},

	closeRevisionsView(dispatch) {
		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_CLOSE
		});
	},


	fetchRevisions(dispatch, {itemId, version}) {
		return fetchRevisions(itemId, version).then((response) => {
			dispatch({
				type: actionTypes.ITEM_REVISIONS_LOADED,
				response: response
			});
		});
	},

	revertToRevision(dispatch, {itemId, version,  revisionId, itemType}) {
		return revertToRevision(itemId, version, revisionId).then(() => {
			this.closeRevisionsView(dispatch);
			if (itemType === screenTypes.LICENSE_MODEL) {
				ScreensHelper.loadScreen(dispatch, {screen: enums.SCREEN.LICENSE_MODEL_OVERVIEW, screenType: screenTypes.LICENSE_MODEL,
					props: {licenseModelId: itemId, version}});
			} else {
				ScreensHelper.loadScreen(dispatch, {screen: enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE, screenType: screenTypes.SOFTWARE_PRODUCT,
					props: {softwareProductId: itemId, version}});
			}
		});

	}
};

export default RevisionaActionHelper;

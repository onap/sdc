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
import ItemsHelper from '../../common/helpers/ItemsHelper.js';
import {actionTypes} from './VersionsPageConstants.js';
import {itemTypes} from './VersionsPageConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import {enums, screenTypes} from 'sdc-app/onboarding/OnboardingConstants.js';


const VersionsPageActionHelper = {
	fetchVersions(dispatch, {itemType, itemId}) {
		return ItemsHelper.fetchVersions({itemId}).then(response => {
			dispatch({
				type: actionTypes.VERSIONS_LOADED,
				versions: response.results,
				itemType,
				itemId
			});
		});
	},

	selectVersion(dispatch, {version}) {
		dispatch({
			type: actionTypes.SELECT_VERSION,
			versionId: version.id
		});
	},

	selectNone(dispatch) {
		dispatch({ type: actionTypes.SELECT_NONE });
	},

	onNavigateToVersion(dispatch, {version, itemId, itemType}) {
		switch (itemType) {
			case itemTypes.LICENSE_MODEL:
				ScreensHelper.loadScreen(dispatch, {
					screen: enums.SCREEN.LICENSE_MODEL_OVERVIEW, screenType: screenTypes.LICENSE_MODEL,
					props: {licenseModelId: itemId, version}
				});
				break;
			case itemTypes.SOFTWARE_PRODUCT:
				ScreensHelper.loadScreen(dispatch, {
					screen: enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE, screenType: screenTypes.SOFTWARE_PRODUCT,
					props: {softwareProductId: itemId, version}
				});
				break;
		}
	},

	openTree(dispatch, treeProps) {
		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_SHOW,
			data: {
				modalComponentName: modalContentMapper.VERSION_TREE,
				modalComponentProps: treeProps,
				onDeclined: () => dispatch({
					type: modalActionTypes.GLOBAL_MODAL_CLOSE
				}),
				modalClassName: 'versions-tree-modal',
				cancelButtonText: i18n('Close'),
				title: i18n('Version Tree')
			}
		});
	},

	selectVersionFromModal(dispatch, {version}) {
		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_CLOSE
		});
		this.selectVersion(dispatch, {version});
	}
};

export default VersionsPageActionHelper;

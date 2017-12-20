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
import {actionTypes} from './LicenseModelConstants.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {actionsEnum as vcActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import LicenseAgreementActionHelper from './licenseAgreement/LicenseAgreementActionHelper.js';
import FeatureGroupsActionHelper from './featureGroups/FeatureGroupsActionHelper.js';
import EntitlementPoolsActionHelper from './entitlementPools/EntitlementPoolsActionHelper.js';
import LicenseKeyGroupsActionHelper from './licenseKeyGroups/LicenseKeyGroupsActionHelper.js';
import ItemsHelper from '../../common/helpers/ItemsHelper.js';
import MergeEditorActionHelper from 'sdc-app/common/merge/MergeEditorActionHelper.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {CommitModalType} from 'nfvo-components/panel/versionController/components/CommitCommentModal.jsx';
import versionPageActionHelper from 'sdc-app/onboarding/versionsPage/VersionsPageActionHelper.js';
import {itemTypes} from 'sdc-app/onboarding/versionsPage/VersionsPageConstants.js';
import {catalogItemStatuses} from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogConstants.js';
import {actionsEnum as VersionControllerActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';

function baseUrl() {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-license-models/`;
}

function fetchLicenseModels() {
	return RestAPIUtil.fetch(`${baseUrl()}?versionFilter=Draft`);
}

function fetchFinalizedLicenseModels() {
	return RestAPIUtil.fetch(`${baseUrl()}?versionFilter=Certified`);
}

function fetchLicenseModelById(licenseModelId, version) {
	const {id: versionId} = version;
	return RestAPIUtil.fetch(`${baseUrl()}${licenseModelId}/versions/${versionId}`);
}

function putLicenseModel(licenseModel) {
	let {id, vendorName, description, iconRef, version: {id: versionId}} = licenseModel;
	return RestAPIUtil.put(`${baseUrl()}${id}/versions/${versionId}`, {
		vendorName,
		description,
		iconRef
	});
}

function putLicenseModelAction({itemId, action, version}) {
	const {id: versionId} = version;
	return RestAPIUtil.put(`${baseUrl()}${itemId}/versions/${versionId}/actions`, {action: action});
}

const LicenseModelActionHelper = {

	fetchLicenseModels(dispatch) {
		return fetchLicenseModels().then(response => {
			dispatch({
				type: actionTypes.LICENSE_MODELS_LIST_LOADED,
				response
			});
		});
	},

	fetchFinalizedLicenseModels(dispatch) {
		return fetchFinalizedLicenseModels().then(response => dispatch({
			type: actionTypes.FINALIZED_LICENSE_MODELS_LIST_LOADED,
			response
		}));

	},

	fetchLicenseModelById(dispatch, {licenseModelId, version}) {

		return fetchLicenseModelById(licenseModelId, version).then(response => {
			dispatch({
				type: actionTypes.LICENSE_MODEL_LOADED,
				response: {...response, version}
			});
		});
	},

	fetchLicenseModelItems(dispatch, {licenseModelId, version}) {
		return Promise.all([
			LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {licenseModelId, version}),
			FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId, version}),
			EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {licenseModelId, version}),
			LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {licenseModelId, version})
		]);
	},

	manageSubmitAction(dispatch, {licenseModelId, version, isDirty}) {
		if(isDirty) {
			const onCommit = comment => {
				return this.performVCAction(dispatch, {licenseModelId, action: vcActionsEnum.COMMIT, version, comment}).then(() => {
					return this.performSubmitAction(dispatch, {licenseModelId, version});
				});
			};
			dispatch({
				type: modalActionTypes.GLOBAL_MODAL_SHOW,
				data: {
					modalComponentName: modalContentMapper.COMMIT_COMMENT,
					modalComponentProps: {
						onCommit,
						type: CommitModalType.COMMIT_SUBMIT
					},
					title: i18n('Commit & Submit')
				}
			});
			return Promise.reject();
		}
		return this.performSubmitAction(dispatch, {licenseModelId, version});
	},

	performSubmitAction(dispatch, {licenseModelId, version}) {
		return putLicenseModelAction({itemId: licenseModelId, action: vcActionsEnum.SUBMIT, version}).then(() => {
			return ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id}).then(updatedVersion => {
				dispatch({
					type: modalActionTypes.GLOBAL_MODAL_SUCCESS,
					data: {
						title: i18n('Submit Succeeded'),
						msg: i18n('This license model successfully submitted'),
						cancelButtonText: i18n('OK'),
						timeout: 2000
					}
				});
				versionPageActionHelper.fetchVersions(dispatch, {itemType: itemTypes.LICENSE_MODEL, itemId: licenseModelId});
				return Promise.resolve(updatedVersion);
			});
		});
	},

	performVCAction(dispatch, {licenseModelId, action, version, comment}) {
		return MergeEditorActionHelper.analyzeSyncResult(dispatch, {itemId: licenseModelId, version}).then(({inMerge, isDirty, updatedVersion}) => {
			if (updatedVersion.status === catalogItemStatuses.CERTIFIED &&
				(action === VersionControllerActionsEnum.COMMIT || action === VersionControllerActionsEnum.SYNC)) {
				versionPageActionHelper.fetchVersions(dispatch, {itemType: itemTypes.LICENSE_MODEL, itemId: licenseModelId});
				dispatch({
					type: modalActionTypes.GLOBAL_MODAL_WARNING,
					data: {
						title: i18n('Commit error'),
						msg: i18n('Item version already Certified'),
						cancelButtonText: i18n('Cancel')
					}
				});
				return Promise.resolve(updatedVersion);
			}
			if (!inMerge) {
				if(action === vcActionsEnum.SUBMIT) {
					return this.manageSubmitAction(dispatch, {licenseModelId, version, isDirty});
				}
				else {
					return ItemsHelper.performVCAction({itemId: licenseModelId, action, version, comment}).then(() => {
						versionPageActionHelper.fetchVersions(dispatch, {itemType: itemTypes.LICENSE_MODEL, itemId: licenseModelId});
						if (action === vcActionsEnum.SYNC) {
							return MergeEditorActionHelper.analyzeSyncResult(dispatch, {itemId: licenseModelId, version}).then(({updatedVersion}) => {
								return Promise.resolve(updatedVersion);
							});
						} else {
							return ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
						}
					});
				}
			}
		});
	},

	saveLicenseModel(dispatch, {licenseModel}) {
		return putLicenseModel(licenseModel).then(() => {
			dispatch({
				type: actionTypes.LICENSE_MODEL_LOADED,
				response: licenseModel
			});
			const {id, version: {id: versionId}} = licenseModel;
			return ItemsHelper.checkItemStatus(dispatch, {itemId: id, versionId}).then(updatedVersion => {
				if (updatedVersion.status !== licenseModel.version.status) {
					versionPageActionHelper.fetchVersions(dispatch, {itemType: itemTypes.LICENSE_MODEL, itemId: licenseModel.id});
				}
			});
		});
	}

};

export default LicenseModelActionHelper;

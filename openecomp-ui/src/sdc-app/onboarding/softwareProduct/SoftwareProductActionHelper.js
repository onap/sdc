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
import i18n from 'nfvo-utils/i18n/i18n.js';
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';
import LicenseAgreementActionHelper from 'sdc-app/onboarding/licenseModel/licenseAgreement/LicenseAgreementActionHelper.js';
import FeatureGroupsActionHelper from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupsActionHelper.js';

import {actionTypes} from './SoftwareProductConstants.js';
import NotificationConstants from 'nfvo-components/notifications/NotificationConstants.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import SoftwareProductComponentsActionHelper from './components/SoftwareProductComponentsActionHelper.js';
import {actionsEnum as VersionControllerActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';

function baseUrl() {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/`;
}
function softwareProductCategoriesUrl() {
	const restATTPrefix = Configuration.get('restATTPrefix');
	return `${restATTPrefix}/v1/categories/resources/`;
}

function uploadFile(vspId, formData) {

	return RestAPIUtil.create(`${baseUrl()}${vspId}/upload`, formData);

}

function putSoftwareProduct(softwareData) {
	return RestAPIUtil.save(`${baseUrl()}${softwareData.id}`, {
		name: softwareData.name,
		description: softwareData.description,
		category: softwareData.category,
		subCategory: softwareData.subCategory,
		vendorId: softwareData.vendorId,
		vendorName: softwareData.vendorName,
		licensingVersion: softwareData.licensingVersion,
		icon: softwareData.icon,
		licensingData: softwareData.licensingData
	});
}

function putSoftwareProductQuestionnaire(vspId, qdata) {
	return RestAPIUtil.save(`${baseUrl()}${vspId}/questionnaire`, qdata);
}

function putSoftwareProductAction(id, action) {
	return RestAPIUtil.save(`${baseUrl()}${id}/actions`, {action: action});
}

function fetchSoftwareProductList() {
	return RestAPIUtil.fetch(baseUrl());
}

function fetchSoftwareProduct(vspId, version) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl()}${vspId}${versionQuery}`);
}

function fetchSoftwareProductQuestionnaire(vspId, version) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl()}${vspId}/questionnaire${versionQuery}`);
}

function objToString(obj) {
	let str = '';
	if (obj instanceof Array) {
		obj.forEach((item) => {
			str += objToString(item) + '\n';
		});
	} else {
		for (let p in obj) {
			if (obj.hasOwnProperty(p)) {
				str += obj[p] + '\n';
			}
		}
	}
	return str;
}

function parseUploadErrorMsg(error) {
	let message = '';
	for (let key in error) {
		if (error.hasOwnProperty(key)) {
			message += objToString(error[key]) + '\n';
		}
	}
	return message;
}

function fetchSoftwareProductCategories(dispatch) {
	let handleResponse = response => dispatch({
		type: actionTypes.SOFTWARE_PRODUCT_CATEGORIES_LOADED,
		softwareProductCategories: response
	});
	return RestAPIUtil.fetch(softwareProductCategoriesUrl())
		.then(handleResponse)
		.fail(() => handleResponse(null));
}

function loadLicensingData(dispatch, {licenseModelId, licensingVersion}) {
	LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {licenseModelId, version: licensingVersion});
	FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId, version: licensingVersion});
}

function getExpandedItemsId(items, itemIdToToggle) {
	for(let i = 0; i < items.length; i++) {
		if(items[i].id === itemIdToToggle) {
			if (items[i].expanded) {
				return {};
			} else {
				return {[itemIdToToggle]: true};
			}
		}
		else if(items[i].items && items[i].items.length > 0) {
			let mapOfExpandedIds = getExpandedItemsId(items[i].items, itemIdToToggle);
			if (mapOfExpandedIds !== false) {
				mapOfExpandedIds[items[i].id] = true;
				return mapOfExpandedIds;
			}
		}
	}
	return false;
}

const SoftwareProductActionHelper = {

	loadSoftwareProductAssociatedData(dispatch) {
		fetchSoftwareProductCategories(dispatch);
		LicenseModelActionHelper.fetchFinalizedLicenseModels(dispatch);
	},

	loadSoftwareProductDetailsData(dispatch, {licenseModelId, licensingVersion}) {
		SoftwareProductActionHelper.loadSoftwareProductAssociatedData(dispatch);
		loadLicensingData(dispatch, {licenseModelId, licensingVersion});
	},

	fetchSoftwareProductList(dispatch) {
		return fetchSoftwareProductList().then(response => dispatch({
			type: actionTypes.SOFTWARE_PRODUCT_LIST_LOADED,
			response
		}));
	},

	uploadFile(dispatch, {softwareProductId, formData, failedNotificationTitle}) {
		Promise.resolve()
			.then(() => uploadFile(softwareProductId, formData))
			.then(response => {
				if (response.status !== 'Success') {
					throw new Error(parseUploadErrorMsg(response.errors));
				}
			})
			.then(() => {
				SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(dispatch, {softwareProductId});
				OnboardingActionHelper.navigateToSoftwareProductAttachments(dispatch, {softwareProductId});
				SoftwareProductActionHelper.fetchSoftwareProduct(dispatch, {softwareProductId});
			})
			.catch(error => {
				dispatch({
					type: NotificationConstants.NOTIFY_ERROR,
					data: {title: failedNotificationTitle, msg: error.message}
				});
			});
	},

	uploadConfirmation(dispatch, {softwareProductId, formData, failedNotificationTitle}) {
		dispatch({
			type: actionTypes.softwareProductEditor.UPLOAD_CONFIRMATION,
			uploadData: {
				softwareProductId,
				formData,
				failedNotificationTitle
			}
		});
	},
	hideUploadConfirm (dispatch) {
		dispatch({
			type: actionTypes.softwareProductEditor.UPLOAD_CONFIRMATION
		});
	},
	updateSoftwareProduct(dispatch, {softwareProduct, qdata}) {
		return Promise.all([
			SoftwareProductActionHelper.updateSoftwareProductData(dispatch, {softwareProduct}).then(
				() => dispatch({
					type: actionTypes.SOFTWARE_PRODUCT_LIST_EDIT,
					payload: {softwareProduct}
				})
			),
			SoftwareProductActionHelper.updateSoftwareProductQuestionnaire(dispatch, {
				softwareProductId: softwareProduct.id,
				qdata
			})
		]);
	},

	updateSoftwareProductData(dispatch, {softwareProduct}) {
		return putSoftwareProduct(softwareProduct);
	},

	updateSoftwareProductQuestionnaire(dispatch, {softwareProductId, qdata}) {
		return putSoftwareProductQuestionnaire(softwareProductId, qdata);
	},

	softwareProductEditorDataChanged(dispatch, {deltaData}) {
		dispatch({
			type: actionTypes.softwareProductEditor.DATA_CHANGED,
			deltaData
		});
	},

	softwareProductQuestionnaireUpdate(dispatch, {data}) {
		dispatch({
			type: actionTypes.SOFTWARE_PRODUCT_QUESTIONNAIRE_UPDATE,
			payload: {qdata: data}
		});
	},

	softwareProductEditorVendorChanged(dispatch, {deltaData}) {
		LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {licenseModelId: deltaData.vendorId, version: deltaData.licensingVersion});
		FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId: deltaData.vendorId, version: deltaData.licensingVersion});
		SoftwareProductActionHelper.softwareProductEditorDataChanged(dispatch, {deltaData});
	},

	setIsValidityData(dispatch, {isValidityData}) {
		dispatch({
			type: actionTypes.softwareProductEditor.IS_VALIDITY_DATA_CHANGED,
			isValidityData
		});
	},

	addSoftwareProduct(dispatch, {softwareProduct}) {
		dispatch({
			type: actionTypes.ADD_SOFTWARE_PRODUCT,
			softwareProduct
		});
	},

	fetchSoftwareProduct(dispatch, {softwareProductId, version}) {
		return Promise.all([
			fetchSoftwareProduct(softwareProductId, version).then(response => {
				dispatch({
					type: actionTypes.SOFTWARE_PRODUCT_LOADED,
					response
				});
				return response;
			}),
			fetchSoftwareProductQuestionnaire(softwareProductId, version).then(response => {
				dispatch({
					type: actionTypes.SOFTWARE_PRODUCT_QUESTIONNAIRE_UPDATE,
					payload: {
						qdata: response.data ? JSON.parse(response.data) : {},
						qschema: JSON.parse(response.schema)
					}
				});
			})
		]);
	},

	performVCAction(dispatch, {softwareProductId, action}) {
		if (action === VersionControllerActionsEnum.SUBMIT) {
			return putSoftwareProductAction(softwareProductId, action).then(() => {
				return putSoftwareProductAction(softwareProductId, VersionControllerActionsEnum.CREATE_PACKAGE).then(() => {
					dispatch({
						type: NotificationConstants.NOTIFY_SUCCESS,
						data: {
							title: i18n('Submit Succeeded'),
							msg: i18n('This software product successfully submitted'),
							timeout: 2000
						}
					});
					fetchSoftwareProduct(softwareProductId).then(response => {
						dispatch({
							type: actionTypes.SOFTWARE_PRODUCT_LOADED,
							response
						});
					});
				});
			}, error => dispatch({
				type: NotificationConstants.NOTIFY_ERROR,
				data: {title: i18n('Submit Failed'), validationResponse: error.responseJSON}
			}));
		}
		else {
			return putSoftwareProductAction(softwareProductId, action).then(() => {
				fetchSoftwareProduct(softwareProductId).then(response => {
					dispatch({
						type: actionTypes.SOFTWARE_PRODUCT_LOADED,
						response
					});
				});
			});
		}
	},

	switchVersion(dispatch, {softwareProductId, licenseModelId, version}) {
		OnboardingActionHelper.navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, licenseModelId, version});
	},

	toggleNavigationItems(dispatch, {items, itemIdToExpand}) {
		let mapOfExpandedIds = getExpandedItemsId(items, itemIdToExpand);
		dispatch({
			type: actionTypes.TOGGLE_NAVIGATION_ITEM,
			mapOfExpandedIds
		});
	},

	/** for the next verision */
	addComponent(dispatch) {
		return dispatch;
	}
};

export default SoftwareProductActionHelper;

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
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';
import LicenseAgreementActionHelper from 'sdc-app/onboarding/licenseModel/licenseAgreement/LicenseAgreementActionHelper.js';
import FeatureGroupsActionHelper from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupsActionHelper.js';

import {actionTypes, onboardingOriginTypes, PRODUCT_QUESTIONNAIRE, forms} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import SoftwareProductComponentsActionHelper from './components/SoftwareProductComponentsActionHelper.js';
import {actionsEnum as VersionControllerActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import {actionTypes as HeatSetupActions} from 'sdc-app/onboarding/softwareProduct/attachments/setup/HeatSetupConstants.js';
import {actionTypes as featureGroupsActionConstants} from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupsConstants.js';
import {actionTypes as licenseAgreementActionTypes} from 'sdc-app/onboarding/licenseModel/licenseAgreement/LicenseAgreementConstants.js';
import {actionTypes as componentActionTypes} from './components/SoftwareProductComponentsConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import {enums, screenTypes} from 'sdc-app/onboarding/OnboardingConstants.js';
import MergeEditorActionHelper from 'sdc-app/common/merge/MergeEditorActionHelper.js';
import {CommitModalType} from 'nfvo-components/panel/versionController/components/CommitCommentModal.jsx';
import {actionTypes as commonActionTypes} from 'sdc-app/common/reducers/PlainDataReducerConstants.js';
import versionPageActionHelper from 'sdc-app/onboarding/versionsPage/VersionsPageActionHelper.js';
import {itemTypes} from 'sdc-app/onboarding/versionsPage/VersionsPageConstants.js';
import {catalogItemStatuses} from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogConstants.js';
import getValue from 'nfvo-utils/getValue.js';

function getLicensingData(licensingData = {}) {
	const {licenseAgreement, featureGroups} = licensingData;
	const newlicenseAgreement = getValue(licenseAgreement);
	const newfeatureGroups = getValue(featureGroups);
	return newlicenseAgreement ? {
		licenseAgreement: newlicenseAgreement,
		featureGroups: newfeatureGroups
	} : undefined;
};

function baseUrl() {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/`;
}
function softwareProductCategoriesUrl() {
	const restATTPrefix = Configuration.get('restATTPrefix');
	return `${restATTPrefix}/v1/categories/resources/`;
}

function uploadFile(vspId, formData, version) {
	return RestAPIUtil.post(`${baseUrl()}${vspId}/versions/${version.id}/orchestration-template-candidate`, formData);

}

function putSoftwareProduct({softwareProduct, version}) {
	return RestAPIUtil.put(`${baseUrl()}${softwareProduct.id}/versions/${version.id}`, {
		name: softwareProduct.name,
		description: softwareProduct.description,
		category: softwareProduct.category,
		subCategory: softwareProduct.subCategory,
		vendorId: softwareProduct.vendorId,
		vendorName: softwareProduct.vendorName,
		licensingVersion: softwareProduct.licensingVersion ? softwareProduct.licensingVersion : undefined,
		icon: softwareProduct.icon,
		licensingData: getLicensingData(softwareProduct.licensingData)
	});
}

function putSoftwareProductQuestionnaire(vspId, qdata, version) {
	return RestAPIUtil.put(`${baseUrl()}${vspId}/versions/${version.id}/questionnaire`, qdata);
}

function putSoftwareProductAction(id, action, version) {
	return RestAPIUtil.put(`${baseUrl()}${id}/versions/${version.id}/actions`, {action: action});
}

function fetchSoftwareProductList() {
	return RestAPIUtil.fetch(`${baseUrl()}?versionFilter=Draft`);
}

function fetchFinalizedSoftwareProductList() {
	return RestAPIUtil.fetch(`${baseUrl()}?versionFilter=Certified`);
}

function fetchSoftwareProduct(vspId, version) {
	return RestAPIUtil.fetch(`${baseUrl()}${vspId}/versions/${version.id}`);
}

function fetchSoftwareProductQuestionnaire(vspId, version) {
	return RestAPIUtil.fetch(`${baseUrl()}${vspId}/versions/${version.id}/questionnaire`);
}

function updateSoftwareProductHeatCandidate(softwareProductId, heatCandidate, version) {
	return RestAPIUtil.put(`${baseUrl()}${softwareProductId}/versions/${version.id}/orchestration-template-candidate/manifest`, heatCandidate);
}
function validateHeatCandidate(softwareProductId, version) {
	return RestAPIUtil.put(`${baseUrl()}${softwareProductId}/versions/${version.id}/orchestration-template-candidate/process`);
}

function fetchOrchestrationTemplateCandidate(softwareProductId, version, ) {
	return RestAPIUtil.fetch(`${baseUrl()}${softwareProductId}/versions/${version.id}/orchestration-template-candidate`, {dataType: 'binary'});
}

function objToString(obj) {
	let str = '';
	if (obj instanceof Array) {
		obj.forEach((item) => {
			str += objToString(item) + '\n';
		});
	}
	else {
		for (let p in obj) {
			if (obj.hasOwnProperty(p)) {
				str += obj[p] + '\n';
			}
		}
	}
	return str.replace(/\n$/, '');
}

function parseUploadErrorMsg(error) {
	let message = '';
	for (let key in error) {
		if (error.hasOwnProperty(key)) {
			message += objToString(error[key]) + '\n';
		}
	}
	return message.replace(/\n$/, '');
}

function fetchSoftwareProductCategories(dispatch) {
	let handleResponse = response => dispatch({
		type: actionTypes.SOFTWARE_PRODUCT_CATEGORIES_LOADED,
		softwareProductCategories: response
	});
	return RestAPIUtil.fetch(softwareProductCategoriesUrl())
		.then(handleResponse)
		.catch(() => handleResponse(null));
}

function loadLicensingData(dispatch, {licenseModelId, licensingVersion}) {
	return ItemsHelper.fetchVersion({itemId: licenseModelId, versionId: licensingVersion}).then(() => {
		return Promise.all([
			LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {licenseModelId, version: {id: licensingVersion}}),
			FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId, version: {id: licensingVersion}})
		]);
	});
}

function getExpandedItemsId(items, itemIdToToggle) {
	for (let i = 0; i < items.length; i++) {
		if (items[i].id === itemIdToToggle) {
			if (items[i].expanded) {
				return {};
			}
			else {
				return {[itemIdToToggle]: true};
			}
		}
		else if (items[i].items && items[i].items.length > 0) {
			let mapOfExpandedIds = getExpandedItemsId(items[i].items, itemIdToToggle);
			if (mapOfExpandedIds !== false) {
				mapOfExpandedIds[items[i].id] = true;
				return mapOfExpandedIds;
			}
		}
	}
	return false;
}

function getTimestampString() {
	let date = new Date();
	let z = n => n < 10 ? '0' + n : n;
	return `${date.getFullYear()}-${z(date.getMonth())}-${z(date.getDate())}_${z(date.getHours())}-${z(date.getMinutes())}`;
}

function showFileSaveDialog({blob, xhr, defaultFilename, addTimestamp}) {
	let filename;
	let contentDisposition = xhr.getResponseHeader('content-disposition') ? xhr.getResponseHeader('content-disposition') : '';
	let match = contentDisposition.match(/filename=(.*?)(;|$)/);
	if (match) {
		filename = match[1];
	}
	else {
		filename = defaultFilename;
	}

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

function migrateSoftwareProduct(vspId, version) {
	return RestAPIUtil.put(`${baseUrl()}${vspId}/versions/${version.id}/heal`);
}



const SoftwareProductActionHelper = {

	fetchFinalizedSoftwareProductList(dispatch) {
		return fetchFinalizedSoftwareProductList().then(response => dispatch({
			type: actionTypes.FINALIZED_SOFTWARE_PRODUCT_LIST_LOADED,
			response
		}));
	},

	loadSoftwareProductAssociatedData(dispatch) {
		fetchSoftwareProductCategories(dispatch);
		LicenseModelActionHelper.fetchFinalizedLicenseModels(dispatch);
	},

	loadSoftwareProductDetailsData(dispatch, {licenseModelId, licensingVersion}) {
		SoftwareProductActionHelper.loadSoftwareProductAssociatedData(dispatch);
		if (licensingVersion) {
			return loadLicensingData(dispatch, {licenseModelId, licensingVersion});
		}
		return Promise.resolve();
	},

	fetchSoftwareProductList(dispatch) {
		return fetchSoftwareProductList().then(response => dispatch({
			type: actionTypes.SOFTWARE_PRODUCT_LIST_LOADED,
			response
		}));
	},

	loadSoftwareProductHeatCandidate(dispatch, {softwareProductId, version}){
		return RestAPIUtil.fetch(`${baseUrl()}${softwareProductId}/versions/${version.id}/orchestration-template-candidate/manifest`).then(response => dispatch({
			type: HeatSetupActions.MANIFEST_LOADED,
			response
		}));
	},

	loadLicensingVersionsList(dispatch, {licenseModelId}){
		return ItemsHelper.fetchVersions({itemId: licenseModelId}).then(response => {
			dispatch({
				type: actionTypes.LOAD_LICENSING_VERSIONS_LIST,
				licensingVersionsList: response.results
			});
		});
	},
	updateSoftwareProductHeatCandidate(dispatch, {softwareProductId, heatCandidate, version}){
		return updateSoftwareProductHeatCandidate(softwareProductId, heatCandidate, version);
	},

	processAndValidateHeatCandidate(dispatch, {softwareProductId, version}){
		return validateHeatCandidate(softwareProductId, version).then(response => {
			if (response.status === 'Success') {
				SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(dispatch, {softwareProductId, version});
				SoftwareProductActionHelper.fetchSoftwareProduct(dispatch, {softwareProductId, version});
			}
		});
	},

	uploadFile(dispatch, {softwareProductId, formData, failedNotificationTitle, version}) {
		dispatch({
			type: HeatSetupActions.FILL_HEAT_SETUP_CACHE,
			payload: {}
		});

		Promise.resolve()
			.then(() => uploadFile(softwareProductId, formData, version))
			.then(response => {
				if (response.status === 'Success') {
					dispatch({
						type: commonActionTypes.DATA_CHANGED,
						deltaData: {onboardingOrigin: response.onboardingOrigin},
						formName: forms.VENDOR_SOFTWARE_PRODUCT_DETAILS
					});
					switch(response.onboardingOrigin){
						case onboardingOriginTypes.ZIP:
							ScreensHelper.loadScreen(dispatch, {
								screen: enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS_SETUP, screenType: screenTypes.SOFTWARE_PRODUCT,
								props: {softwareProductId, version}
							});
							break;
						case onboardingOriginTypes.CSAR:
							ScreensHelper.loadScreen(dispatch, {
								screen: enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS_VALIDATION, screenType: screenTypes.SOFTWARE_PRODUCT,
								props: {softwareProductId, version}
							});
							break;
					}
				}
				else {
					throw new Error(parseUploadErrorMsg(response.errors));
				}
			})
			.catch(error => {
				dispatch({
					type: modalActionTypes.GLOBAL_MODAL_ERROR,
					data: {
						title: failedNotificationTitle,
						msg: error.message || (error.responseJSON && error.responseJSON.message)
					}
				});
			});
	},

	downloadHeatFile(dispatch, {softwareProductId, heatCandidate, isReadOnlyMode, version}){
		let p = isReadOnlyMode ? Promise.resolve() : SoftwareProductActionHelper.updateSoftwareProductHeatCandidate(dispatch, {softwareProductId, heatCandidate, version});
		p.then(() => {
			fetchOrchestrationTemplateCandidate(softwareProductId, version)
				.then((blob, statusText, xhr) => showFileSaveDialog({blob, xhr, defaultFilename: 'HEAT_file.zip', addTimestamp: true}));
		}, null/* do not download if data was not saved correctly*/);
	},

	hideUploadConfirm (dispatch) {
		dispatch({
			type: actionTypes.softwareProductEditor.UPLOAD_CONFIRMATION
		});
	},
	updateSoftwareProduct(dispatch, {softwareProduct, version, qdata}) {
		return Promise.all([
			SoftwareProductActionHelper.updateSoftwareProductData(dispatch, {softwareProduct, version}).then(
				() => dispatch({
					type: actionTypes.SOFTWARE_PRODUCT_LIST_EDIT,
					payload: {softwareProduct}
				})
			),
			SoftwareProductActionHelper.updateSoftwareProductQuestionnaire(dispatch, {
				softwareProductId: softwareProduct.id,
				qdata,
				version
			})
		]);
	},

	updateSoftwareProductData(dispatch, {softwareProduct, version}) {
		return putSoftwareProduct({softwareProduct, version});
	},

	updateSoftwareProductQuestionnaire(dispatch, {softwareProductId, qdata, version}) {
		return putSoftwareProductQuestionnaire(softwareProductId, qdata, version);
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

	softwareProductEditorVendorChanged(dispatch, {deltaData, formName}) {
		if (deltaData.licensingVersion){
			return loadLicensingData(dispatch, {licenseModelId: deltaData.vendorId, licensingVersion: deltaData.licensingVersion}).then(() => {
				ValidationHelper.dataChanged(dispatch, {deltaData, formName});
				return Promise.resolve();
			});
		} else if (deltaData.vendorId) {
			ValidationHelper.dataChanged(dispatch, {deltaData, formName});
			return SoftwareProductActionHelper.loadLicensingVersionsList(dispatch, {
				licenseModelId: deltaData.vendorId
			}).then( () =>
				OnboardingActionHelper.forceBreadCrumbsUpdate(dispatch)
			);
		} else {
			ValidationHelper.dataChanged(dispatch, {deltaData, formName});

			dispatch({
				type: licenseAgreementActionTypes.LICENSE_AGREEMENT_LIST_LOADED,
				response: {results: []}
			});

			dispatch({
				type: featureGroupsActionConstants.FEATURE_GROUPS_LIST_LOADED,
				response: {results: []}
			});
		}

	},

	setIsValidityData(dispatch, {isValidityData}) {
		dispatch({
			type: actionTypes.softwareProductEditor.IS_VALIDITY_DATA_CHANGED,
			isValidityData
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
				ValidationHelper.qDataLoaded(dispatch, {response: {qdata: response.data ? JSON.parse(response.data) : {},
					qschema: JSON.parse(response.schema)}, qName: PRODUCT_QUESTIONNAIRE});
			})
		]);
	},

	manageSubmitAction(dispatch, {softwareProductId, version, isDirty}) {
		if (isDirty) {
			const onCommit = comment => {
				return this.performVCAction(dispatch, {softwareProductId, action: VersionControllerActionsEnum.COMMIT, version, comment}).then(() => {
					return this.performSubmitAction(dispatch, {softwareProductId, version});
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
			return Promise.resolve(version);
		}
		return this.performSubmitAction(dispatch, {softwareProductId, version});
	},

	performSubmitAction(dispatch, {softwareProductId, version}) {
		return putSoftwareProductAction(softwareProductId, VersionControllerActionsEnum.SUBMIT, version).then(() => {
			return putSoftwareProductAction(softwareProductId, VersionControllerActionsEnum.CREATE_PACKAGE, version).then(() => {
				return ItemsHelper.checkItemStatus(dispatch, {itemId: softwareProductId, versionId: version.id}).then(updatedVersion => {
					dispatch({
						type: modalActionTypes.GLOBAL_MODAL_SUCCESS,
						data: {
							title: i18n('Submit Succeeded'),
							msg: i18n('This software product successfully submitted'),
							cancelButtonText: i18n('OK'),
							timeout: 2000
						}
					});
					versionPageActionHelper.fetchVersions(dispatch, {itemType: itemTypes.SOFTWARE_PRODUCT, itemId: softwareProductId});
					return Promise.resolve(updatedVersion);
				});
			});
		}, error =>
		{
			dispatch({
				type: modalActionTypes.GLOBAL_MODAL_ERROR,
				data: {
					modalComponentName: modalContentMapper.SUMBIT_ERROR_RESPONSE,
					title: i18n('Submit Failed'),
					modalComponentProps: {
						validationResponse: error.responseJSON
					},
					cancelButtonText: i18n('OK')
				}
			});
			return Promise.reject(error.responseJSON);
		});
	},

	performVCAction(dispatch, {softwareProductId, action, version, comment}) {
		return MergeEditorActionHelper.analyzeSyncResult(dispatch, {itemId: softwareProductId, version}).then(({inMerge, isDirty, updatedVersion}) => {
			if (updatedVersion.status === catalogItemStatuses.CERTIFIED &&
					 (action === VersionControllerActionsEnum.COMMIT || action === VersionControllerActionsEnum.SYNC)) {
				versionPageActionHelper.fetchVersions(dispatch, {itemType: itemTypes.SOFTWARE_PRODUCT, itemId: softwareProductId});
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
				if (action === VersionControllerActionsEnum.SUBMIT) {
					return this.manageSubmitAction(dispatch, {softwareProductId, version, isDirty});
				}
				else {
					let isCallActionValid = action !== VersionControllerActionsEnum.COMMIT || isDirty;
					if(isCallActionValid) {
						return 	ItemsHelper.performVCAction({itemId: softwareProductId, action, version, comment}).then(() => {
							versionPageActionHelper.fetchVersions(dispatch, {itemType: itemTypes.LICENSE_MODEL, itemId: softwareProductId});
							if (action === VersionControllerActionsEnum.SYNC) {
								return MergeEditorActionHelper.analyzeSyncResult(dispatch, {itemId: softwareProductId, version}).then(({updatedVersion}) => {
									return Promise.resolve(updatedVersion);
								});
							} else {
								return ItemsHelper.checkItemStatus(dispatch, {itemId: softwareProductId, versionId: version.id});
							}
						});
					}
					else {
						dispatch({
							type: modalActionTypes.GLOBAL_MODAL_ERROR,
							data: {
								title: i18n('Commit Failed'),
								msg: i18n('There is nothing to commit')
							}
						});
					}
				}
			}
		});
	},

	toggleNavigationItems(dispatch, {items, itemIdToExpand}) {
		let mapOfExpandedIds = getExpandedItemsId(items, itemIdToExpand);
		dispatch({
			type: actionTypes.TOGGLE_NAVIGATION_ITEM,
			mapOfExpandedIds
		});
	},

	/** for the next verision */
	addComponent(dispatch, {softwareProductId, modalClassName, version}) {
		SoftwareProductComponentsActionHelper.clearComponentCreationData(dispatch);
		dispatch({
			type: componentActionTypes.COMPONENT_CREATE_OPEN
		});
		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_SHOW,
			data: {
				modalComponentName: modalContentMapper.COMPONENT_CREATION,
				modalComponentProps: {softwareProductId, version},
				modalClassName,
				title: 'Create Virtual Function Component'
			}
		});
	},

	migrateSoftwareProduct(dispatch, {softwareProduct}) {
		let {id: softwareProductId, version} = softwareProduct;
		const  newVer = version.id;
		migrateSoftwareProduct(softwareProductId, version).then(() => ScreensHelper.loadScreen(dispatch, {
			screen: enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE, screenType: screenTypes.SOFTWARE_PRODUCT,
			props: {softwareProductId, version: {id: newVer, label: newVer}}
		}));
	}

};

export default SoftwareProductActionHelper;

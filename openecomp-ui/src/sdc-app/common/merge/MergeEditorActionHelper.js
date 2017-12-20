/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
import {actionTypes, rules, dataRules, SyncStates} from './MergeEditorConstants.js';
import cloneDeep from 'lodash/cloneDeep.js';
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import ItemsHelper from '../../common/helpers/ItemsHelper.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {optionsInputValues as epOptionsValues} from 'sdc-app/onboarding/licenseModel/entitlementPools/EntitlementPoolsConstants.js';
import {optionsInputValues as laOptionsValues} from 'sdc-app/onboarding/licenseModel/licenseAgreement/LicenseAgreementConstants.js';
import {optionsInputValues as processOptionValues} from 'sdc-app/onboarding/softwareProduct/components/processes/SoftwareProductComponentProcessesConstants.js';
import {selectValues as limitSelectValues} from 'sdc-app/onboarding/licenseModel/limits/LimitEditorConstants.js';
import FeatureGroupsActionHelper from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupsActionHelper.js';
import LicenseAgreementActionHelper from 'sdc-app/onboarding/licenseModel/licenseAgreement/LicenseAgreementActionHelper.js';
import moment from 'moment';
import {DATE_FORMAT} from 'sdc-app/onboarding/OnboardingConstants.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';

function softwareProductCategoriesUrl() {
	const restATTPrefix = Configuration.get('restATTPrefix');
	return `${restATTPrefix}/v1/categories/resources/`;
}

function versionUrl(itemId, versionId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/items/${itemId}/versions/${versionId}`;
}

function baseUrl(itemId, version, conflictId) {
	const versionId = version.id;
	const restPrefix = Configuration.get('restPrefix');
	let baseUrl = `${restPrefix}/v1.0/items/${itemId}/versions/${versionId}/conflicts`;
	return conflictId ? `${baseUrl}/${conflictId}` : baseUrl;
}

function fetchConflicts({itemId, version}) {
	return RestAPIUtil.fetch(`${baseUrl(itemId, version)}`);
}

function fetchConflictById({itemId, version, cid}) {
	return RestAPIUtil.fetch(`${baseUrl(itemId, version, cid)}`);
}

function resolveConflict({itemId, version, conflictId, resolution}) {
	return RestAPIUtil.put(`${baseUrl(itemId, version, conflictId)}`, {resolution});
}

function fetchCategories() {
	return RestAPIUtil.fetch(softwareProductCategoriesUrl());
}

function fetchVersion({vendorId, licensingVersion}) {
	return RestAPIUtil.fetch(versionUrl(vendorId, licensingVersion));
}

function createCategoryStr(data, {categories}) {

	let {category, subCategory} = data;
	let foundCat = categories.find(element => element.uniqueId === category);
	if (!foundCat) { return ''; }

	let catName = foundCat.name;
	let foundSub =  foundCat.subcategories.find(element => element.uniqueId === subCategory);
	if (!foundSub) { return `${catName}`; }

	let subcatName = foundSub.name;
	return `${catName} - ${subcatName}`;

}

function getEnumValues({enums, list}) {

	if (!list) { return ''; }
	return list.map(item => enums.find(el => el.enum === item).title);

}

const MergeEditorActionHelper = {

	analyzeSyncResult(dispatch, {itemId, version}) {
		return ItemsHelper.checkItemStatus(dispatch, {itemId, versionId: version.id}).then((response) => {
			let inMerge = response && response.state && response.state.synchronizationState === SyncStates.MERGE;
			if (inMerge) {
				MergeEditorActionHelper.fetchConflicts(dispatch, {itemId, version}).then(() =>
					dispatch({
						type: modalActionTypes.GLOBAL_MODAL_SHOW,
						data: {
							modalComponentName: modalContentMapper.MERGE_EDITOR,
							modalClassName: 'merge-editor-modal',
							title: `${i18n('Merge Required')} - ${version.description}`,
							onDeclined: () => {
								dispatch({
									type: modalActionTypes.GLOBAL_MODAL_CLOSE
								});
							},
							modalComponentProps: {
								size: 'lg',
								type: 'default'
							}
						}
					})
				);
			}
			return Promise.resolve({updatedVersion: response, inMerge, isDirty: response.state.dirty});
		});
	},

	fetchConflicts(dispatch, {itemId, version}) {
		return fetchConflicts({itemId, version}).then(
			(data) => {
				dispatch({
					type: actionTypes.LOAD_CONFLICTS,
					data
				});
				return data;
			}
		);
	},

	fetchConflict(dispatch, {itemId, version, cid}) {
		fetchConflictById({itemId, version, cid}).then(
			(data) => {
				let newData = {};
				newData = MergeEditorActionHelper.processConflict(dispatch, {conflict: data, itemId, cid, version});
				dispatch({
					type: actionTypes.LOAD_CONFLICT,
					data: newData
				});
			}
		);
	},

	resolveConflict(dispatch, {itemId, version, conflictId, resolution, currentScreen}) {
		resolveConflict({itemId, version, conflictId, resolution}).then(() => {
			MergeEditorActionHelper.fetchConflicts(dispatch, {itemId, version}).then(conflicts => {
				if(conflicts.conflictInfoList && conflicts.conflictInfoList.length === 0) {
					dispatch({
						type: modalActionTypes.GLOBAL_MODAL_CLOSE
					});
					ScreensHelper.loadLandingScreen(dispatch, {previousScreenName: currentScreen.screen, props: currentScreen.props});
					ItemsHelper.checkItemStatus(dispatch, {itemId, versionId: version.id});
				}
			});
		});
	},

	createConflictObject(data, {cid, conflict, dispatch, itemId, version, isYours}) {

		let newData = {};

		for (let key in data) {

			if (data.hasOwnProperty(key)) {
				let value = data[key];
				let fieldRule = dataRules[conflict.type] && dataRules[conflict.type][key] || dataRules.general[key];

				if (fieldRule) {
					switch (fieldRule.rule) {

						case rules.SKIP:
							break;

						case rules.BOOLEAN:
							let {trueValue, falseValue} = fieldRule;
							newData[key] = value === trueValue ? true : value === falseValue ? false : undefined;
							break;

						case rules.PARSE:
							let {moveFields, subFields} = fieldRule;
							if (moveFields) {
								let fields = subFields || Object.keys(value);
								fields.forEach(field => {
									newData[field] = MergeEditorActionHelper.createConflictObject(
										value[field], {cid, conflict, dispatch, itemId, version, isYours}
									);
								});
							} else {
								newData[key] = MergeEditorActionHelper.createConflictObject(
									value, {cid, conflict, dispatch, itemId, version, isYours}
								);
							}
							break;

						case rules.FUNCTION:
							let {args, functionName} = fieldRule;
							newData[key] = MergeEditorActionHelper[functionName](data, {
								cid, conflict, dispatch, version, fieldName: key, isYours, itemId, args
							});
							break;

						default:
							newData[key] = value;
							break;
					}

				} else {
					newData[key] = value;

				}
			}
		}

		return newData;

	},

	getNamesFromIDs(data, {version, cid, dispatch, itemId, fieldName, isYours, args}) {

		let idList = data[fieldName] || [];
		let {fetchFunction, fetchField} = args;

		let promises = idList.map(id =>
			new Promise(resolve =>
				MergeEditorActionHelper[fetchFunction](
					dispatch, {licenseModelId: itemId, [fetchField]: id, version}
				).then(item => resolve(item.name))
			)
		);

		Promise.all(promises).then(fetchedItems => {
			let yoursOrTheirs = isYours ? 'yoursField' : 'theirsField';
			dispatch({
				type: actionTypes.DATA_PROCESSED,
				data: {
					cid,
					[yoursOrTheirs]: { name: fieldName, value: fetchedItems }
				}
			});
		});

		return idList;

	},

	getFeatureGroups(data, {version, cid, dispatch, itemId, fieldName, isYours}) {

		let featureGroups = data[fieldName] || [];
		if (!(featureGroups instanceof Array)) {
			featureGroups = [featureGroups];
		}

		let promises = featureGroups.map(featureGroupId =>
			new Promise(resolve =>
				FeatureGroupsActionHelper.fetchFeatureGroup(
					dispatch, {licenseModelId: itemId, featureGroupId, version}
				).then(featureGroup => resolve(featureGroup.name))
				.catch(reason => console.log(`getFeatureGroups Promise rejected ('${reason}')`))
			)
		);

		Promise.all(promises).then(fetchedGroups => {
			let yoursOrTheirs = isYours ? 'yoursField' : 'theirsField';
			dispatch({
				type: actionTypes.DATA_PROCESSED,
				data: {
					cid,
					[yoursOrTheirs]: { name: fieldName, value: fetchedGroups }
				}
			});
		});

		return featureGroups;

	},

	getLicenseAgreements(data, {version, cid, dispatch, itemId, fieldName, isYours}) {

		let licenseAgreements = data[fieldName] || [];
		if (!(licenseAgreements instanceof Array)) {
			licenseAgreements = [licenseAgreements];
		}

		let promises = licenseAgreements.map(licenseAgreementId =>
			new Promise(resolve =>
				LicenseAgreementActionHelper.fetchLicenseAgreement(
					dispatch, {licenseModelId: itemId, licenseAgreementId, version}
				).then(licenseAgreement => resolve(licenseAgreement.name))
				.catch(reason => console.log(`getLicenseAgreements Promise rejected ('${reason}')`))
			)
		);

		Promise.all(promises).then(fetchedAgreements => {
			let yoursOrTheirs = isYours ? 'yoursField' : 'theirsField';
			dispatch({
				type: actionTypes.DATA_PROCESSED,
				data: {
					cid,
					[yoursOrTheirs]: { name: fieldName, value: fetchedAgreements }
				}
			});
		});

		return licenseAgreements;

	},

	processConflict(dispatch, {conflict, cid, version, itemId,}) {

		let {id, type, yours, theirs} = conflict;

		let newYours = MergeEditorActionHelper.createConflictObject(
			cloneDeep(yours), {cid, conflict, dispatch, itemId, version, isYours: true}
		);
		let newTheirs = MergeEditorActionHelper.createConflictObject(
			cloneDeep(theirs), {cid, conflict, dispatch, itemId, version, isYours: false}
		);

		return {
			id,
			type,
			yours: newYours,
			theirs: newTheirs
		};

	},

	reduceList(data, {fieldName, args}) {

		let {subField} = args;
		return data[fieldName].map(el => el[subField]);

	},

	getEnumList({fieldName}) {

		const enumLists = {
			'licenseTerm': laOptionsValues.LICENSE_MODEL_TYPE,
			'operationalScope': epOptionsValues.OPERATIONAL_SCOPE,
			'processType': processOptionValues.PROCESS_TYPE,
			'limitType': [
				{title: 'Service Provider', enum: 'ServiceProvider'},
				{title: 'Vendor', enum: 'Vendor'}
			],
			'limitUnit': limitSelectValues.UNIT
		};

		return enumLists[fieldName];

	},

	getEnumValue(data, {fieldName, args = {}}) {

		let value = data[fieldName];
		let enumValues = MergeEditorActionHelper.getEnumList({fieldName: args.listName || fieldName});
		let enumValue = enumValues.find(el => el.enum === value);

		return enumValue && enumValue.title || value;

	},

	processChoice(data, {fieldName, args = {}}) {

		let value = data[fieldName];
		let enumValues = MergeEditorActionHelper.getEnumList({fieldName: args.listName || fieldName});
		let newValue = value.other || enumValues && enumValues.find(el => el.enum === value.choice).title || value.choice;

		return newValue;

	},

	processChoices(data, {fieldName, args = {}}) {

		let value = data[fieldName];
		let enumValues = MergeEditorActionHelper.getEnumList({fieldName: args.listName || fieldName});
		let newValue = value.other || getEnumValues({enums: enumValues, list: value.choices}) || value.choices;

		return newValue;

	},

	convertArrayToObject(data, {fieldName}) {
		let value = data[fieldName];
		let newValue = {};
		value.forEach((el, index) => {
			newValue[index] = el;
		});
		return newValue;
	},

	fetchCategory(data, {cid, isYours, fieldName, dispatch}) {

		fetchCategories().then((categories) => {
			let value = createCategoryStr(data, {categories});
			let yoursOrTheirs = isYours ? 'yoursField' : 'theirsField';

			dispatch({
				type: actionTypes.DATA_PROCESSED,
				data: {
					cid,
					[yoursOrTheirs]: { name: fieldName, value }
				}
			});

		});
	},

	fetchLMVersion(data, {cid, dispatch, isYours}) {

		let {licensingVersion, vendorId} = data;
		let yoursOrTheirs = isYours ? 'yoursField' : 'theirsField';

		if (licensingVersion) {
			fetchVersion({licensingVersion, vendorId}).then(response => {
				dispatch({
					type: actionTypes.DATA_PROCESSED,
					data: {
						cid,
						[yoursOrTheirs]: {
							name: 'licensingVersion',
							value: response.name
						}
					}
				});
			});
		}

	},

	parseDate(data, {fieldName}) {

		let date = data[fieldName];
		return date && moment(date, DATE_FORMAT).format(DATE_FORMAT);

	}

};

export default MergeEditorActionHelper;

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
import {connect} from 'react-redux';

import i18n from 'nfvo-utils/i18n/i18n.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import TabulatedEditor from 'src/nfvo-components/editor/TabulatedEditor.jsx';

import {enums, screenTypes} from 'sdc-app/onboarding/OnboardingConstants.js';

import {onboardingMethod as onboardingMethodTypes, onboardingOriginTypes} from './SoftwareProductConstants.js';
import SoftwareProductActionHelper from './SoftwareProductActionHelper.js';
import SoftwareProductComponentsActionHelper from './components/SoftwareProductComponentsActionHelper.js';
import PermissionsActionHelper from './../permissions/PermissionsActionHelper.js';
import RevisionsActionHelper from './../revisions/RevisionsActionHelper.js';
import HeatSetupActionHelper from './attachments/setup/HeatSetupActionHelper.js';
import { actionsEnum as versionControllerActions } from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {modalContentMapper} from 'sdc-app/common/modal/ModalContentMapper.js';
import {CommitModalType} from 'nfvo-components/panel/versionController/components/CommitCommentModal.jsx';
import {onboardingMethod as onboardingMethodType} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import {SyncStates} from 'sdc-app/common/merge/MergeEditorConstants.js';
import {catalogItemStatuses} from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogConstants.js';

function getActiveNavigationId(screen, componentId) {
	let activeItemId = componentId ? screen + '|' + componentId : screen;
	return activeItemId;
}

const buildComponentNavigationBarGroups = ({componentId, meta}) => {
	const groups = ([
		{
			id: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL + '|' + componentId,
			name: i18n('General'),
			disabled: false,
			meta
		}, {
			id: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE + '|' + componentId,
			name: i18n('Compute'),
			disabled: false,
			meta
		}, {
			id: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING + '|' + componentId,
			name: i18n('High Availability & Load Balancing'),
			disabled: false,
			meta
		}, {
			id: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK + '|' + componentId,
			name: i18n('Networks'),
			disabled: false,
			meta
		}, {
			id: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE + '|' + componentId,
			name: i18n('Storage'),
			disabled: false,
			meta
		}, {
			id: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES + '|' + componentId,
			name: i18n('Images'),
			disabled: false,
			meta
		}, {
			id: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES + '|' + componentId,
			name: i18n('Process Details'),
			disabled: false,
			meta
		}, {
			id: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING + '|' + componentId,
			name: i18n('Monitoring'),
			disabled: false,
			meta
		}
	]);

	return groups;
};

const buildNavigationBarProps = ({softwareProduct, meta, screen, componentId, componentsList, mapOfExpandedIds}) => {
	const {softwareProductEditor: {data: currentSoftwareProduct = {}}} = softwareProduct;
	const {id, name, onboardingMethod, candidateOnboardingOrigin} = currentSoftwareProduct;
	const groups = [{
		id: id,
		name: name,
		items: [
			{
				id: enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE,
				name: i18n('Overview'),
				disabled: false,
				meta
			}, {
				id: enums.SCREEN.SOFTWARE_PRODUCT_DETAILS,
				name: i18n('General'),
				disabled: false,
				meta
			},
			{
				id: enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT,
				name: i18n('Deployment Flavors'),
				disabled: false,
				hidden: onboardingMethod !== onboardingMethodTypes.MANUAL,
				meta
			}, {
				id: enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES,
				name: i18n('Process Details'),
				disabled: false,
				meta
			}, {
				id: enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS,
				name: i18n('Networks'),
				disabled: false,
				meta
			}, {
				id: enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS,
				name: i18n('Attachments'),
				disabled: false,
				hidden: candidateOnboardingOrigin === onboardingOriginTypes.NONE,
				meta
			}, {
				id: enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG,
				name: i18n('Activity Log'),
				disabled: false,
				meta
			}, {
				id: enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES,
				name: i18n('Component Dependencies'),
				hidden: componentsList.length <= 1,
				disabled: false,
				meta
			}, {
				id: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS,
				name: i18n('Components'),
				hidden: componentsList.length <= 0,
				meta,
				expanded: mapOfExpandedIds[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS] === true && screen !== enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE,
				items: [
					...componentsList.map(({id, displayName}) => ({
						id: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS + '|' + id,
						name: displayName,
						meta,
						expanded: mapOfExpandedIds[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS + '|' + id] === true  && screen !== enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE,
						items: buildComponentNavigationBarGroups({componentId: id, meta})
					}))
				]
			}
		]
	}];
	let activeItemId = getActiveNavigationId(screen, componentId);
	return {
		activeItemId, groups
	};
};

const buildVersionControllerProps = ({softwareProduct, versions, currentVersion, permissions, userInfo, usersList, itemPermission, isReadOnlyMode}) => {
	const {softwareProductEditor = {data: {}}} = softwareProduct;
	const {isValidityData = true, data: {name, onboardingMethod}} = softwareProductEditor;

	return {
		version: currentVersion,
		viewableVersions: versions,
		isFormDataValid: isValidityData,
		permissions,
		itemName: name,
		itemPermission,
		isReadOnlyMode,
		userInfo,
		usersList,
		isManual: onboardingMethod === onboardingMethodType.MANUAL
	};
};

function buildMeta({softwareProduct, componentId, softwareProductDependencies, isReadOnlyMode}) {
	const {softwareProductEditor, softwareProductComponents, softwareProductQuestionnaire, softwareProductAttachments} = softwareProduct;
	const {data: currentSoftwareProduct = {}} = softwareProductEditor;
	const {version, onboardingOrigin, candidateOnboardingOrigin} = currentSoftwareProduct;
	const {qdata} = softwareProductQuestionnaire;
	const {heatSetup, heatSetupCache} = softwareProductAttachments;
	let currentComponentMeta = {};
	if(componentId) {
		const {componentEditor: {data: componentData = {} , qdata: componentQdata}} = softwareProductComponents;
		currentComponentMeta = {componentData, componentQdata};
	}
	const meta = {softwareProduct: currentSoftwareProduct, qdata, version, onboardingOrigin, candidateOnboardingOrigin, heatSetup, heatSetupCache,
		isReadOnlyMode, currentComponentMeta, softwareProductDependencies};
	return meta;
}

const mapStateToProps = (
	{
		softwareProduct,
		users: {usersList, userInfo},
		versionsPage: {versionsList: {versions}, permissions}
	},
	{
		currentScreen: {screen, itemPermission, props: {version: currentVersion, componentId, isReadOnlyMode}}
	}
) => {
	const {softwareProductEditor, softwareProductComponents, softwareProductDependencies} = softwareProduct;
	const {mapOfExpandedIds = []} = softwareProductEditor;
	const {componentsList = []} = softwareProductComponents;

	const meta = buildMeta({softwareProduct, componentId, softwareProductDependencies, isReadOnlyMode});
	return {
		versionControllerProps: buildVersionControllerProps({
			softwareProduct,
			versions,
			currentVersion,
			userInfo,
			usersList,
			permissions,
			itemPermission: {...itemPermission, isDirty: true},
			isReadOnlyMode
		}),
		navigationBarProps: buildNavigationBarProps({softwareProduct, meta, screen, componentId, componentsList, mapOfExpandedIds}),
		meta
	};
};

const autoSaveBeforeNavigate = ({dispatch, screen, softwareProductId, version, componentId,
		meta: {isReadOnlyMode, softwareProduct, qdata,
		currentComponentMeta: {componentData, componentQdata}}}) => {
	let promise;
	if (isReadOnlyMode) {
		promise = Promise.resolve();
	} else {
		switch(screen) {
			case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
				promise = SoftwareProductActionHelper.updateSoftwareProduct(dispatch, {softwareProduct, version, qdata});
				break;
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
				promise = SoftwareProductComponentsActionHelper.updateSoftwareProductComponent(dispatch,
					{softwareProductId, version, vspComponentId: componentId, componentData, qdata: componentQdata});
				break;
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
				promise = SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, version, vspComponentId: componentId, qdata: componentQdata});
				break;
			default:
				promise = Promise.resolve();
				break;
		}
	}
	return promise;
};


const mapActionsToProps = (dispatch, {currentScreen: {screen, props: {softwareProductId, licenseModelId, version, componentId: currentComponentId}}}) => {

	const props = {
		onVersionSwitching: (versionToSwitch, meta) => {
			ScreensHelper.loadScreen(dispatch, {screen: enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE, screenType: screenTypes.SOFTWARE_PRODUCT,
				props: {softwareProductId: meta.softwareProduct.id, version: versionToSwitch}});
		},
		onOpenPermissions: ({users}) => {
			return PermissionsActionHelper.fetchItemUsers(dispatch, {itemId: softwareProductId, allUsers: users});
		},
		onOpenRevisionsModal: () => {
			return RevisionsActionHelper.openRevisionsView(dispatch, {itemId: softwareProductId, version: version, itemType: screenTypes.SOFTWARE_PRODUCT});
		},
		onOpenCommentCommitModal: ({onCommit, title}) => dispatch({
			type: modalActionTypes.GLOBAL_MODAL_SHOW,
			data: {
				modalComponentName: modalContentMapper.COMMIT_COMMENT,
				modalComponentProps: {
					onCommit,
					type: CommitModalType.COMMIT
				},
				title
			}
		}),
		onMoreVersionsClick: ({itemName, users}) => {
			ScreensHelper.loadScreen(dispatch, {screen: enums.SCREEN.SOFTWARE_PRODUCT_VERSIONS_PAGE, screenType: screenTypes.SOFTWARE_PRODUCT,
				props: {softwareProductId, softwareProduct: {name: itemName, vendorId: licenseModelId}, usersList: users}});

		},
		onToggle: (groups, itemIdToExpand) => groups.map(({items}) => SoftwareProductActionHelper.toggleNavigationItems(dispatch, {items, itemIdToExpand})),
		onNavigate: ({id, meta, newVersion}) => {
			let navigationVersion = newVersion || version;
			let {onboardingOrigin, candidateOnboardingOrigin, heatSetup, heatSetupCache} = meta;
			let heatSetupPopupPromise = screen === enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS ?
								HeatSetupActionHelper.heatSetupLeaveConfirmation(dispatch, {softwareProductId, heatSetup, heatSetupCache}) :
								Promise.resolve();
			let preNavigate = meta ? autoSaveBeforeNavigate({dispatch, screen, meta, version, softwareProductId, componentId: currentComponentId}) : Promise.resolve();
			version = version || (meta ? meta.version : undefined);
			Promise.all([preNavigate, heatSetupPopupPromise]).then(() => {
				let [nextScreen, nextComponentId] = id.split('|');
				if(nextScreen === enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS && nextComponentId && nextComponentId === currentComponentId) {
					ScreensHelper.loadScreen(dispatch, {
						screen: nextScreen, screenType: screenTypes.SOFTWARE_PRODUCT,
						props: {softwareProductId, version: navigationVersion}
					});
				}
				else {
					if(nextScreen === enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS) {
						if(onboardingOrigin === onboardingOriginTypes.ZIP || candidateOnboardingOrigin === onboardingOriginTypes.ZIP) {
							nextScreen = enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS_SETUP;
						}
						else if(onboardingOrigin === onboardingOriginTypes.CSAR) {
							nextScreen = enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS_VALIDATION;
						}
					}
					ScreensHelper.loadScreen(dispatch, {
						screen: nextScreen, screenType: screenTypes.SOFTWARE_PRODUCT,
						props: {softwareProductId, version: navigationVersion, componentId: nextComponentId}
					});
				}
			}).catch((e) => {console.error(e);});
		}
	};

	switch (screen) {
		case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
		case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS:
		case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
		case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
		case enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES:
		case enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG:
		case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:
		case enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT:
		case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
		case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
			props.onSave = () => Promise.resolve();
			break;
		default:
			props.onSave = ({softwareProduct, qdata}) => SoftwareProductActionHelper.updateSoftwareProduct(dispatch, {softwareProduct, qdata, version});
			break;
	}


	props.onVersionControllerAction = (action, version, comment, meta) => {
		let {heatSetup, heatSetupCache} = meta;
		let autoSavePromise = meta ? autoSaveBeforeNavigate({dispatch, screen, meta, version, softwareProductId, componentId: currentComponentId}) : Promise.resolve();
		let heatSetupPopupPromise = screen === enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS && action === versionControllerActions.COMMIT ?
								HeatSetupActionHelper.heatSetupLeaveConfirmation(dispatch, {softwareProductId, heatSetup, heatSetupCache}) :
								Promise.resolve();
		Promise.all([autoSavePromise, heatSetupPopupPromise]).then(() => {
			return SoftwareProductActionHelper.performVCAction(dispatch, {softwareProductId, action, version, comment, meta}).then(updatedVersion => {
				const inMerge = updatedVersion && updatedVersion.state && updatedVersion.state.synchronizationState === SyncStates.MERGE;
				if((action === versionControllerActions.SYNC && !inMerge) ||
					 ((action === versionControllerActions.COMMIT || action === versionControllerActions.SYNC) && updatedVersion.status === catalogItemStatuses.CERTIFIED)) {
					ScreensHelper.loadLandingScreen(dispatch, {previousScreenName: screen, props: {softwareProductId, version: updatedVersion}});

				} else {
					ScreensHelper.loadScreen(dispatch, {screen, screenType: screenTypes.SOFTWARE_PRODUCT,
						props: {softwareProductId, version: updatedVersion, componentId: currentComponentId}});
				}
			});
		}).catch((e) => {console.error(e);});
	};

	props.onManagePermissions = () => PermissionsActionHelper.openPermissonsManager(dispatch, {itemId: softwareProductId, askForRights: false});
	return props;
};

export default connect(mapStateToProps, mapActionsToProps)(TabulatedEditor);

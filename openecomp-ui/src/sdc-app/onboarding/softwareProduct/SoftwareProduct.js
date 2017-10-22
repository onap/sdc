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
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import TabulatedEditor from 'src/nfvo-components/editor/TabulatedEditor.jsx';

import {enums} from 'sdc-app/onboarding/OnboardingConstants.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';

import {navigationItems, mapScreenToNavigationItem, onboardingMethod as onboardingMethodTypes, onboardingOriginTypes} from './SoftwareProductConstants.js';
import SoftwareProductActionHelper from './SoftwareProductActionHelper.js';
import SoftwareProductComponentsActionHelper from './components/SoftwareProductComponentsActionHelper.js';
import SoftwareProductDependenciesActionHelper from './dependencies/SoftwareProductDependenciesActionHelper.js';

import HeatSetupActionHelper from './attachments/setup/HeatSetupActionHelper.js';
import { actionsEnum as versionControllerActions } from 'nfvo-components/panel/versionController/VersionControllerConstants.js';

function getActiveNavigationId(screen, componentId) {
	let activeItemId = componentId ? mapScreenToNavigationItem[screen] + '|' + componentId : mapScreenToNavigationItem[screen];
	return activeItemId;
}

const buildComponentNavigationBarGroups = ({componentId, meta}) => {
	const groups = ([
		{
			id: navigationItems.GENERAL + '|' + componentId,
			name: i18n('General'),
			disabled: false,
			meta
		}, {
			id: navigationItems.COMPUTE + '|' + componentId,
			name: i18n('Compute'),
			disabled: false,
			meta
		}, {
			id: navigationItems.LOAD_BALANCING + '|' + componentId,
			name: i18n('High Availability & Load Balancing'),
			disabled: false,
			meta
		}, {
			id: navigationItems.NETWORKS + '|' + componentId,
			name: i18n('Networks'),
			disabled: false,
			meta
		}, {
			id: navigationItems.STORAGE + '|' + componentId,
			name: i18n('Storage'),
			disabled: false,
			meta
		}, {
			id: navigationItems.IMAGES + '|' + componentId,
			name: i18n('Images'),
			disabled: false,
			meta
		}, {
			id: navigationItems.PROCESS_DETAILS + '|' + componentId,
			name: i18n('Process Details'),
			disabled: false,
			meta
		}, {
			id: navigationItems.MONITORING + '|' + componentId,
			name: i18n('Monitoring'),
			disabled: false,
			meta
		}
	]);

	return groups;
};

const buildNavigationBarProps = ({softwareProduct, meta, screen, componentId, componentsList, mapOfExpandedIds}) => {
	const {softwareProductEditor: {data: currentSoftwareProduct = {}}} = softwareProduct;
	const {id, name, onboardingMethod, onboardingOrigin} = currentSoftwareProduct;
	const groups = [{
		id: id,
		name: name,
		items: [
			{
				id: navigationItems.VENDOR_SOFTWARE_PRODUCT,
				name: i18n('Overview'),
				disabled: false,
				meta
			}, {
				id: navigationItems.GENERAL,
				name: i18n('General'),
				disabled: false,
				meta
			},
			{
				id: navigationItems.DEPLOYMENT_FLAVORS,
				name: i18n('Deployment Flavors'),
				disabled: false,
				hidden: onboardingMethod !== onboardingMethodTypes.MANUAL,
				meta
			}, {
				id: navigationItems.PROCESS_DETAILS,
				name: i18n('Process Details'),
				disabled: false,
				meta
			}, {
				id: navigationItems.NETWORKS,
				name: i18n('Networks'),
				disabled: false,
				meta
			}, {
				id: navigationItems.ATTACHMENTS,
				name: i18n('Attachments'),
				disabled: false,
				hidden: onboardingOrigin === onboardingOriginTypes.NONE,
				meta
			}, {
				id: navigationItems.ACTIVITY_LOG,
				name: i18n('Activity Log'),
				disabled: false,
				meta
			}, {
				id: navigationItems.DEPENDENCIES,
				name: i18n('Component Dependencies'),
				hidden: componentsList.length <= 1,
				disabled: false,
				meta
			}, {
				id: navigationItems.COMPONENTS,
				name: i18n('Components'),
				hidden: componentsList.length <= 0,
				meta,
				expanded: mapOfExpandedIds[navigationItems.COMPONENTS] === true && screen !== enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE,
				items: [
					...componentsList.map(({id, displayName}) => ({
						id: navigationItems.COMPONENTS + '|' + id,
						name: displayName,
						meta,
						expanded: mapOfExpandedIds[navigationItems.COMPONENTS + '|' + id] === true  && screen !== enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE,
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

const buildVersionControllerProps = (softwareProduct) => {
	const {softwareProductEditor} = softwareProduct;
	const {data: currentSoftwareProduct = {}, isValidityData = true} = softwareProductEditor;

	const {version, viewableVersions, status: currentStatus, lockingUser} = currentSoftwareProduct;
	const {status, isCheckedOut} = VersionControllerUtils.getCheckOutStatusKindByUserID(currentStatus, lockingUser);

	return {
		status, isCheckedOut, version, viewableVersions,
		isFormDataValid: isValidityData
	};
};

function buildMeta({softwareProduct, componentId, softwareProductDependencies}) {
	const {softwareProductEditor, softwareProductComponents, softwareProductQuestionnaire, softwareProductAttachments} = softwareProduct;
	const {data: currentSoftwareProduct = {}} = softwareProductEditor;
	const {version, onboardingOrigin} = currentSoftwareProduct;
	const isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);
	const {qdata} = softwareProductQuestionnaire;
	const {heatSetup, heatSetupCache} = softwareProductAttachments;
	let currentComponentMeta = {};
	if(componentId) {
		const {componentEditor: {data: componentData = {} , qdata: componentQdata}} = softwareProductComponents;
		currentComponentMeta = {componentData, componentQdata};
	}
	const meta = {softwareProduct: currentSoftwareProduct, qdata, version, onboardingOrigin, heatSetup, heatSetupCache, isReadOnlyMode, currentComponentMeta, softwareProductDependencies};
	return meta;
}

const mapStateToProps = ({softwareProduct}, {currentScreen: {screen, props: {componentId}}}) => {
	const {softwareProductEditor, softwareProductComponents, softwareProductDependencies} = softwareProduct;
	const {mapOfExpandedIds = []} = softwareProductEditor;
	const {componentsList = []} = softwareProductComponents;

	const meta = buildMeta({softwareProduct, componentId, softwareProductDependencies});
	return {
		versionControllerProps: buildVersionControllerProps(softwareProduct),
		navigationBarProps: buildNavigationBarProps({softwareProduct, meta, screen, componentId, componentsList, mapOfExpandedIds}),
		meta
	};
};

const autoSaveBeforeNavigate = ({dispatch, screen, softwareProductId, componentId,
		meta: {isReadOnlyMode, softwareProduct, version, qdata, softwareProductDependencies,
		currentComponentMeta: {componentData, componentQdata}}}) => {
	let promise;
	if (isReadOnlyMode) {
		promise = Promise.resolve();
	} else {
		switch(screen) {
			case enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES:
				promise = SoftwareProductDependenciesActionHelper.saveDependencies(dispatch,{softwareProductId, version, dependenciesList: softwareProductDependencies});
			case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
				promise = SoftwareProductActionHelper.updateSoftwareProduct(dispatch, {softwareProduct, qdata});
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


const onComponentNavigate = (dispatch, {id, softwareProductId, version, currentComponentId}) => {
	const [nextScreen, nextComponentId] = id.split('|');
	switch(nextScreen) {
		case navigationItems.COMPONENTS:
			if(nextComponentId === currentComponentId) {
				OnboardingActionHelper.navigateToSoftwareProductComponents(dispatch, {softwareProductId});
			} else {
				OnboardingActionHelper.navigateToSoftwareProductComponentGeneral(dispatch, {softwareProductId, componentId: nextComponentId, version});
			}
			break;
		case navigationItems.GENERAL:
			OnboardingActionHelper.navigateToSoftwareProductComponentGeneral(dispatch, {softwareProductId, componentId: nextComponentId, version});
			break;
		case navigationItems.COMPUTE:
			OnboardingActionHelper.navigateToComponentCompute(dispatch, {softwareProductId, componentId: nextComponentId, version});
			break;
		case navigationItems.LOAD_BALANCING:
			OnboardingActionHelper.navigateToComponentLoadBalancing(dispatch, {softwareProductId, componentId: nextComponentId, version});
			break;
		case navigationItems.NETWORKS:
			OnboardingActionHelper.navigateToComponentNetwork(dispatch, {softwareProductId, componentId: nextComponentId, version});
			break;
		case navigationItems.IMAGES:
			OnboardingActionHelper.navigateToComponentImages(dispatch, {softwareProductId, componentId: nextComponentId, version});
			break;
		case navigationItems.STORAGE:
			OnboardingActionHelper.navigateToComponentStorage(dispatch, {softwareProductId, componentId: nextComponentId, version});
			break;
		case navigationItems.PROCESS_DETAILS:
			OnboardingActionHelper.navigateToSoftwareProductComponentProcesses(dispatch, {softwareProductId, componentId: nextComponentId, version});
			break;
		case navigationItems.MONITORING:
			OnboardingActionHelper.navigateToSoftwareProductComponentMonitoring(dispatch, {softwareProductId, componentId: nextComponentId, version});
			break;
	}
};

const mapActionsToProps = (dispatch, {currentScreen: {screen, props: {softwareProductId, componentId: currentComponentId}}}) => {

	const props = {
		onVersionSwitching: (version, meta) => {
			const screenToLoad = !currentComponentId ? screen : enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS;
			SoftwareProductActionHelper.fetchSoftwareProduct(dispatch, {softwareProductId, version});
			props.onNavigate({id: getActiveNavigationId(screenToLoad), meta, version});
		},
		onToggle: (groups, itemIdToExpand) => groups.map(({items}) => SoftwareProductActionHelper.toggleNavigationItems(dispatch, {items, itemIdToExpand})),
		onNavigate: ({id, meta, version}) => {
			let {onboardingOrigin, heatSetup, heatSetupCache} = meta;
			let heatSetupPopupPromise = screen === enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS ?
								HeatSetupActionHelper.heatSetupLeaveConfirmation(dispatch, {softwareProductId, heatSetup, heatSetupCache}) :
								Promise.resolve();
			let preNavigate = meta ? autoSaveBeforeNavigate({dispatch, screen, meta, softwareProductId, componentId: currentComponentId}) : Promise.resolve();
			version = version || (meta ? meta.version : undefined);
			Promise.all([preNavigate, heatSetupPopupPromise]).then(() => {
				switch(id) {
					case navigationItems.VENDOR_SOFTWARE_PRODUCT:
						OnboardingActionHelper.navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, version});
						break;
					case navigationItems.GENERAL:
						OnboardingActionHelper.navigateToSoftwareProductDetails(dispatch, {softwareProductId, version});
						break;
					case navigationItems.DEPLOYMENT_FLAVORS:
						OnboardingActionHelper.navigateToSoftwareProductDeployment(dispatch, {softwareProductId, version});
						break;
					case navigationItems.PROCESS_DETAILS:
						OnboardingActionHelper.navigateToSoftwareProductProcesses(dispatch, {softwareProductId, version});
						break;
					case navigationItems.NETWORKS:
						OnboardingActionHelper.navigateToSoftwareProductNetworks(dispatch, {softwareProductId, version});
						break;
					case navigationItems.DEPENDENCIES:
						OnboardingActionHelper.navigateToSoftwareProductDependencies(dispatch, {softwareProductId, version});
						break;
					case navigationItems.ATTACHMENTS:
						if(onboardingOrigin === onboardingOriginTypes.ZIP) {
							OnboardingActionHelper.navigateToSoftwareProductAttachmentsSetupTab(dispatch, {softwareProductId, version});
						}
						else if(onboardingOrigin === onboardingOriginTypes.CSAR) {
							OnboardingActionHelper.navigateToSoftwareProductAttachmentsValidationTab(dispatch, {softwareProductId, version});
						}
						break;
					case navigationItems.COMPONENTS:
						OnboardingActionHelper.navigateToSoftwareProductComponents(dispatch, {softwareProductId, version});
						break;
					case navigationItems.ACTIVITY_LOG:
						OnboardingActionHelper.navigateToSoftwareProductActivityLog(dispatch, {softwareProductId, version});
						break;
					default:
						onComponentNavigate(dispatch, {id, softwareProductId, version, screen, currentComponentId});
						break;
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
			props.onSave = ({softwareProduct, qdata}) => SoftwareProductActionHelper.updateSoftwareProduct(dispatch, {softwareProduct, qdata});
			break;
	}


	props.onVersionControllerAction = (action, version, meta) => {
		let {heatSetup, heatSetupCache} = meta;
		let heatSetupPopupPromise = screen === enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS && action === versionControllerActions.CHECK_IN ?
								HeatSetupActionHelper.heatSetupLeaveConfirmation(dispatch, {softwareProductId, heatSetup, heatSetupCache}) :
								Promise.resolve();
		heatSetupPopupPromise.then(() => {
			return SoftwareProductActionHelper.performVCAction(dispatch, {softwareProductId, action, version}).then(({newVersion}) => {
				//props.onNavigate({id: getActiveNavigationId(screen, currentComponentId), version});
				if(screen === enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG) {
					OnboardingActionHelper.navigateToSoftwareProductActivityLog(dispatch, {softwareProductId, version: newVersion});
				}
			});
		}).catch((e) => {console.error(e);});
	};
	return props;
};

export default connect(mapStateToProps, mapActionsToProps)(TabulatedEditor);

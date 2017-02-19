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

import {connect} from 'react-redux';

import i18n from 'nfvo-utils/i18n/i18n.js';
import {statusEnum as versionStatusEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import VersionControllerUtils from 'nfvo-components/panel/versionController/VersionControllerUtils.js';
import TabulatedEditor from 'src/nfvo-components/editor/TabulatedEditor.jsx';

import {enums} from 'sdc-app/onboarding/OnboardingConstants.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';

import {navigationItems} from './SoftwareProductConstants.js';
import SoftwareProductActionHelper from './SoftwareProductActionHelper.js';
import SoftwareProductComponentsActionHelper from './components/SoftwareProductComponentsActionHelper.js';

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
	const {id, name} = currentSoftwareProduct;
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
	let activeItemId = ({
		[enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE]: navigationItems.VENDOR_SOFTWARE_PRODUCT,
		[enums.SCREEN.SOFTWARE_PRODUCT_DETAILS]: navigationItems.GENERAL,
		[enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS]: navigationItems.ATTACHMENTS,
		[enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES]: navigationItems.PROCESS_DETAILS,
		[enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS]: navigationItems.NETWORKS,
		[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS]: navigationItems.COMPONENTS
	})[screen];

	if(componentId) {
		activeItemId =
			Object.keys(mapOfExpandedIds).length === 1 && mapOfExpandedIds[navigationItems.COMPONENTS] === true ?
			navigationItems.COMPONENTS : ({
				[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL]: navigationItems.GENERAL,
				[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE]: navigationItems.COMPUTE,
				[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING]: navigationItems.LOAD_BALANCING,
				[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK]: navigationItems.NETWORKS,
				[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE]: navigationItems.STORAGE,
				[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES]: navigationItems.PROCESS_DETAILS,
				[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING]: navigationItems.MONITORING
			})[screen] + '|' + componentId;
	}

	return {
		activeItemId, groups
	};
};

const buildVersionControllerProps = (softwareProduct) => {
	const {softwareProductEditor} = softwareProduct;
	const {data: currentSoftwareProduct = {}, isValidityData = true} = softwareProductEditor;

	const {version, viewableVersions, status: currentStatus, lockingUser} = currentSoftwareProduct;
	const {status, isCheckedOut} = (currentStatus === versionStatusEnum.CHECK_OUT_STATUS) ?
		VersionControllerUtils.getCheckOutStatusKindByUserID(currentStatus, lockingUser) :
		{status: currentStatus, isCheckedOut: false};

	return {
		status, isCheckedOut, version, viewableVersions,
		isFormDataValid: isValidityData
	};
};

const mapStateToProps = ({softwareProduct}, {currentScreen: {screen, props: {componentId}}}) => {
	const {softwareProductEditor, softwareProductComponents, softwareProductQuestionnaire} = softwareProduct;
	const {data: currentSoftwareProduct = {}, mapOfExpandedIds = []} = softwareProductEditor;
	const {version} = currentSoftwareProduct;
	const {componentsList = []} = softwareProductComponents;
	const isReadOnlyMode = VersionControllerUtils.isReadOnly(currentSoftwareProduct);
	const {qdata} = softwareProductQuestionnaire;
	let currentComponentMeta = {};
	if(componentId) {
		const {componentEditor: {data: componentData = {} , qdata: componentQdata}} = softwareProductComponents;
		currentComponentMeta = {componentData, componentQdata};
	}
	const meta = {softwareProduct: currentSoftwareProduct, qdata, version, isReadOnlyMode, currentComponentMeta};
	return {
		versionControllerProps: buildVersionControllerProps(softwareProduct),
		navigationBarProps: buildNavigationBarProps({softwareProduct, meta, screen, componentId, componentsList, mapOfExpandedIds})
	};
};

const autoSaveBeforeNavigate = ({dispatch, screen, softwareProductId, componentId, meta: {isReadOnlyMode, softwareProduct, qdata, currentComponentMeta: {componentData, componentQdata}}}) => {
	let promise;
	if (isReadOnlyMode) {
		promise = Promise.resolve();
	} else {
		switch(screen) {
			case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
				promise = SoftwareProductActionHelper.updateSoftwareProduct(dispatch, {softwareProduct, qdata});
				break;
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
				promise = SoftwareProductComponentsActionHelper.updateSoftwareProductComponent(dispatch, {softwareProductId, vspComponentId: componentId, componentData, qdata: componentQdata});
				break;
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
				promise = SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, vspComponentId: componentId, qdata: componentQdata});
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
			OnboardingActionHelper.navigateToComponentCompute(dispatch, {softwareProductId, componentId: nextComponentId});
			break;
		case navigationItems.LOAD_BALANCING:
			OnboardingActionHelper.navigateToComponentLoadBalancing(dispatch, {softwareProductId, componentId: nextComponentId});
			break;
		case navigationItems.NETWORKS:
			OnboardingActionHelper.navigateToComponentNetwork(dispatch, {softwareProductId, componentId: nextComponentId, version});
			break;
		case navigationItems.STORAGE:
			OnboardingActionHelper.navigateToComponentStorage(dispatch, {softwareProductId, componentId: nextComponentId});
			break;
		case navigationItems.PROCESS_DETAILS:
			OnboardingActionHelper.navigateToSoftwareProductComponentProcesses(dispatch, {softwareProductId, componentId: nextComponentId});
			break;
		case navigationItems.MONITORING:
			OnboardingActionHelper.navigateToSoftwareProductComponentMonitoring(dispatch, {softwareProductId, componentId: nextComponentId});
			break;
	}
};

const mapActionsToProps = (dispatch, {currentScreen: {screen, props: {softwareProductId, componentId: currentComponentId}}}) => {

	const props = {
		onClose: ({version}) => {
			if (screen === enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE) {
				OnboardingActionHelper.navigateToOnboardingCatalog(dispatch);
			} else {
				OnboardingActionHelper.navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, version});
			}
		},
		onVersionSwitching: (version) => {
			OnboardingActionHelper.navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, version});
		},
		onToggle: (groups, itemIdToExpand) => groups.map(({items}) => SoftwareProductActionHelper.toggleNavigationItems(dispatch, {items, itemIdToExpand})),
		onNavigate: ({id, meta}) => {
			let preNavigate = autoSaveBeforeNavigate({dispatch, screen, meta, softwareProductId, componentId: currentComponentId});
			preNavigate.then(() => {
				switch(id) {
					case navigationItems.VENDOR_SOFTWARE_PRODUCT:
						OnboardingActionHelper.navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, version: meta.version});
						break;
					case navigationItems.GENERAL:
						OnboardingActionHelper.navigateToSoftwareProductDetails(dispatch, {softwareProductId});
						break;
					case navigationItems.PROCESS_DETAILS:
						OnboardingActionHelper.navigateToSoftwareProductProcesses(dispatch, {softwareProductId, version: meta.version});
						break;
					case navigationItems.NETWORKS:
						OnboardingActionHelper.navigateToSoftwareProductNetworks(dispatch, {softwareProductId, version: meta.version});
						break;
					case navigationItems.ATTACHMENTS:
						OnboardingActionHelper.navigateToSoftwareProductAttachments(dispatch, {softwareProductId});
						break;
					case navigationItems.COMPONENTS:
						OnboardingActionHelper.navigateToSoftwareProductComponents(dispatch, {softwareProductId});
						break;
					default:
						onComponentNavigate(dispatch, {id, softwareProductId, version: meta.version, screen, currentComponentId});
						break;
				}
			});
		}
	};

	switch (screen) {
		case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
		case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS:
		case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
		case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
		case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:
		case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
		case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
			props.onSave = () => {
				return Promise.resolve();
			};
			break;
		default:
			props.onSave = ({softwareProduct, qdata}) => SoftwareProductActionHelper.updateSoftwareProduct(dispatch, {softwareProduct, qdata});
			break;
	}


	props.onVersionControllerAction = (action) =>
		SoftwareProductActionHelper.performVCAction(dispatch, {softwareProductId, action}).then(() => {
			SoftwareProductActionHelper.fetchSoftwareProduct(dispatch, {softwareProductId});
		});

	return props;
};

export default connect(mapStateToProps, mapActionsToProps)(TabulatedEditor);

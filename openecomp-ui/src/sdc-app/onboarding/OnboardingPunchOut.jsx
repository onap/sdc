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
import React from 'react';
import PropTypes from 'prop-types';
import ReactDOM from 'react-dom';
import {connect} from 'react-redux';
import isEqual from 'lodash/isEqual.js';
import objectValues from 'lodash/values.js';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Application from 'sdc-app/Application.jsx';
import store from 'sdc-app/AppStore.js';
import Configuration from 'sdc-app/config/Configuration.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';

import Onboard from './onboard/Onboard.js';
import VersionsPage from './versionsPage/VersionsPage.js';
import LicenseModel from './licenseModel/LicenseModel.js';
import LicenseModelOverview from './licenseModel/overview/LicenseModelOverview.js';
import ActivityLog from 'sdc-app/common/activity-log/ActivityLog.js';

import LicenseAgreementListEditor from './licenseModel/licenseAgreement/LicenseAgreementListEditor.js';
import FeatureGroupListEditor from './licenseModel/featureGroups/FeatureGroupListEditor.js';
import LicenseKeyGroupsListEditor from './licenseModel/licenseKeyGroups/LicenseKeyGroupsListEditor.js';
import EntitlementPoolsListEditor from './licenseModel/entitlementPools/EntitlementPoolsListEditor.js';
import SoftwareProduct from './softwareProduct/SoftwareProduct.js';
import SoftwareProductLandingPage  from './softwareProduct/landingPage/SoftwareProductLandingPage.js';
import SoftwareProductDetails  from './softwareProduct/details/SoftwareProductDetails.js';
import SoftwareProductAttachments from './softwareProduct/attachments/SoftwareProductAttachments.js';
import SoftwareProductProcesses from './softwareProduct/processes/SoftwareProductProcesses.js';
import SoftwareProductDeployment from './softwareProduct/deployment/SoftwareProductDeployment.js';
import SoftwareProductNetworks from './softwareProduct/networks/SoftwareProductNetworks.js';
import SoftwareProductDependencies from './softwareProduct/dependencies/SoftwareProductDependencies.js';

import SoftwareProductComponentsList from './softwareProduct/components/SoftwareProductComponents.js';
import SoftwareProductComponentProcessesList from './softwareProduct/components/processes/SoftwareProductComponentProcessesList.js';
import SoftwareProductComponentStorage from './softwareProduct/components/storage/SoftwareProductComponentStorage.js';
import SoftwareProductComponentsNetworkList from './softwareProduct/components/network/SoftwareProductComponentsNetworkList.js';
import SoftwareProductComponentsGeneral from './softwareProduct/components/general/SoftwareProductComponentsGeneral.js';
import SoftwareProductComponentsCompute from './softwareProduct/components/compute/SoftwareProductComponentCompute.js';
import SoftwareProductComponentLoadBalancing from './softwareProduct/components/loadBalancing/SoftwareProductComponentLoadBalancing.js';
import SoftwareProductComponentsImageList from './softwareProduct/components/images/SoftwareProductComponentsImageList.js';
import SoftwareProductComponentsMonitoring from './softwareProduct/components/monitoring/SoftwareProductComponentsMonitoring.js';
import {onboardingMethod as onboardingMethodTypes, onboardingOriginTypes} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';

import {itemTypes} from './versionsPage/VersionsPageConstants.js';

import HeatSetupActionHelper from './softwareProduct/attachments/setup/HeatSetupActionHelper.js';

import {actionTypes, enums, screenTypes} from './OnboardingConstants.js';
import OnboardingActionHelper from './OnboardingActionHelper.js';

class OnboardingView extends React.Component {
	static propTypes = {
		currentScreen: PropTypes.shape({
			screen: PropTypes.oneOf(objectValues(enums.SCREEN)).isRequired,
			props: PropTypes.object.isRequired,
			itemPermission: PropTypes.object
		}).isRequired
	};

	componentDidMount() {
		let element = ReactDOM.findDOMNode(this);
		element.addEventListener('click', event => {
			if (event.target.tagName === 'A') {
				event.preventDefault();
			}
		});
		['wheel', 'mousewheel', 'DOMMouseScroll'].forEach(eventType =>
			element.addEventListener(eventType, event => event.stopPropagation())
		);
	}

	render() {
		let {currentScreen} = this.props;
		let {screen, props} = currentScreen;

		return (
			<div className='dox-ui dox-ui-punch-out dox-ui-punch-out-full-page'>
				{(() => {
					switch (screen) {
						case enums.SCREEN.ONBOARDING_CATALOG:
							return <Onboard {...props}/>;
						case enums.SCREEN.VERSIONS_PAGE:
							return <VersionsPage {...props} />;

						case enums.SCREEN.LICENSE_AGREEMENTS:
						case enums.SCREEN.FEATURE_GROUPS:
						case enums.SCREEN.ENTITLEMENT_POOLS:
						case enums.SCREEN.LICENSE_KEY_GROUPS:
						case enums.SCREEN.LICENSE_MODEL_OVERVIEW:
						case enums.SCREEN.ACTIVITY_LOG:
							return (
								<LicenseModel currentScreen={currentScreen}>
									{
										(()=>{
											switch(screen) {
												case enums.SCREEN.LICENSE_MODEL_OVERVIEW:
													return <LicenseModelOverview {...props}/>;
												case enums.SCREEN.LICENSE_AGREEMENTS:
													return <LicenseAgreementListEditor {...props}/>;
												case enums.SCREEN.FEATURE_GROUPS:
													return <FeatureGroupListEditor {...props}/>;
												case enums.SCREEN.ENTITLEMENT_POOLS:
													return <EntitlementPoolsListEditor {...props}/>;
												case enums.SCREEN.LICENSE_KEY_GROUPS:
													return <LicenseKeyGroupsListEditor {...props}/>;
												case enums.SCREEN.ACTIVITY_LOG:
													return <ActivityLog {...props}/>;
											}
										})()
									}
								</LicenseModel>
							);

						case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
						case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
						case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS:
						case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
						case enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT:
						case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
						case enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
						case enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG:
							return (
								<SoftwareProduct currentScreen={currentScreen}>
									{
										(()=>{
											switch(screen) {
												case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
													return <SoftwareProductLandingPage {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
													return <SoftwareProductDetails {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS:
													return <SoftwareProductAttachments className='no-padding-content-area' {...props} />;
												case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
													return <SoftwareProductProcesses {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT:
													return <SoftwareProductDeployment {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
													return <SoftwareProductNetworks {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES:
													return <SoftwareProductDependencies {...props} />;
												case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:
													return <SoftwareProductComponentsList  {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
													return <SoftwareProductComponentProcessesList  {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
													return <SoftwareProductComponentStorage {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
													return <SoftwareProductComponentsNetworkList {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
													return <SoftwareProductComponentsGeneral{...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
													return <SoftwareProductComponentsCompute {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
													return <SoftwareProductComponentLoadBalancing{...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES:
													return <SoftwareProductComponentsImageList{...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
													return <SoftwareProductComponentsMonitoring {...props}/>;
												case enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG:
													return <ActivityLog {...props}/>;
											}
										})()
									}
								</SoftwareProduct>
							);
					}
				})()}
			</div>
		);
	}
}
const mapStateToProps = ({currentScreen}) => ({currentScreen});
let Onboarding = connect(mapStateToProps, null)(OnboardingView);

export default class OnboardingPunchOut {

	render({options: {data, apiRoot, apiHeaders}, onEvent}, element) {
		if (!this.unsubscribeFromStore) {
			this.unsubscribeFromStore = store.subscribe(() => this.handleStoreChange());
		}

		if (!this.isConfigSet) {
			Configuration.setATTApiRoot(apiRoot);
			Configuration.setATTApiHeaders(apiHeaders);
			this.isConfigSet = true;
		}

		this.onEvent = (...args) => onEvent(...args);
		this.handleData(data);

		if (!this.rendered) {
			ReactDOM.render(
				<Application>
					<Onboarding/>
				</Application>,
				element
			);
			this.rendered = true;
		}
	}

	unmount(element) {
		ReactDOM.unmountComponentAtNode(element);
		this.rendered = false;
		this.unsubscribeFromStore();
		this.unsubscribeFromStore = null;
	}

	handleData(data) {
		let {breadcrumbs: {selectedKeys = []} = {}} = data;
		let dispatch = action => store.dispatch(action);
		let {currentScreen, users: {usersList}, softwareProductList, licenseModelList, softwareProduct: {softwareProductEditor: {data: vspData = {}},
			softwareProductComponents = {}, softwareProductQuestionnaire = {}}} = store.getState();

		let {props: {version, isReadOnlyMode}, screen} = currentScreen;
		let {componentEditor: {data: componentData = {}, qdata: componentQData = {}}} = softwareProductComponents;
		if (this.programmaticBreadcrumbsUpdate) {
			this.prevSelectedKeys = selectedKeys;
			this.programmaticBreadcrumbsUpdate = false;
			return;
		}
		if (!isEqual(selectedKeys, this.prevSelectedKeys)) {
			this.breadcrumbsPrefixSelected = isEqual(selectedKeys, this.prevSelectedKeys && this.prevSelectedKeys.slice(0, selectedKeys.length));

			const [, screenType, prevVspId, , prevComponentId] = this.prevSelectedKeys || [];
			let preNavigate = Promise.resolve();
			if(screenType === enums.BREADCRUMS.SOFTWARE_PRODUCT && screen !== 'VERSIONS_PAGE' && !isReadOnlyMode) {
				let dataToSave = prevVspId ? prevComponentId ? {componentData, qdata: componentQData} : {softwareProduct: vspData, qdata: softwareProductQuestionnaire.qdata} : {};
				preNavigate = OnboardingActionHelper.autoSaveBeforeNavigate(dispatch, {
					softwareProductId: prevVspId,
					version,
					vspComponentId: prevComponentId,
					dataToSave
				});
			}

			let {currentScreen: {props: {softwareProductId}}, softwareProduct: {softwareProductAttachments: {heatSetup, heatSetupCache}}} = store.getState();
			let heatSetupPopupPromise = currentScreen.screen === enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS ?
				HeatSetupActionHelper.heatSetupLeaveConfirmation(dispatch, {softwareProductId, heatSetup, heatSetupCache}) :
				Promise.resolve();
			Promise.all([preNavigate, heatSetupPopupPromise]).then(() => {
				this.prevSelectedKeys = selectedKeys;
				if (selectedKeys.length === 0) {
					ScreensHelper.loadScreen(dispatch, {screen: enums.SCREEN.ONBOARDING_CATALOG});

				} else if (selectedKeys.length === 1 || selectedKeys[1] === enums.BREADCRUMS.LICENSE_MODEL) {
					let [licenseModelId, , licenseModelScreen] = selectedKeys;
					let licenseModel = licenseModelList.find(vlm => vlm.id === licenseModelId);
					ScreensHelper.loadScreen(dispatch, {screen: licenseModelScreen, screenType: screenTypes.LICENSE_MODEL,
						props: {licenseModelId, version, licenseModel, usersList}});

				} else if (selectedKeys.length <= 4 && selectedKeys[1] === enums.BREADCRUMS.SOFTWARE_PRODUCT) {
					let [licenseModelId, , softwareProductId, softwareProductScreen] = selectedKeys;
					let softwareProduct = softwareProductId ?
						softwareProductList.find(({id}) => id === softwareProductId) :
						softwareProductList.find(({vendorId}) => vendorId === licenseModelId);
					if (!softwareProductId) {
						softwareProductId = softwareProduct.id;
					}
					if (softwareProductScreen === enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS) {
						softwareProduct = vspData;
						//check current vsp fields to determine which file has uploaded
						if(vspData.onboardingOrigin === onboardingOriginTypes.ZIP || vspData.candidateOnboardingOrigin === onboardingOriginTypes.ZIP) {
							softwareProductScreen = enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS_SETUP;
						}
						else if(vspData.onboardingOrigin === onboardingOriginTypes.CSAR) {
							softwareProductScreen = enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS_VALIDATION;
						}
					}

					ScreensHelper.loadScreen(dispatch, {screen: softwareProductScreen, screenType: screenTypes.SOFTWARE_PRODUCT,
						props: {softwareProductId, softwareProduct, version, usersList}});

				} else if (selectedKeys.length === 5 && selectedKeys[1] === enums.BREADCRUMS.SOFTWARE_PRODUCT && selectedKeys[3] === enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS) {
					let [licenseModelId, , softwareProductId, , componentId] = selectedKeys;
					let softwareProduct = softwareProductId ?
						softwareProductList.find(({id}) => id === softwareProductId) :
						softwareProductList.find(({vendorId}) => vendorId === licenseModelId);
					if (!softwareProductId) {
						softwareProductId = softwareProduct.id;
					}
					ScreensHelper.loadScreen(dispatch, {screen: enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS, screenType: screenTypes.SOFTWARE_PRODUCT,
						props: {softwareProductId, softwareProduct, componentId, version, usersList}});

				} else if (selectedKeys.length === 6 && selectedKeys[1] === enums.BREADCRUMS.SOFTWARE_PRODUCT && selectedKeys[3] === enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS) {
					let [licenseModelId, , softwareProductId, , componentId, componentScreen] = selectedKeys;
					let softwareProduct = softwareProductId ?
						softwareProductList.find(({id}) => id === softwareProductId) :
						softwareProductList.find(({vendorId}) => vendorId === licenseModelId);
					if (!softwareProductId) {
						softwareProductId = softwareProduct.id;
					}
					ScreensHelper.loadScreen(dispatch, {screen: componentScreen, screenType: screenTypes.SOFTWARE_PRODUCT,
						props: {softwareProductId, softwareProduct, componentId, version, usersList}});

				} else {
					console.error('Unknown breadcrumbs path: ', selectedKeys);
				}
			}).catch(() => {
				store.dispatch({
					type: actionTypes.SET_CURRENT_SCREEN,
					currentScreen: {
						...currentScreen,
						forceBreadCrumbsUpdate: true
					}
				});
			});
		}
	}

	handleStoreChange() {
		let {currentScreen, licenseModelList, finalizedLicenseModelList, softwareProductList, finalizedSoftwareProductList, versionsPage:
			{versionsList: {itemType, itemId}},
			softwareProduct: {softwareProductEditor: {data: currentSoftwareProduct = {onboardingMethod: ''}},
				softwareProductComponents: {componentsList}}} = store.getState();
		const wholeSoftwareProductList = [...softwareProductList, ...finalizedSoftwareProductList];
		const wholeLicenseModelList = [...licenseModelList, ...finalizedLicenseModelList];
		let breadcrumbsData = {itemType, itemId, currentScreen, wholeLicenseModelList, wholeSoftwareProductList, currentSoftwareProduct, componentsList};

		if (currentScreen.forceBreadCrumbsUpdate || !isEqual(breadcrumbsData, this.prevBreadcrumbsData) || this.breadcrumbsPrefixSelected) {
			this.prevBreadcrumbsData = breadcrumbsData;
			this.breadcrumbsPrefixSelected = false;
			this.programmaticBreadcrumbsUpdate = true;
			let breadcrumbs = this.buildBreadcrumbs(breadcrumbsData);
			this.onEvent('breadcrumbsupdated', breadcrumbs);
			store.dispatch({
				type: actionTypes.SET_CURRENT_SCREEN,
				currentScreen: {
					...currentScreen,
					forceBreadCrumbsUpdate: false
				}
			});
		}
	}

	buildBreadcrumbs({currentScreen: {screen, props}, itemType, itemId, currentSoftwareProduct,
		wholeLicenseModelList, wholeSoftwareProductList, componentsList}) {
		let {onboardingMethod, onboardingOrigin, candidateOnboardingOrigin} = currentSoftwareProduct;
		let screenToBreadcrumb;
		switch (screen) {
			case enums.SCREEN.ONBOARDING_CATALOG:
				return [];

			case enums.SCREEN.VERSIONS_PAGE:
				let firstMenuItems = itemType === itemTypes.LICENSE_MODEL ? [
					{
						selectedKey: itemId,
						menuItems: wholeLicenseModelList.map(({id, name}) => ({
							key: id,
							displayText: name
						}))
					}] : [
						{
							selectedKey: props.additionalProps.licenseModelId || currentSoftwareProduct.vendorId,
							menuItems: wholeLicenseModelList.map(({id, name}) => ({
								key: id,
								displayText: name
							}))
						},
						{
							selectedKey: enums.BREADCRUMS.SOFTWARE_PRODUCT,
							menuItems: [{
								key: enums.BREADCRUMS.LICENSE_MODEL,
								displayText: i18n('License Model')
							}, {
								key: enums.BREADCRUMS.SOFTWARE_PRODUCT,
								displayText: i18n('Software Products')
							}]
						},
						{
							selectedKey: itemId,
							menuItems: wholeSoftwareProductList
								.filter(({id, vendorId}) => vendorId === currentSoftwareProduct.vendorId || id === itemId)
								.map(({id, name}) => ({
									key: id,
									displayText: name
								}))
						},
					];
				return [
					...firstMenuItems,
					{
						selectedKey: enums.BREADCRUMS.VERSIONS_PAGE,
						menuItems: [{key: enums.BREADCRUMS.VERSIONS_PAGE, displayText: i18n('Versions Page')}]
					}
				];

			case enums.SCREEN.LICENSE_AGREEMENTS:
			case enums.SCREEN.FEATURE_GROUPS:
			case enums.SCREEN.ENTITLEMENT_POOLS:
			case enums.SCREEN.LICENSE_KEY_GROUPS:
			case enums.SCREEN.LICENSE_MODEL_OVERVIEW:
			case enums.SCREEN.ACTIVITY_LOG:
				screenToBreadcrumb = {
					[enums.SCREEN.LICENSE_AGREEMENTS]: enums.BREADCRUMS.LICENSE_AGREEMENTS,
					[enums.SCREEN.FEATURE_GROUPS]: enums.BREADCRUMS.FEATURE_GROUPS,
					[enums.SCREEN.ENTITLEMENT_POOLS]: enums.BREADCRUMS.ENTITLEMENT_POOLS,
					[enums.SCREEN.LICENSE_KEY_GROUPS]: enums.BREADCRUMS.LICENSE_KEY_GROUPS,
					[enums.SCREEN.LICENSE_MODEL_OVERVIEW]: enums.BREADCRUMS.LICENSE_MODEL_OVERVIEW,
					[enums.SCREEN.ACTIVITY_LOG]: enums.BREADCRUMS.ACTIVITY_LOG
				};
				return [
					{
						selectedKey: props.licenseModelId,
						menuItems: wholeLicenseModelList.map(({id, name}) => ({
							key: id,
							displayText: name
						}))
					},
					{
						selectedKey: enums.BREADCRUMS.LICENSE_MODEL,
						menuItems: [{
							key: enums.BREADCRUMS.LICENSE_MODEL,
							displayText: i18n('License Model')
						},
						...(wholeSoftwareProductList.findIndex(({vendorId}) => vendorId === props.licenseModelId) === -1 ? [] : [{
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT,
							displayText: i18n('Software Products')
						}])]
					}, {
						selectedKey: screenToBreadcrumb[screen],
						menuItems: [{
							key: enums.BREADCRUMS.LICENSE_MODEL_OVERVIEW,
							displayText: i18n('Overview')
						},{
							key: enums.BREADCRUMS.LICENSE_AGREEMENTS,
							displayText: i18n('License Agreements')
						}, {
							key: enums.BREADCRUMS.FEATURE_GROUPS,
							displayText: i18n('Feature Groups')
						}, {
							key: enums.BREADCRUMS.ENTITLEMENT_POOLS,
							displayText: i18n('Entitlement Pools')
						}, {
							key: enums.BREADCRUMS.LICENSE_KEY_GROUPS,
							displayText: i18n('License Key Groups')
						}, {
							key: enums.BREADCRUMS.ACTIVITY_LOG,
							displayText: i18n('Activity Log')
						}]
					}
				];

			case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
			case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
			case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS:
			case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
			case enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT:
			case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
			case enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES:
			case enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:

			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
				screenToBreadcrumb = {
					[enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE]: enums.BREADCRUMS.SOFTWARE_PRODUCT_LANDING_PAGE,
					[enums.SCREEN.SOFTWARE_PRODUCT_DETAILS]: enums.BREADCRUMS.SOFTWARE_PRODUCT_DETAILS,
					[enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS]: enums.BREADCRUMS.SOFTWARE_PRODUCT_ATTACHMENTS,
					[enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES]: enums.BREADCRUMS.SOFTWARE_PRODUCT_PROCESSES,
					[enums.SCREEN.SOFTWARE_PRODUCT_DEPLOYMENT]: enums.BREADCRUMS.SOFTWARE_PRODUCT_DEPLOYMENT,
					[enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS]: enums.BREADCRUMS.SOFTWARE_PRODUCT_NETWORKS,
					[enums.SCREEN.SOFTWARE_PRODUCT_DEPENDENCIES]: enums.BREADCRUMS.SOFTWARE_PRODUCT_DEPENDENCIES,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS,
					[enums.SCREEN.SOFTWARE_PRODUCT_ACTIVITY_LOG]: enums.BREADCRUMS.SOFTWARE_PRODUCT_ACTIVITY_LOG
				};
				let componentScreenToBreadcrumb = {
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_PROCESSES,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_COMPUTE,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_STORAGE,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_NETWORK,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_GENERAL,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_IMAGES]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_IMAGES,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_MONITORING
				};
				let licenseModelId = currentSoftwareProduct.vendorId;
				let returnedBreadcrumb = [
					{
						selectedKey: licenseModelId,
						menuItems: wholeLicenseModelList.map(({id, name}) => ({
							key: id,
							displayText: name
						}))
					},
					{
						selectedKey: enums.BREADCRUMS.SOFTWARE_PRODUCT,
						menuItems: [{
							key: enums.BREADCRUMS.LICENSE_MODEL,
							displayText: i18n('License Model')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT,
							displayText: i18n('Software Products')
						}]
					},
					{
						selectedKey: props.softwareProductId,
						menuItems: wholeSoftwareProductList
							.filter(({vendorId, id}) => vendorId === licenseModelId || id === props.softwareProductId)
							.map(({id, name}) => ({
								key: id,
								displayText: name
							}))
					},
					...(/*screen === enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE ? [] :*/ [{
						selectedKey: screenToBreadcrumb[screen] || enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS,
						menuItems: [{
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_LANDING_PAGE,
							displayText: i18n('Overview')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_DETAILS,
							displayText: i18n('General')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_DEPLOYMENT,
							displayText: i18n('Deployment Flavors')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_PROCESSES,
							displayText: i18n('Process Details')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_NETWORKS,
							displayText: i18n('Networks')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_DEPENDENCIES,
							displayText: i18n('Components Dependencies')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_ATTACHMENTS,
							displayText: i18n('Attachments')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_ACTIVITY_LOG,
							displayText: i18n('Activity Log')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS,
							displayText: i18n('Components')
						}].filter(item => {
							switch (item.key) {
								case enums.BREADCRUMS.SOFTWARE_PRODUCT_ATTACHMENTS:
									let isHeatData = onboardingOrigin !== onboardingOriginTypes.NONE || candidateOnboardingOrigin === onboardingOriginTypes.ZIP;
									return isHeatData;
								case enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS:
									return (componentsList.length > 0);
								case enums.BREADCRUMS.SOFTWARE_PRODUCT_DEPLOYMENT:
									let isManualMode = onboardingMethod === onboardingMethodTypes.MANUAL;
									return isManualMode;
								case enums.BREADCRUMS.SOFTWARE_PRODUCT_DEPENDENCIES:
									return (componentsList.length > 1);
								default:
									return true;
							}
						})
					}])
				];
				if(props.componentId) {
					returnedBreadcrumb = [
						...returnedBreadcrumb, {
							selectedKey: props.componentId,
							menuItems: componentsList
								.map(({id, displayName}) => ({
									key: id,
									displayText: displayName
								}))
						},
						...[{
							selectedKey: componentScreenToBreadcrumb[screen],
							menuItems: [{
								key: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_GENERAL,
								displayText: i18n('General')
							}, {
								key: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_COMPUTE,
								displayText: i18n('Compute')
							}, {
								key: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING,
								displayText: i18n('High Availability & Load Balancing')
							}, {
								key: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_NETWORK,
								displayText: i18n('Networks')
							}, {
								key: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_STORAGE,
								displayText: i18n('Storage')
							}, {
								key: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_IMAGES,
								displayText: i18n('Images')
							}, {
								key: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_PROCESSES,
								displayText: i18n('Process Details')
							}, {
								key: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_MONITORING,
								displayText: i18n('Monitoring')
							}]
						}]
					];
				}
				return returnedBreadcrumb;
		}
	}
}

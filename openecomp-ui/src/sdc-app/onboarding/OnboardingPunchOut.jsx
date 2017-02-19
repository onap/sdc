import React from 'react';
import ReactDOM from 'react-dom';
import {connect} from 'react-redux';
import isEqual from 'lodash/isEqual.js';
import objectValues from 'lodash/values.js';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Application from 'sdc-app/Application.jsx';
import store from 'sdc-app/AppStore.js';
import Configuration from 'sdc-app/config/Configuration.js';

import OnboardingCatalog from './OnboardingCatalog.js';
import LicenseModel from './licenseModel/LicenseModel.js';
import LicenseAgreementListEditor from './licenseModel/licenseAgreement/LicenseAgreementListEditor.js';
import FeatureGroupListEditor from './licenseModel/featureGroups/FeatureGroupListEditor.js';
import LicenseKeyGroupsListEditor from './licenseModel/licenseKeyGroups/LicenseKeyGroupsListEditor.js';
import EntitlementPoolsListEditor from './licenseModel/entitlementPools/EntitlementPoolsListEditor.js';
import SoftwareProduct from './softwareProduct/SoftwareProduct.js';
import SoftwareProductLandingPage  from './softwareProduct/landingPage/SoftwareProductLandingPage.js';
import SoftwareProductDetails  from './softwareProduct/details/SoftwareProductDetails.js';
import SoftwareProductAttachments from './softwareProduct/attachments/SoftwareProductAttachments.js';
import SoftwareProductProcesses from './softwareProduct/processes/SoftwareProductProcesses.js';
import SoftwareProductNetworks from './softwareProduct/networks/SoftwareProductNetworks.js';
import SoftwareProductComponentsList from './softwareProduct/components/SoftwareProductComponentsList.js';
import SoftwareProductComponentProcessesList from './softwareProduct/components/processes/SoftwareProductComponentProcessesList.js';
import SoftwareProductComponentStorage from './softwareProduct/components/storage/SoftwareProductComponentStorage.js';
import SoftwareProductComponentsNetworkList from './softwareProduct/components/network/SoftwareProductComponentsNetworkList.js';
import SoftwareProductComponentsGeneral from './softwareProduct/components/general/SoftwareProductComponentsGeneral.js';
import SoftwareProductComponentsCompute from './softwareProduct/components/compute/SoftwareProductComponentCompute.js';
import SoftwareProductComponentLoadBalancing from './softwareProduct/components/loadBalancing/SoftwareProductComponentLoadBalancing.js';
import SoftwareProductComponentsMonitoring from './softwareProduct/components/monitoring/SoftwareProductComponentsMonitoring.js';
import {navigationItems as SoftwareProductNavigationItems, actionTypes as SoftwareProductActionTypes} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';

import {enums} from './OnboardingConstants.js';
import OnboardingActionHelper from './OnboardingActionHelper.js';


class OnboardingView extends React.Component {
	static propTypes = {
		currentScreen: React.PropTypes.shape({
			screen: React.PropTypes.oneOf(objectValues(enums.SCREEN)).isRequired,
			props: React.PropTypes.object.isRequired
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
							return <OnboardingCatalog {...props}/>;

						case enums.SCREEN.LICENSE_AGREEMENTS:
						case enums.SCREEN.FEATURE_GROUPS:
						case enums.SCREEN.ENTITLEMENT_POOLS:
						case enums.SCREEN.LICENSE_KEY_GROUPS:
							return (
								<LicenseModel currentScreen={currentScreen}>
								{
									(()=>{
										switch(screen) {
											case enums.SCREEN.LICENSE_AGREEMENTS:
												return <LicenseAgreementListEditor {...props}/>;
											case enums.SCREEN.FEATURE_GROUPS:
												return <FeatureGroupListEditor {...props}/>;
											case enums.SCREEN.ENTITLEMENT_POOLS:
												return <EntitlementPoolsListEditor {...props}/>;
											case enums.SCREEN.LICENSE_KEY_GROUPS:
												return <LicenseKeyGroupsListEditor {...props}/>;
										}
									})()
								}
								</LicenseModel>
							);

						case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
						case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
						case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS:
						case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
						case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
						case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
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
												return <SoftwareProductAttachments className='no-padding-content-area'  {...props} />;
											case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
												return <SoftwareProductProcesses {...props}/>;
											case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
												return <SoftwareProductNetworks {...props}/>;
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
											case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
												return <SoftwareProductComponentsMonitoring {...props}/>;
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
		let {softwareProductList, softwareProduct: {softwareProductEditor: {data: {id: currentSoftwareProductId, version: currentSoftwareProductVersion} = {}}},
			licenseModelList, licenseModel: {licenseModelEditor: {data: {id: currentLicenseModelId, version: currentLicenseModelVersion} = {}}}} = store.getState();

		if (this.programmaticBreadcrumbsUpdate) {
			this.prevSelectedKeys = selectedKeys;
			this.programmaticBreadcrumbsUpdate = false;
			return;
		}

		if (!isEqual(selectedKeys, this.prevSelectedKeys)) {
			this.breadcrumbsPrefixSelected = isEqual(selectedKeys, this.prevSelectedKeys && this.prevSelectedKeys.slice(0, selectedKeys.length));
			this.prevSelectedKeys = selectedKeys;

			if (selectedKeys.length === 0) {
				OnboardingActionHelper.navigateToOnboardingCatalog(dispatch);
			} else if (selectedKeys.length === 1 || selectedKeys[1] === enums.BREADCRUMS.LICENSE_MODEL) {
				let [licenseModelId, , licenseModelScreen] = selectedKeys;
				if (!licenseModelScreen) {
					licenseModelScreen = enums.BREADCRUMS.LICENSE_AGREEMENTS;
				}
				if(currentLicenseModelId !== licenseModelId) {
					currentLicenseModelVersion = licenseModelList.find(lm => lm.id === licenseModelId).version;
				}
				switch (licenseModelScreen) {
					case enums.BREADCRUMS.LICENSE_AGREEMENTS:
						OnboardingActionHelper.navigateToLicenseAgreements(dispatch, {licenseModelId, version: currentLicenseModelVersion});
						break;
					case enums.BREADCRUMS.FEATURE_GROUPS:
						OnboardingActionHelper.navigateToFeatureGroups(dispatch, {licenseModelId, version: currentLicenseModelVersion});
						break;
					case enums.BREADCRUMS.ENTITLEMENT_POOLS:
						OnboardingActionHelper.navigateToEntitlementPools(dispatch, {licenseModelId, version: currentLicenseModelVersion});
						break;
					case enums.BREADCRUMS.LICENSE_KEY_GROUPS:
						OnboardingActionHelper.navigateToLicenseKeyGroups(dispatch, {licenseModelId, version: currentLicenseModelVersion});
						break;
				}
			} else if (selectedKeys.length <= 4 && selectedKeys[1] === enums.BREADCRUMS.SOFTWARE_PRODUCT) {
				let [licenseModelId, , softwareProductId, softwareProductScreen] = selectedKeys;
				let softwareProduct = softwareProductId ?
					softwareProductList.find(({id}) => id === softwareProductId) :
					softwareProductList.find(({vendorId}) => vendorId === licenseModelId);
				if (!softwareProductId) {
					softwareProductId = softwareProduct.id;
				}
				if(currentSoftwareProductId !== softwareProductId) {
					currentSoftwareProductVersion = softwareProduct.version;
				}
				switch (softwareProductScreen) {
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_DETAILS:
						OnboardingActionHelper.navigateToSoftwareProductDetails(dispatch, {softwareProductId});
						break;
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_ATTACHMENTS:
						OnboardingActionHelper.navigateToSoftwareProductAttachments(dispatch, {softwareProductId});
						break;
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_PROCESSES:
						OnboardingActionHelper.navigateToSoftwareProductProcesses(dispatch, {softwareProductId, version: currentSoftwareProductVersion});
						break;
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_NETWORKS:
						OnboardingActionHelper.navigateToSoftwareProductNetworks(dispatch, {softwareProductId, version: currentSoftwareProductVersion});
						break;
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS:
						OnboardingActionHelper.navigateToSoftwareProductComponents(dispatch, {softwareProductId, version: currentSoftwareProductVersion});
						dispatch({
							type: SoftwareProductActionTypes.TOGGLE_NAVIGATION_ITEM,
							mapOfExpandedIds: {
								[SoftwareProductNavigationItems.COMPONENTS]: true
							}
						});
						break;
					default:
						OnboardingActionHelper.navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, version: currentSoftwareProductVersion});
						break;
				}
			}else if (selectedKeys.length === 5 && selectedKeys[1] === enums.BREADCRUMS.SOFTWARE_PRODUCT && selectedKeys[3] === enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS) {
				let [licenseModelId, , softwareProductId, , componentId] = selectedKeys;
				let softwareProduct = softwareProductId ?
					softwareProductList.find(({id}) => id === softwareProductId) :
					softwareProductList.find(({vendorId}) => vendorId === licenseModelId);
				if (!softwareProductId) {
					softwareProductId = softwareProduct.id;
				}
				if(currentSoftwareProductId !== softwareProductId) {
					currentSoftwareProductVersion = softwareProduct.version;
				}
				OnboardingActionHelper.navigateToSoftwareProductComponentGeneralAndUpdateLeftPanel(dispatch, {softwareProductId, componentId, version: currentSoftwareProductVersion});
			}else if (selectedKeys.length === 6 && selectedKeys[1] === enums.BREADCRUMS.SOFTWARE_PRODUCT && selectedKeys[3] === enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS) {
				let [licenseModelId, , softwareProductId, , componentId, componentScreen] = selectedKeys;
				let softwareProduct = softwareProductId ?
					softwareProductList.find(({id}) => id === softwareProductId) :
					softwareProductList.find(({vendorId}) => vendorId === licenseModelId);
				if (!softwareProductId) {
					softwareProductId = softwareProduct.id;
				}
				if(currentSoftwareProductId !== softwareProductId) {
					currentSoftwareProductVersion = softwareProduct.version;
				}
				switch (componentScreen) {
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
						OnboardingActionHelper.navigateToSoftwareProductComponentGeneral(dispatch, {softwareProductId, componentId, version: currentSoftwareProductVersion});
						break;
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
						OnboardingActionHelper.navigateToComponentCompute(dispatch, {softwareProductId, componentId});
						break;
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
						OnboardingActionHelper.navigateToComponentLoadBalancing(dispatch, {softwareProductId, componentId});
						break;
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
						OnboardingActionHelper.navigateToComponentNetwork(dispatch, {softwareProductId, componentId});
						break;
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
						OnboardingActionHelper.navigateToComponentStorage(dispatch, {softwareProductId, componentId});
						break;
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
						OnboardingActionHelper.navigateToSoftwareProductComponentProcesses(dispatch, {softwareProductId, componentId});
						break;
					case enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
						OnboardingActionHelper.navigateToSoftwareProductComponentMonitoring(dispatch, {softwareProductId, componentId});
						break;
				}
			} else {
				console.error('Unknown breadcrumbs path: ', selectedKeys);
			}
		}
	}

	handleStoreChange() {
		let {currentScreen, licenseModelList, softwareProductList} = store.getState();
		let breadcrumbsData = {currentScreen, licenseModelList, softwareProductList};

		if (!isEqual(breadcrumbsData, this.prevBreadcrumbsData) || this.breadcrumbsPrefixSelected) {
			this.prevBreadcrumbsData = breadcrumbsData;
			this.breadcrumbsPrefixSelected = false;
			this.programmaticBreadcrumbsUpdate = true;

			let breadcrumbs = this.buildBreadcrumbs(breadcrumbsData);
			this.onEvent('breadcrumbsupdated', breadcrumbs);
		}
	}

	buildBreadcrumbs({currentScreen: {screen, props}, licenseModelList, softwareProductList}) {
		let componentsList;
		if(props.componentId) {
			componentsList = store.getState().softwareProduct.softwareProductComponents.componentsList;
		}
		let screenToBreadcrumb;
		switch (screen) {
			case enums.SCREEN.ONBOARDING_CATALOG:
				return [];

			case enums.SCREEN.LICENSE_AGREEMENTS:
			case enums.SCREEN.FEATURE_GROUPS:
			case enums.SCREEN.ENTITLEMENT_POOLS:
			case enums.SCREEN.LICENSE_KEY_GROUPS:
				screenToBreadcrumb = {
					[enums.SCREEN.LICENSE_AGREEMENTS]: enums.BREADCRUMS.LICENSE_AGREEMENTS,
					[enums.SCREEN.FEATURE_GROUPS]: enums.BREADCRUMS.FEATURE_GROUPS,
					[enums.SCREEN.ENTITLEMENT_POOLS]: enums.BREADCRUMS.ENTITLEMENT_POOLS,
					[enums.SCREEN.LICENSE_KEY_GROUPS]: enums.BREADCRUMS.LICENSE_KEY_GROUPS
				};
				return [
					{
						selectedKey: props.licenseModelId,
						menuItems: licenseModelList.map(({id, vendorName}) => ({
							key: id,
							displayText: vendorName
						}))
					},
					{
						selectedKey: enums.BREADCRUMS.LICENSE_MODEL,
						menuItems: [{
							key: enums.BREADCRUMS.LICENSE_MODEL,
							displayText: i18n('License Model')
						},
							...(softwareProductList.findIndex(({vendorId}) => vendorId === props.licenseModelId) === -1 ? [] : [{
								key: enums.BREADCRUMS.SOFTWARE_PRODUCT,
								displayText: i18n('Software Products')
							}])]
					}, {
						selectedKey: screenToBreadcrumb[screen],
						menuItems: [{
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
						}]
					}
				];

			case enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE:
			case enums.SCREEN.SOFTWARE_PRODUCT_DETAILS:
			case enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS:
			case enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES:
			case enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS:

			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING:
			case enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING:
				screenToBreadcrumb = {
					[enums.SCREEN.SOFTWARE_PRODUCT_DETAILS]: enums.BREADCRUMS.SOFTWARE_PRODUCT_DETAILS,
					[enums.SCREEN.SOFTWARE_PRODUCT_ATTACHMENTS]: enums.BREADCRUMS.SOFTWARE_PRODUCT_ATTACHMENTS,
					[enums.SCREEN.SOFTWARE_PRODUCT_PROCESSES]: enums.BREADCRUMS.SOFTWARE_PRODUCT_PROCESSES,
					[enums.SCREEN.SOFTWARE_PRODUCT_NETWORKS]: enums.BREADCRUMS.SOFTWARE_PRODUCT_NETWORKS,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENTS]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS
				};
				let componentScreenToBreadcrumb = {
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_PROCESSES]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_PROCESSES,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_COMPUTE]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_COMPUTE,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_STORAGE]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_STORAGE,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_NETWORK]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_NETWORK,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_GENERAL]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_GENERAL,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_LOAD_BALANCING,
					[enums.SCREEN.SOFTWARE_PRODUCT_COMPONENT_MONITORING]: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENT_MONITORING
				};
				let licenseModelId = softwareProductList.find(({id}) => id === props.softwareProductId).vendorId;
				let returnedBreadcrumb = [
					{
						selectedKey: licenseModelId,
						menuItems: licenseModelList.map(({id, vendorName}) => ({
							key: id,
							displayText: vendorName
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
						menuItems: softwareProductList
							.filter(({vendorId}) => vendorId === licenseModelId)
							.map(({id, name}) => ({
								key: id,
								displayText: name
							}))
					},
					...(screen === enums.SCREEN.SOFTWARE_PRODUCT_LANDING_PAGE ? [] : [{
						selectedKey: screenToBreadcrumb[screen] || enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS,
						menuItems: [{
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_DETAILS,
							displayText: i18n('General')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_ATTACHMENTS,
							displayText: i18n('Attachments')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_PROCESSES,
							displayText: i18n('Process Details')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_NETWORKS,
							displayText: i18n('Networks')
						}, {
							key: enums.BREADCRUMS.SOFTWARE_PRODUCT_COMPONENTS,
							displayText: i18n('Components')
						}]
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

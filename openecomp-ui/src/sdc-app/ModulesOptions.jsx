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
import {connect} from 'react-redux';
import Input from 'nfvo-components/input/validation/InputWrapper.jsx';

import LicenseModelActionHelper from './onboarding/licenseModel/LicenseModelActionHelper.js';
import LicenseAgreementListEditor from './onboarding/licenseModel/licenseAgreement/LicenseAgreementListEditor.js';
import LicenseAgreementActionHelper from './onboarding/licenseModel/licenseAgreement/LicenseAgreementActionHelper.js';
import FeatureGroupListEditor from './onboarding/licenseModel/featureGroups/FeatureGroupListEditor.js';
import FeatureGroupsActionHelper from './onboarding/licenseModel/featureGroups/FeatureGroupsActionHelper.js';
import LicenseKeyGroupsListEditor from './onboarding/licenseModel/licenseKeyGroups/LicenseKeyGroupsListEditor.js';
import LicenseKeyGroupsActionHelper from './onboarding/licenseModel/licenseKeyGroups/LicenseKeyGroupsActionHelper.js';
import EntitlementPoolsListEditor from './onboarding/licenseModel/entitlementPools/EntitlementPoolsListEditor.js';
import EntitlementPoolsActionHelper from './onboarding/licenseModel/entitlementPools/EntitlementPoolsActionHelper.js';
import SoftwareProductLandingPage  from './onboarding/softwareProduct/landingPage/SoftwareProductLandingPage.js';
import SoftwareProductDetails  from './onboarding/softwareProduct/details/SoftwareProductDetails.js';
import Onboard from './onboarding/onboard/Onboard.js';
import SoftwareProductActionHelper from './onboarding/softwareProduct/SoftwareProductActionHelper.js';
import FlowsListEditor from './flows/FlowsListEditor.js';
import FlowsActions from './flows/FlowsActions.js';


const mapStateToProps = ({licenseModelList}) => {
	return {licenseModelList};
};


const mapActionsToProps = dispatch => {
	return {
		onBootstrapped: () => LicenseModelActionHelper.fetchLicenseModels(dispatch),
		onLicenseAgreementListEditor: licenseModelId => LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {licenseModelId}),
		onFeatureGroupsListEditor: licenseModelId => FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId}),
		onLicenseKeyGroupsListEditor: licenseModelId =>LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {licenseModelId}),
		onEntitlementPoolsListEditor: licenseModelId => EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {licenseModelId}),
		onOnboardingCatalog: () => SoftwareProductActionHelper.fetchSoftwareProductList(dispatch),
		onSoftwareProductDetails: () => SoftwareProductActionHelper.fetchSoftwareProductCategories(dispatch),
		onFlowsListEditor: () => FlowsActions.fetchFlows(dispatch)
	};
};

class ModuleOptions extends React.Component {

	static propTypes = {
		onBootstrapped: PropTypes.func.isRequired,
		onLicenseAgreementListEditor: PropTypes.func.isRequired,
		onFeatureGroupsListEditor: PropTypes.func.isRequired,
		onLicenseKeyGroupsListEditor: PropTypes.func.isRequired,
		onEntitlementPoolsListEditor: PropTypes.func.isRequired,
		onOnboardingCatalog: PropTypes.func.isRequired,
		onSoftwareProductDetails: PropTypes.func.isRequired,
	};

	state = {
		currentModule: localStorage.getItem('default-module'),
		licenseModelId: localStorage.getItem('default-license-model-id')
	};

	componentDidMount() {
		this.props.onBootstrapped();
	}

	render() {
		let {currentModule, licenseModelId} = this.state;
		let {licenseModelList} = this.props;
		return (
			<div style={{marginTop:20}}>
				<Input
					name='licenseModel'
					value={licenseModelId}
					ref='licenseModelId'
					type='select'
					onChange={this.handleLicenseModelIdChange}
					className='inner-pagination select-input'>
					<option value='' key={null}>Select License Model</option>
					{
						licenseModelList.map(({id, vendorName}) => <option value={id} key={id}>{`${vendorName} License Model`}</option>)
					}
				</Input>
				<Input
					name='currentView'
					value={currentModule}
					ref='selectedModule'
					type='select'
					onChange={this.handleModuleSelection}
					className='inner-pagination select-input'>
					<option value=''>Select Module</option>
					<option value='EntitlementPoolsListEditor'>Entitlement Pools</option>
					<option value='LicenseAgreementListEditor'>License Agreements</option>
					<option value='FutureGroupListEditor'>Feature Groups</option>
					<option value='LicenseKeyGroupsListEditor'>License Key Groups</option>
					<option value='SoftwareProductLanding'>Software Product Landing</option>
					<option value='SoftwareProductDetails'>Software Product Details</option>
					<option value='OnboardingCatalog'>Onboarding Catalog</option>
					<option value='Flows'>Flows</option>
				</Input>
				<div className='sub-module-view' style={{paddingTop: 10, margin: 4, borderTop: '1px solid silver'}}>
					{this.renderModule(currentModule)}
				</div>
			</div>
		);
	}

	renderModule(currentModule) {
		const {licenseModelId} = this.state;
		if (!licenseModelId) {
			return;
		}

		switch (currentModule) {
			case 'LicenseAgreementListEditor':
				this.props.onLicenseAgreementListEditor(licenseModelId);
				return <LicenseAgreementListEditor licenseModelId={licenseModelId}/>;
			case 'FutureGroupListEditor':
				this.props.onFeatureGroupsListEditor(licenseModelId);
				return <FeatureGroupListEditor licenseModelId={licenseModelId}/>;
			case 'EntitlementPoolsListEditor':
				this.props.onEntitlementPoolsListEditor(licenseModelId);
				return <EntitlementPoolsListEditor licenseModelId={licenseModelId}/>;
			case 'LicenseKeyGroupsListEditor':
				this.props.onLicenseKeyGroupsListEditor(licenseModelId);
				return <LicenseKeyGroupsListEditor licenseModelId={licenseModelId}/>;
			case 'SoftwareProductLanding':
				return <SoftwareProductLandingPage licenseModelId={licenseModelId}/>;
			case 'SoftwareProductDetails':
				this.props.onSoftwareProductDetails(licenseModelId);
				return <SoftwareProductDetails licenseModelId={licenseModelId}/>;
			case 'OnboardingCatalog':
				this.props.onOnboardingCatalog();
				return <Onboard/>;
			case 'Flows':
				this.props.onFlowsListEditor();
				return <FlowsListEditor/>;
			default:
				return;
		}
	}

	handleModuleSelection = () => {
		let selectedModule = this.refs.selectedModule.getValue();
		localStorage.setItem('default-module', selectedModule);
		this.setState({currentModule: selectedModule});
	}

	handleLicenseModelIdChange = () => {
		let licenseModelId = this.refs.licenseModelId.getValue();
		localStorage.setItem('default-license-model-id', licenseModelId);
		this.setState({licenseModelId});
	}
}

export default connect(mapStateToProps, mapActionsToProps)(ModuleOptions);

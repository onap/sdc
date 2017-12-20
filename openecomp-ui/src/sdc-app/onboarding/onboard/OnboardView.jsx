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
import OnboardingCatalogView from './onboardingCatalog/OnboardingCatalogView.jsx';
import WorkspaceView from './workspace/WorkspaceView.jsx';
import {tabsMapping} from './OnboardConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import classnames from 'classnames';
import ExpandableInput from 'nfvo-components/input/ExpandableInput.jsx';
import objectValues from 'lodash/values.js';
import {catalogItemTypes} from './onboardingCatalog/OnboardingCatalogConstants.js';
import NotificationsView from 'sdc-app/onboarding/userNotifications/NotificationsView.jsx';

const OnboardHeaderTabs = ({onTabClick, activeTab}) => (
	<div className='onboard-header-tabs'>
		<div
			className={classnames('onboard-header-tab', {'active': activeTab === tabsMapping.WORKSPACE })}
			onClick={() => onTabClick(tabsMapping.WORKSPACE)}
			data-test-id='onboard-workspace-tab'>
			{i18n('WORKSPACE')}
		</div>
		<div
			className={classnames('onboard-header-tab', {'active': activeTab === tabsMapping.CATALOG })}
			onClick={() => onTabClick(tabsMapping.CATALOG)}
			data-test-id='onboard-onboard-tab'>
			{i18n('ONBOARD CATALOG')}
		</div>
	</div>
);

const OnboardHeader = ({onSearch, activeTab, onTabClick, searchValue}) => (
	<div className='onboard-header'>
		<OnboardHeaderTabs activeTab={activeTab} onTabClick={onTabClick} />
		<ExpandableInput
			onChange={onSearch}
			iconType='search'
			value={searchValue}/>
		<NotificationsView />
	</div>
);

class OnboardView extends React.Component {
	static propTypes = {
		licenseModelList: PropTypes.array,
		softwareProductList: PropTypes.array,
		finalizedLicenseModelList: PropTypes.array,
		finalizedSoftwareProductList: PropTypes.array,
		modalToShow: PropTypes.oneOf(objectValues(catalogItemTypes)),
		onSelectLicenseModel: PropTypes.func.isRequired,
		onSelectSoftwareProduct: PropTypes.func.isRequired,
		onAddLicenseModelClick: PropTypes.func.isRequired,
		onAddSoftwareProductClick: PropTypes.func.isRequired,
		closeVspOverlay: PropTypes.func.isRequired,
		onVspOverlayChange: PropTypes.func.isRequired,
		onTabClick: PropTypes.func.isRequired,
		onCatalogTabClick: PropTypes.func.isRequired,
		onSearch: PropTypes.func.isRequired,
		activeTab: PropTypes.number.isRequired,
		catalogActiveTab: PropTypes.number.isRequired,
		searchValue: PropTypes.string.isRequired,
		onMigrate: PropTypes.func.isRequired,
	};
	renderViewByTab(activeTab){
		switch (activeTab){
			case tabsMapping.WORKSPACE:
				return <WorkspaceView {...this.props} />;
			case tabsMapping.CATALOG:
			default:
				return <OnboardingCatalogView {...this.props} />;
		}
	}

	render() {
		let {activeTab, onTabClick, onSearch, searchValue} = this.props;
		return (
			<div className='catalog-view'>
				<OnboardHeader activeTab={activeTab} onTabClick={onTabClick} searchValue={searchValue} onSearch={value => onSearch(value)}/>
				{this.renderViewByTab(activeTab)}
			</div>
		);
	}
}

export default OnboardView;

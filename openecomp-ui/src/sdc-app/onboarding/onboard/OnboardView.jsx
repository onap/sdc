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
import OnboardingCatalogView from './onboardingCatalog/OnboardingCatalogView.jsx';
import WorkspaceView from './workspace/WorkspaceView.jsx';
import {tabsMapping} from './OnboardConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import classnames from 'classnames';
import ExpandableInput from 'nfvo-components/input/ExpandableInput.jsx';
import objectValues from 'lodash/values.js';
import {catalogItemTypes} from './onboardingCatalog/OnboardingCatalogConstants.js';

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
	</div>
);

class OnboardView extends React.Component {
	static propTypes = {
		licenseModelList: React.PropTypes.array,
		softwareProductList: React.PropTypes.array,
		finalizedLicenseModelList: React.PropTypes.array,
		finalizedSoftwareProductList: React.PropTypes.array,
		modalToShow: React.PropTypes.oneOf(objectValues(catalogItemTypes)),
		onSelectLicenseModel: React.PropTypes.func.isRequired,
		onSelectSoftwareProduct: React.PropTypes.func.isRequired,
		onAddLicenseModelClick: React.PropTypes.func.isRequired,
		onAddSoftwareProductClick: React.PropTypes.func.isRequired,
		closeVspOverlay: React.PropTypes.func.isRequired,
		onVspOverlayChange: React.PropTypes.func.isRequired,
		onTabClick: React.PropTypes.func.isRequired,
		onCatalogTabClick: React.PropTypes.func.isRequired,
		onSearch: React.PropTypes.func.isRequired,
		activeTab: React.PropTypes.number.isRequired,
		catalogActiveTab: React.PropTypes.number.isRequired,
		searchValue: React.PropTypes.string.isRequired,
		onMigrate: React.PropTypes.func.isRequired,
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
		let {closeVspOverlay, activeTab, onTabClick, onSearch, searchValue} = this.props;
		return (
			<div className='catalog-view' onClick={closeVspOverlay}>
				<OnboardHeader activeTab={activeTab} onTabClick={onTabClick} searchValue={searchValue} onSearch={value => onSearch(value)}/>
				{this.renderViewByTab(activeTab)}
			</div>
		);
	}
}

export default OnboardView;

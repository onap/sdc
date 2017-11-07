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
import {selectedButton} from '../LicenseModelOverviewConstants.js';
import Tabs from 'sdc-ui/lib/react/Tabs.js';
import Tab from 'sdc-ui/lib/react/Tab.js';
import i18n from 'nfvo-utils/i18n/i18n.js';

function ListButtons ({onTabSelect, selectedTab, hasOrphans, hasLicensing}) {
	// no data, no tabs
	if (!hasLicensing && !hasOrphans) {
		return null;
	}
	return (
		<Tabs 
			onTabClick={(tabId) => onTabSelect(tabId)}
			activeTab={selectedTab}
			className='overview-buttons-section'
			type='header' >
			<Tab
				tabId={selectedButton.VLM_LIST_VIEW}
				title={i18n('Connections List')}
				data-test-id='vlm-overview-vlmlist-tab' />
			<Tab
				tabId={selectedButton.NOT_IN_USE}
				title={i18n('Orphans List')}
				data-test-id='vlm-overview-orphans-tab' />
		</Tabs>
	);
}

ListButtons.propTypes = {
	onTabSelect: React.PropTypes.func,
	selectedInUse: React.PropTypes.bool
};

export default ListButtons;

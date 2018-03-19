/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */

import {connect} from 'react-redux';
import React from 'react';
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Input from 'nfvo-components/input/validation/Input.jsx';
import Accordion from 'nfvo-components/accordion/Accordion.jsx';
import {actionTypes} from './FilterConstants.js';
import featureToggle from 'sdc-app/features/featureToggle.js';
import {tabsMapping as onboardTabsMapping} from '../OnboardConstants.js';
import {itemsType as itemsTypeConstants} from './FilterConstants.js';
const mapStateToProps = ({onboard: {filter, activeTab}}) => {
	return {
		data: filter,
		activeTab
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onDataChanged: (deltaData) => 
			dispatch({
				type: actionTypes.FILTER_DATA_CHANGED,
				deltaData
			})		
	};
};

const Filter = ({onDataChanged, data: {entityTypeVsp, entityTypeVlm, roleOwner, roleContributor, roleViewer,
		 procedureNetwork, procedureManual, recentlyUpdated, byVendorView, itemsType}, activeTab}) => (
    <div className='catalog-filter'>			
		 {activeTab === onboardTabsMapping.CATALOG && <Input
			type='select'
			className='catalog-filter-items-type'
			data-test-id='catalog-filter-items-type' 
			disabled={byVendorView}
			value={itemsType}
			onChange={e => onDataChanged({itemsType: e.target.value})}>
				<option key={itemsTypeConstants.ACTIVE} value={itemsTypeConstants.ACTIVE}>Active Items</option>
				<option key={itemsTypeConstants.ARCHIVED} value={itemsTypeConstants.ARCHIVED}>Archived Items</option>
		</Input>}
		{activeTab === onboardTabsMapping.CATALOG && <Input 
			label={i18n('By Vendor View')}
			 type='checkbox'
			 disabled={itemsType === itemsTypeConstants.ARCHIVED} 
			 checked={byVendorView} 
			 onChange={byVendorView => onDataChanged({byVendorView})}
			 data-test-id='filter-by-vendor-view' value='' />
		}
        <Input label={i18n('Recently Updated')} type='checkbox' checked={recentlyUpdated} 
	onChange={recentlyUpdated => onDataChanged({recentlyUpdated})}  data-test-id='filter-recently-updated' value='' />

		<Accordion title={i18n('ENTITY TYPE')}>
			<Input label={i18n('VSP')} type='checkbox' checked={entityTypeVsp} onChange={entityTypeVsp => onDataChanged({entityTypeVsp})} data-test-id='filter-type-vsp' value='' />
			<Input label={i18n('VLM')} type='checkbox' checked={entityTypeVlm} onChange={entityTypeVlm => onDataChanged({entityTypeVlm})} data-test-id='filter-type-vlm' value='' />			
		</Accordion>

		<Accordion title={i18n('ROLE')}>
			<Input label={i18n('Owner')} type='checkbox' checked={roleOwner} onChange={roleOwner => onDataChanged({roleOwner})}  data-test-id='filter-role-owner' value='' />
			<Input label={i18n('Contributer')} type='checkbox' checked={roleContributor} 
				onChange={roleContributor => onDataChanged({roleContributor})}  data-test-id='filter-role-contributor' value='' />
			<Input label={i18n('Viewer')} type='checkbox' checked={roleViewer} onChange={roleViewer => onDataChanged({roleViewer})} data-test-id='filter-role-viewr' value='' />
		</Accordion>
		
		<Accordion title={i18n('ONBOARDING PROCEDURE')}>
			<Input label={i18n('Network Package')} type='checkbox' checked={procedureNetwork} 
				onChange={procedureNetwork => onDataChanged({procedureNetwork})}  data-test-id='filter-procedure-network' value='' />
			<Input label={i18n('Manual')} type='checkbox' checked={procedureManual} 
				onChange={procedureManual => onDataChanged({procedureManual})} data-test-id='filter-procedure-manual' value='' />			
		</Accordion>
    </div>
);

Filter.PropTypes = {
	onDataChanged: PropTypes.func,
	data: PropTypes.object
};

export default featureToggle('FILTER')(connect(mapStateToProps, mapActionsToProps)(Filter));

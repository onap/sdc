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
import React, {Component} from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {extractValue, extractUnits} from '../../entitlementPools/EntitlementPoolsConstants.js';
import ArrowCol from './listItemsComponents/ArrowCol.jsx';
import ItemInfo from './listItemsComponents/ItemInfo.jsx';
import IconCol from './listItemsComponents/IconCol.jsx';
import {AdditionalDataCol, AdditionalDataElement} from './listItemsComponents/AdditionalDataCol.jsx';

class EntitlementPool extends Component {
	render() {
		let {epData: {name, description, manufacturerReferenceNumber}} = this.props;
		return (
			<div className='vlm-list-item vlm-list-item-ep' data-test-id='vlm-list-item-ep'>
				<ArrowCol/>
				<IconCol className='ep-icon'/>
				<ItemInfo name={name} description={description}/>
				<AdditionalDataCol>
					<AdditionalDataElement
						className='vlm-list-item-entitlement-metric'
						name={i18n('Entitlement')}
						value={this.getEntitlement()}/>
					<AdditionalDataElement
						name={i18n('Manufacturer Reference Number')}
						value={manufacturerReferenceNumber}
						className='vlm-list-item-sku'/>
				</AdditionalDataCol>
			</div>
		);
	}

	getEntitlement() {
		let {epData: {entitlementMetric, aggregationFunction, time, thresholdValue, thresholdUnits}} = this.props;
		return `${extractValue(aggregationFunction)} ${extractValue(entitlementMetric)} per  ${extractValue(time)} ${thresholdValue} ${extractUnits(thresholdUnits)}`;
	}

}

export default EntitlementPool;

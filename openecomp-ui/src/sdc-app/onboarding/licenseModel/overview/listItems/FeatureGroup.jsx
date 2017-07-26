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
import {overviewEditorHeaders} from '../LicenseModelOverviewConstants.js';
import ArrowCol from './listItemsComponents/ArrowCol.jsx';
import ItemInfo from './listItemsComponents/ItemInfo.jsx';
import IconCol from './listItemsComponents/IconCol.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {AdditionalDataCol, AdditionalDataElement} from './listItemsComponents/AdditionalDataCol.jsx';

class FeatureGroup extends Component {
	render() {
		let {fgData: {name, manufacturerReferenceNumber, description, children = []}, isCollapsed, onClick, isOrphan} = this.props;
		return (
			<div 
				onClick={e => onClick(e)} 
				className={`vlm-list-item vlm-list-item-fg ${isOrphan ? 'orphan-list-item' : ''} ${children.length && !isOrphan ? 'clickable' : ''}`} data-test-id='vlm-list-item-fg'>
				{!isOrphan && <ArrowCol isCollapsed={isCollapsed} length={children.length} />}
				<IconCol className='fg-icon' text='FG'/>
				<ItemInfo name={name} description={description}>
					<div className='children-count'>
						<span className='count-value'>
							{i18n('Entitlement Pools: ')}
							<span data-test-id='vlm-list-ep-count-value'>
								{`${children.filter(child => child.itemType === overviewEditorHeaders.ENTITLEMENT_POOL).length}`}
							</span>
						</span>
						<span className='fg-pipeline-separator'>|</span>
						<span className='count-value'>
								{i18n('License Key Groups: ')}
								<span data-test-id='vlm-list-lkg-count-value'>
									{`${children.filter(child => child.itemType === overviewEditorHeaders.LICENSE_KEY_GROUP).length}`}
								</span>
						</span>
					</div>
				</ItemInfo>
				<AdditionalDataCol>
					<AdditionalDataElement
						name={i18n('Manufacturer Reference Number')}
						value={manufacturerReferenceNumber}
						className='vlm-list-item-sku'/>
				</AdditionalDataCol>
			</div>
		);
	}
}

export default FeatureGroup;

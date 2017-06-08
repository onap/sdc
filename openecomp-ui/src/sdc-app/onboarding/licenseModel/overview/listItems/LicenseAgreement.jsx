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
import InputOptions, {other as optionInputOther} from 'nfvo-components/input/inputOptions/InputOptions.jsx';
import {optionsInputValues} from '../../licenseAgreement/LicenseAgreementConstants.js';
import ArrowCol from './listItemsComponents/ArrowCol.jsx';
import ItemInfo from './listItemsComponents/ItemInfo.jsx';
import IconCol from './listItemsComponents/IconCol.jsx';
import {AdditionalDataCol, AdditionalDataElement} from './listItemsComponents/AdditionalDataCol.jsx';

class LicenseAgreement extends Component {
	render() {
		let {laData: {name, description, licenseTerm, children = []}, isCollapsed, onClick} = this.props;
		return (
			<div onClick={e => onClick(e)} className='vlm-list-item vlm-list-item-la' data-test-id='vlm-list-la-item'>
				<ArrowCol isCollapsed={isCollapsed} length={children.length} />
				<IconCol className='la-icon'/>
				<ItemInfo name={name} description={description}>
					<div className='children-count'>
						<span className='count-value'>Feature Groups: <span data-test-id='vlm-list-fg-count-value'>{`${children.length}`}</span></span>
					</div>
				</ItemInfo>
				<AdditionalDataCol>
					<AdditionalDataElement
						name={i18n('License Model Type')}
						value={this.extractValue(licenseTerm)}/>
				</AdditionalDataCol>
			</div>
		);
	}

	extractValue(item) {
		if (item === undefined) {return '';} //TODO fix it later

		return  item ? item.choice === optionInputOther.OTHER ? item.other : InputOptions.getTitleByName(optionsInputValues, item.choice) : '';
	}
}

export default LicenseAgreement;

/*!
 * Copyright © 2016-2018 European Support Limited
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
import i18n from 'nfvo-utils/i18n/i18n.js';
import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import sortByStringProperty from 'nfvo-utils/sortByStringProperty.js';

class VendorSelector extends React.Component {
	static propTypes = {
		finalizedLicenseModelList: PropTypes.array,
		vendorName: PropTypes.string,
		onClose: PropTypes.func.isRequired,
		onConfirm: PropTypes.func.isRequired
	}
	constructor(props){
		super(props);
		const selectedValue = props.finalizedLicenseModelList.length ? props.finalizedLicenseModelList[0].id : '';
		this.state = {
			selectedValue
		};
	}
	submit() {
		const vendor = this.props.finalizedLicenseModelList.find(item => item.id === this.state.selectedValue);
		this.props.onConfirm(vendor.id);
		this.props.onClose();
	}
	render() {
		const {finalizedLicenseModelList, vendorName, onClose} =  this.props;
		const {selectedValue} = this.state;		
		return (
			<div className='vsp-details-vendor-select'>
				<Form 
					onSubmit={() => this.submit()}
					onReset={() => onClose()}
					isValid = {!!selectedValue}
					submitButtonText={i18n('Save')}
					hasButtons={true}>
						<div className='vendor-selector-modal-title'>{`${i18n('The VLM')} '${vendorName}' ${i18n('assigned to this VSP is archived')}.`}</div>
						<div className='vendor-selector-modal-additional-text'>{i18n('If you select a different VLM you will not be able to reselect the archived VLM.')}</div>
						<Input
							data-test-id='vsp-vendor-name-select'
							label={i18n('Vendor')}
							type='select'
							onChange={e => {this.setState({
								selectedValue: e.target.options[e.target.selectedIndex].value
							});}} 
							value={selectedValue}>
							<option key='emtyVendor' value=''>{i18n('please select...')}</option>
							{sortByStringProperty(
								finalizedLicenseModelList,
								'name'
							).map(lm => <option key={lm.id} value={lm.id}>{lm.name}</option>)
							}
						</Input>
				</Form>
			</div>
		);
	}
}

export default VendorSelector;


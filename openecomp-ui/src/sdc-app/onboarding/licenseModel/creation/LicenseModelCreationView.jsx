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
import i18n from 'nfvo-utils/i18n/i18n.js';
import Validator from 'nfvo-utils/Validator.js';
import Input from 'nfvo-components/input/validation/Input.jsx';
import Form from 'nfvo-components/input/validation/Form.jsx';
import {LICENSE_MODEL_CREATION_FORM_NAME} from './LicenseModelCreationConstants.js';

const LicenseModelPropType = PropTypes.shape({
	id: PropTypes.string,
	vendorName: PropTypes.string,
	description: PropTypes.string
});

class LicenseModelCreationView extends React.Component {

	static propTypes = {
		data: LicenseModelPropType,
		VLMNames: PropTypes.object,
		usersList: PropTypes.array,
		onDataChanged: PropTypes.func.isRequired,
		onSubmit: PropTypes.func.isRequired,
		onValidateForm: PropTypes.func.isRequired,
		onCancel: PropTypes.func.isRequired
	};

	render() {
		let {data = {}, onDataChanged, genericFieldInfo} = this.props;
		let {vendorName, description} = data;
		return (
			<div>
				{genericFieldInfo && <Form
					ref='validationForm'
					hasButtons={true}
					onSubmit={ () => this.submit() }
					submitButtonText={i18n('Create')}
					onReset={ () => this.props.onCancel() }
					labledButtons={true}
					isValid={this.props.isFormValid}
					formReady={this.props.formReady}
					onValidateForm={() => this.validate() }>
					<Input
						value={vendorName}
						label={i18n('Vendor Name')}
						data-test-id='vendor-name'
						onChange={vendorName => onDataChanged({vendorName}, LICENSE_MODEL_CREATION_FORM_NAME, {vendorName: name => this.validateName(name)})}
						isValid={genericFieldInfo.vendorName.isValid}
						errorText={genericFieldInfo.vendorName.errorText}
						type='text'
						isRequired={true}
						className='field-section'/>
					<Input
						isRequired={true}
						value={description}
						label={i18n('Description')}
						data-test-id='vendor-description'
						overlayPos='bottom'
						onChange={description => onDataChanged({description}, LICENSE_MODEL_CREATION_FORM_NAME)}
						isValid={genericFieldInfo.description.isValid}
						errorText={genericFieldInfo.description.errorText}
						type='textarea'
						className='field-section'/>
				</Form>}
			</div>
		);
	}


	submit() {
		const {data:licenseModel, usersList} = this.props;
		this.props.onSubmit(licenseModel, usersList);
	}

	validateName(value) {
		const {data: {id}, VLMNames} = this.props;
		const isExists = Validator.isItemNameAlreadyExistsInList({itemId: id, itemName: value, list: VLMNames});

		return !isExists ?  {isValid: true, errorText: ''} :
			{isValid: false, errorText: i18n('License model by the name \'' + value + '\' already exists. License model name must be unique')};
	}

	validate() {
		this.props.onValidateForm(LICENSE_MODEL_CREATION_FORM_NAME);
	}
}

export default LicenseModelCreationView;

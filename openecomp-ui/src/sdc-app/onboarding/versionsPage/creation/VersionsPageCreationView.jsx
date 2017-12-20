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
import Input from 'nfvo-components/input/validation/Input.jsx';
import Form from 'nfvo-components/input/validation/Form.jsx';

const VersionPropType = PropTypes.shape({
	name: PropTypes.string,
	description: PropTypes.string,
	creationMethod: PropTypes.string
});

class VersionsPageCreationView extends React.Component {

	static propTypes = {
		data: VersionPropType,
		availableMethods: PropTypes.array,
		onDataChanged: PropTypes.func.isRequired,
		onSubmit: PropTypes.func.isRequired,
		onCancel: PropTypes.func.isRequired
	};

	render() {
		let {data = {}, genericFieldInfo, baseVersion, onDataChanged, onCancel} = this.props;
		let {additionalInfo: {OptionalCreationMethods}} = baseVersion;
		let {description, creationMethod} = data;

		return (
			<div className='version-creation-page'>
				{ genericFieldInfo && <Form
					ref={(validationForm) => this.validationForm = validationForm}
					hasButtons={true}
					onSubmit={() => this.submit()}
					submitButtonText={i18n('Create')}
					onReset={() => onCancel()}
					labledButtons={true}
					isValid={this.props.isFormValid}
					formReady={this.props.formReady}
					onValidateForm={() => this.validate()}>

					<div className='version-form-row'>
						<Input
							label={i18n('Version Category')}
							value={creationMethod}
							onChange={e => this.onSelectMethod(e)}
							type='select'
							overlayPos='bottom'
							data-test-id='new-version-category'
							isValid={genericFieldInfo.creationMethod.isValid}
							errorText={genericFieldInfo.creationMethod.errorText}
							isRequired>
							<option key='' value=''>{i18n('Please select…')}</option>
							{OptionalCreationMethods.map(method => <option key={method} value={method}>{i18n(method)}</option>)}
						</Input>
					</div>

					<div className='version-form-row'>
						<Input
							label={i18n('Description')}
							value={description}
							type='text'
							overlayPos='bottom'
							data-test-id='new-version-description'
							isValid={genericFieldInfo.description.isValid}
							errorText={genericFieldInfo.description.errorText}
							onChange={description => onDataChanged({description})}
							isRequired />
					</div>

				</Form> }
			</div>
		);
	}

	onSelectMethod(e) {
		const selectedIndex = e.target.selectedIndex;
		const creationMethod = e.target.options[selectedIndex].value;
		this.props.onDataChanged({creationMethod});
	}

	submit() {
		let {baseVersion, data: {description, creationMethod}} = this.props;
		this.props.onSubmit({baseVersion, payload: {description, creationMethod}});
	}

	validate() {
		this.props.onValidateForm();
	}

}

export default VersionsPageCreationView;


































/*
						<div className='software-product-inline-section'>
							<Input
								value={name}
								label={i18n('Name')}
								isRequired={true}
								onChange={name => onDataChanged({name},V_CREATION_FORM_NAME, {name: name => this.validateName(name)})}
								isValid={genericFieldInfo.name.isValid}
								errorText={genericFieldInfo.name.errorText}
								type='text'
								className='field-section'
								data-test-id='new-vsp-name' />
							<Input
								label={i18n('Vendor')}
								type='select'
								value={vendorId}
								isRequired={true}
								disabled={disableVendor}
								onChange={e => this.onSelectVendor(e)}
								isValid={genericFieldInfo.vendorId.isValid}
								errorText={genericFieldInfo.vendorId.errorText}
								className='input-options-select'
								groupClassName='bootstrap-input-options'
								data-test-id='new-vsp-vendor' >
								{vendorList.map(vendor =>
								<option key={vendor.title} value={vendor.enum}>{vendor.title}</option>)}
							</Input>
							<Input
								label={i18n('Category')}
								type='select'
								value={subCategory}
								isRequired={true}
								onChange={e => this.onSelectSubCategory(e)}
								isValid={genericFieldInfo.subCategory.isValid}
								errorText={genericFieldInfo.subCategory.errorText}
								className='input-options-select'
								groupClassName='bootstrap-input-options'
								data-test-id='new-vsp-category' >
								<option key='' value=''>{i18n('please select…')}</option>
								{softwareProductCategories.map(category =>
									category.subcategories &&
									<optgroup
										key={category.name}
										label={category.name}>{category.subcategories.map(sub =>
										<option key={sub.uniqueId} value={sub.uniqueId}>{`${sub.name} (${category.name})`}</option>)}
									</optgroup>)
								}
							</Input>
						</div>
						<div className='software-product-inline-section'>
							<Input
								value={description}
								label={i18n('Description')}
								isRequired={true}
								overlayPos='bottom'
								onChange={description => onDataChanged({description},V_CREATION_FORM_NAME)}
								isValid={genericFieldInfo.description.isValid}
								errorText={genericFieldInfo.description.errorText}
								type='textarea'
								className='field-section'
								data-test-id='new-vsp-description' />
						</div>
					</div>
				</Form>}
			</div>
		);
	}

	getAvailableMethodsList() {
		let {availableMethods} =  this.props;
		return [...availableMethods];
	}

	onSelectVendor(e) {
		const selectedIndex = e.target.selectedIndex;
		const vendorId = e.target.options[selectedIndex].value;
		this.props.onDataChanged({vendorId},V_CREATION_FORM_NAME);
	}

	onSelectSubCategory(e) {
		const selectedIndex = e.target.selectedIndex;
		const subCategory = e.target.options[selectedIndex].value;
		let {softwareProductCategories, onDataChanged} = this.props;
		let category = SoftwareProductCategoriesHelper.getCurrentCategoryOfSubCategory(subCategory, softwareProductCategories);
		onDataChanged({category, subCategory},V_CREATION_FORM_NAME);
	}

	submit() {
		let  {data:softwareProduct, finalizedLicenseModelList} = this.props;
		softwareProduct.vendorName = finalizedLicenseModelList.find(vendor => vendor.id === softwareProduct.vendorId).name;
		this.props.onSubmit(softwareProduct);
	}

	validateName(value) {
		const {data: {id}, VSPNames} = this.props;
		const isExists = Validator.isItemNameAlreadyExistsInList({itemId: id, itemName: value, list: VSPNames});

		return !isExists ?  {isValid: true, errorText: ''} :
			{isValid: false, errorText: i18n('Software product by the name \'' + value + '\' already exists. Software product name must be unique')};
	}

	validate() {
		this.props.onValidateForm(SP_CREATION_FORM_NAME);
	}
}

export default SoftwareProductCreationView;
*/

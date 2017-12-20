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
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';

import {SP_CREATION_FORM_NAME} from './SoftwareProductCreationConstants.js';
import sortByStringProperty from 'nfvo-utils/sortByStringProperty.js';

import SoftwareProductCategoriesHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductCategoriesHelper.js';
import {onboardingMethod as onboardingMethodConst} from '../SoftwareProductConstants.js';

const SoftwareProductPropType = PropTypes.shape({
	id: PropTypes.string,
	name: PropTypes.string,
	description: PropTypes.string,
	category: PropTypes.string,
	subCategory: PropTypes.string,
	vendorId: PropTypes.string
});

class SoftwareProductCreationView extends React.Component {

	static propTypes = {
		data: SoftwareProductPropType,
		finalizedLicenseModelList: PropTypes.array,
		softwareProductCategories: PropTypes.array,
		VSPNames: PropTypes.object,
		usersList: PropTypes.array,
		onDataChanged: PropTypes.func.isRequired,
		onSubmit: PropTypes.func.isRequired,
		onCancel: PropTypes.func.isRequired
	};

	render() {
		let {softwareProductCategories, data = {}, onDataChanged, onCancel, genericFieldInfo, disableVendor} = this.props;
		let {name, description, vendorId, subCategory, onboardingMethod} = data;

		const vendorList = this.getVendorList();
		return (
			<div className='software-product-creation-page'>
				{ genericFieldInfo && <Form
					ref={(validationForm) => this.validationForm = validationForm}
					hasButtons={true}
					onSubmit={() => this.submit() }
					onReset={() => onCancel() }
					labledButtons={true}
					isValid={this.props.isFormValid}
					submitButtonText={i18n('Create')}
					formReady={this.props.formReady}
					onValidateForm={() => this.validate() }>
					<GridSection hasLastColSet>
						<GridItem colSpan='2'>
							<Input
								value={name}
								label={i18n('Name')}
								isRequired={true}
								onChange={name => onDataChanged({name},SP_CREATION_FORM_NAME, {name: name => this.validateName(name)})}
								isValid={genericFieldInfo.name.isValid}
								errorText={genericFieldInfo.name.errorText}
								type='text'
								className='field-section'
								data-test-id='new-vsp-name' />
							<Input
								label={i18n('Vendor')}
								type='select'
								value={vendorId}
								overlayPos='bottom'
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
						</GridItem>
						<GridItem colSpan='2' stretch lastColInRow>
							<Input
								value={description}
								label={i18n('Description')}
								isRequired={true}
								overlayPos='bottom'
								onChange={description => onDataChanged({description},SP_CREATION_FORM_NAME)}
								isValid={genericFieldInfo.description.isValid}
								errorText={genericFieldInfo.description.errorText}
								type='textarea'
								className='field-section'
								data-test-id='new-vsp-description'/>
						</GridItem>
					</GridSection>
					<OnboardingProcedure genericFieldInfo={genericFieldInfo} onboardingMethod={onboardingMethod} onDataChanged={onDataChanged} />
				</Form>}
			</div>
		);
	}

	getVendorList() {
		let {finalizedLicenseModelList} =  this.props;

		return [{enum: '', title: i18n('please select...')}].concat(
			sortByStringProperty(finalizedLicenseModelList, 'name').map(vendor => {
				return {
					enum: vendor.id,
					title: vendor.name
				};
			})
		);
	}

	onSelectVendor(e) {
		const selectedIndex = e.target.selectedIndex;
		const vendorId = e.target.options[selectedIndex].value;
		this.props.onDataChanged({vendorId},SP_CREATION_FORM_NAME);
	}

	onSelectSubCategory(e) {
		const selectedIndex = e.target.selectedIndex;
		const subCategory = e.target.options[selectedIndex].value;
		let {softwareProductCategories, onDataChanged} = this.props;
		let category = SoftwareProductCategoriesHelper.getCurrentCategoryOfSubCategory(subCategory, softwareProductCategories);
		onDataChanged({category, subCategory},SP_CREATION_FORM_NAME);
	}

	submit() {
		let  {data:softwareProduct, finalizedLicenseModelList, usersList} = this.props;
		softwareProduct.vendorName = finalizedLicenseModelList.find(vendor => vendor.id === softwareProduct.vendorId).name;
		this.props.onSubmit(softwareProduct, usersList);
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

const OnboardingProcedure = ({onboardingMethod, onDataChanged, genericFieldInfo}) => {
	return(
		<GridSection title={i18n('Onboarding procedure')}>
			<GridItem colSpan={4}>
				<Input
					label={i18n('Network Package')}
					overlayPos='top'
					isValid={genericFieldInfo.onboardingMethod.isValid}
					checked={onboardingMethod === onboardingMethodConst.NETWORK_PACKAGE}
					errorText={genericFieldInfo.onboardingMethod.errorText}
					onChange={() => onDataChanged({onboardingMethod: onboardingMethodConst.NETWORK_PACKAGE},SP_CREATION_FORM_NAME)}
					type='radio'
					data-test-id='new-vsp-creation-procedure-heat' />
			</GridItem>
			<GridItem colSpan={4}>
				<Input
					label={i18n('Manual')}
					overlayPos='bottom'
					checked={onboardingMethod === onboardingMethodConst.MANUAL}
					isValid={genericFieldInfo.onboardingMethod.isValid}
					errorText={genericFieldInfo.onboardingMethod.errorText}
					onChange={() => onDataChanged({onboardingMethod: onboardingMethodConst.MANUAL},SP_CREATION_FORM_NAME)}
					type='radio'
					data-test-id='new-vsp-creation-procedure-manual' />
			</GridItem>
		</GridSection>
	);
};

export default SoftwareProductCreationView;

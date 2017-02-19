import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';
import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';

import SoftwareProductCategoriesHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductCategoriesHelper.js';


const SoftwareProductPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	category: React.PropTypes.string,
	subCategory: React.PropTypes.string,
	vendorId: React.PropTypes.string
});

class SoftwareProductCreationView extends React.Component {

	static propTypes = {
		data: SoftwareProductPropType,
		finalizedLicenseModelList: React.PropTypes.array,
		softwareProductCategories: React.PropTypes.array,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired
	};

	render() {
		let {softwareProductCategories, data = {}, onDataChanged, onCancel} = this.props;
		let {name, description, vendorId, subCategory} = data;

		const vendorList = this.getVendorList();

		return (
			<div className='software-product-creation-page'>
				<ValidationForm
					ref='validationForm'
					hasButtons={true}
					onSubmit={() => this.submit() }
					onReset={() => onCancel() }
					labledButtons={true}>
					<div className='software-product-form-row'>
						<div className='software-product-inline-section'>
							<ValidationInput
								value={name}
								label={i18n('Name')}
								ref='software-product-name'
								onChange={name => onDataChanged({name})}
								validations={{validateName: true, maxLength: 25, required: true}}
								type='text'
								className='field-section'/>
							<ValidationInput
								onEnumChange={vendorId => onDataChanged({vendorId})}
								value={vendorId}
								label={i18n('Vendor')}
								values={vendorList}
								validations={{required: true}}
								type='select'
								className='field-section'/>
							<ValidationInput
								label={i18n('Category')}
								type='select'
								value={subCategory}
								onChange={subCategory => this.onSelectSubCategory(subCategory)}
								validations={{required: true}}
								className='options-input-category'>
								<option key='' value=''>{i18n('please select…')}</option>
								{softwareProductCategories.map(category =>
									category.subcategories &&
									<optgroup
										key={category.name}
										label={category.name}>{category.subcategories.map(sub =>
										<option key={sub.uniqueId} value={sub.uniqueId}>{`${sub.name} (${category.name})`}</option>)}
									</optgroup>)
								}
							</ValidationInput>
						</div>
						<div className='software-product-inline-section'>
							<ValidationInput
								value={description}
								label={i18n('Description')}
								ref='description'
								onChange={description => onDataChanged({description})}
								validations={{freeEnglishText: true, maxLength: 1000, required: true}}
								type='textarea'
								className='field-section'/>
						</div>
					</div>
				</ValidationForm>
			</div>
		);
	}

	getVendorList() {
		let {finalizedLicenseModelList} =  this.props;

		return [{enum: '', title: i18n('please select...')}].concat(finalizedLicenseModelList.map(vendor => {
			return {
				enum: vendor.id,
				title: vendor.vendorName
			};
		}));
	}

	onSelectSubCategory(subCategory) {
		let {softwareProductCategories, onDataChanged} = this.props;
		let category = SoftwareProductCategoriesHelper.getCurrentCategoryOfSubCategory(subCategory, softwareProductCategories);
		onDataChanged({category, subCategory});
	}

	create(){
		this.refs.validationForm.handleFormSubmit(new Event('dummy'));
	}

	submit() {
		const {data:softwareProduct, finalizedLicenseModelList} = this.props;
		softwareProduct.vendorName = finalizedLicenseModelList.find(vendor => vendor.id === softwareProduct.vendorId).vendorName;
		this.props.onSubmit(softwareProduct);
	}
}

export default SoftwareProductCreationView;

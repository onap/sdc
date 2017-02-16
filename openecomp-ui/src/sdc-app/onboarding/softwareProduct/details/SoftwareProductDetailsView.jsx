import React, {Component, PropTypes} from 'react';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Form from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';
import SoftwareProductCategoriesHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductCategoriesHelper.js';

class SoftwareProductDetails extends Component {

	static propTypes = {
		vendorName: PropTypes.string,
		currentSoftwareProduct: PropTypes.shape({
			id: PropTypes.string,
			name: PropTypes.string,
			description: PropTypes.string,
			category: PropTypes.string,
			subCategory: PropTypes.string,
			vendorId: PropTypes.string,
			vendorName: PropTypes.string,
			licensingVersion: PropTypes.string,
			licensingData: PropTypes.shape({
				licenceAgreement: PropTypes.string,
				featureGroups: PropTypes.array
			})
		}),
		softwareProductCategories: PropTypes.array,
		finalizedLicenseModelList: PropTypes.array,
		licenseAgreementList: PropTypes.array,
		featureGroupsList: PropTypes.array,
		onSubmit: PropTypes.func.isRequired,
		onDataChanged: PropTypes.func.isRequired,
		onValidityChanged: PropTypes.func.isRequired,
		qdata: PropTypes.object.isRequired,
		qschema: PropTypes.object.isRequired,
		onQDataChanged: PropTypes.func.isRequired,
		onVendorParamChanged: PropTypes.func.isRequired
	};

	state = {
		licensingVersionsList: []
	};

	render() {
		let {softwareProductCategories, finalizedLicenseModelList, onDataChanged, featureGroupsList, licenseAgreementList, currentSoftwareProduct} = this.props;
		let {name, description, vendorId, licensingVersion, subCategory, licensingData = {}} = currentSoftwareProduct;
		let licensingVersionsList = this.state.licensingVersionsList.length > 0 ? this.state.licensingVersionsList : this.refreshVendorVersionsList(vendorId);
		let {qdata, qschema, onQDataChanged} = this.props;
		let {isReadOnlyMode} = this.props;

		return (
			<div className='vsp-details-page'>
				<Form
					ref='validationForm'
					className='vsp-general-tab'
					hasButtons={false}
					onSubmit={() => this.props.onSubmit(currentSoftwareProduct, qdata)}
					onValidityChanged={(isValidityData) => this.props.onValidityChanged(isValidityData)}
					isReadOnlyMode={isReadOnlyMode}>
					<div className='section-title general'>{i18n('General')}</div>
					<div className='vsp-general-tab-inline-section'>
						<div className='vsp-general-tab-sub-section'>
							<ValidationInput
								label={i18n('Name')}
								type='text'
								value={name}
								onChange={name => onDataChanged({name})}
								validations={{validateName: true, maxLength: 120, required: true}}
								className='field-section'/>
							<ValidationInput
								label={i18n('Vendor')}
								type='select'
								selectedEnum={vendorId}
								onEnumChange={vendorId => this.onVendorParamChanged({vendorId})}
								className='field-section'>
								{finalizedLicenseModelList.map(lm => <option key={lm.id} value={lm.id}>{lm.vendorName}</option>)}
							</ValidationInput>
							<div className='input-row'>
								<ValidationInput
									label={i18n('Category')}
									type='select'
									selectedEnum={subCategory}
									onEnumChange={subCategory => this.onSelectSubCategory(subCategory)}
									className='field-section'>
									{
										softwareProductCategories.map(category =>
											category.subcategories &&
											<optgroup
												key={category.name}
												label={category.name}>{category.subcategories.map(sub =>
												<option
													key={sub.uniqueId}
													value={sub.uniqueId}>{`${sub.name} (${category.name})`}</option>)}
											</optgroup>
										)
									}
								</ValidationInput>
							</div>
						</div>
						<div className='vsp-general-tab-sub-section input-row'>
							<ValidationInput
								label={i18n('Description')}
								type='textarea'
								value={description}
								onChange={description => onDataChanged({description})}
								className='field-section'
								validations={{required: true}}/>
						</div>
					</div>
					<div className='vsp-general-tab-section licenses'>
						<div className='section-title'>{i18n('Licenses')}</div>
						<div className='vsp-general-tab-inline-section input-row'>
							<ValidationInput
								onEnumChange={licensingVersion => this.onVendorParamChanged({vendorId, licensingVersion})}
								selectedEnum={licensingVersion}
								label={i18n('Licensing Version')}
								values={licensingVersionsList}
								type='select'
								className='field-section'/>
							<ValidationInput
								label={i18n('License Agreement')}
								type='select'
								selectedEnum={licensingData.licenseAgreement}
								className='field-section'
								onEnumChange={(licenseAgreement) => this.onLicensingDataChanged({licenseAgreement, featureGroups: []})}>
								<option key='placeholder' value=''>{i18n('Select...')}</option>
								{licenseAgreementList.map(la => <option value={la.id} key={la.id}>{la.name}</option>)}
							</ValidationInput>
						</div>
						<div className='vsp-general-tab-inline-section input-row'>
							{licensingData.licenseAgreement && (
								<ValidationInput
									type='select'
									isMultiSelect={true}
									onEnumChange={featureGroups => this.onFeatureGroupsChanged({featureGroups})}
									multiSelectedEnum={licensingData.featureGroups}
									name='feature-groups'
									label={i18n('Feature Groups')}
									clearable={false}
									values={featureGroupsList}/>)
							}
						</div>
					</div>
				</Form>
				<Form
					data={qdata}
					schema={qschema}
					onDataChanged={onQDataChanged}
					className='vsp-general-tab'
					hasButtons={false}
					isReadOnlyMode={isReadOnlyMode}>
					<div className='vsp-general-tab-section'>
						<div className='section-title'> {i18n('Availability')} </div>
						<div className='vsp-general-tab-inline-section'>
							<div className='vsp-general-tab-sub-section input-row'>
								<ValidationInput
									label={i18n('Use Availability Zones for High Availability')}
									type='checkbox'
									pointer='/general/availability/useAvailabilityZonesForHighAvailability'/>
							</div>
						</div>
						<div className='section-title'> {i18n('Regions')} </div>
						<div className='vsp-general-tab-inline-section'>
							<div className='vsp-general-tab-sub-section input-row'>
								<ValidationInput
									type='select'
									laebl='Ziv'
									pointer='/general/regionsData/regions'/>
							</div>
						</div>
						<div className='section-title'> {i18n('Storage Data Replication')} </div>
						<div className='vsp-general-tab-inline-section'>
							<div className='vsp-general-tab-sub-section'>
								<ValidationInput
									label={i18n('Storage Replication Size (GB)')}
									type='text'
									pointer='/general/storageDataReplication/storageReplicationSize'
									className='field-section'/>
								<ValidationInput
									label={i18n('Storage Replication Source')}
									type='text'
									pointer='/general/storageDataReplication/storageReplicationSource'
									className='field-section'/>
							</div>
							<div className='vsp-general-tab-sub-section'>
								<ValidationInput
									label={i18n('Storage Replication Frequency (minutes)')}
									type='text'
									pointer='/general/storageDataReplication/storageReplicationFrequency'
									className='field-section'/>
								<ValidationInput
									label={i18n('Storage Replication Destination')}
									type='text'
									pointer='/general/storageDataReplication/storageReplicationDestination'
									className='field-section'/>
							</div>
						</div>
					</div>
				</Form>
			</div>
		);
	}

	onVendorParamChanged({vendorId, licensingVersion}) {
		let {finalizedLicenseModelList, onVendorParamChanged} = this.props;
		if(!licensingVersion) {
			const licensingVersionsList = this.refreshVendorVersionsList(vendorId);
			licensingVersion = licensingVersionsList.length > 0 ? licensingVersionsList[0].enum : '';
		}
		let vendorName = finalizedLicenseModelList.find(licenseModelItem => licenseModelItem.id === vendorId).vendorName || '';
		let deltaData = {
			vendorId,
			vendorName,
			licensingVersion,
			licensingData: {}
		};
		onVendorParamChanged(deltaData);
	}

	refreshVendorVersionsList(vendorId) {
		if(!vendorId) {
			return [];
		}

		let {finalVersions} = this.props.finalizedLicenseModelList.find(vendor => vendor.id === vendorId);

		let licensingVersionsList = [{
			enum: '',
			title: i18n('Select...')
		}];
		if(finalVersions) {
			finalVersions.forEach(version => licensingVersionsList.push({
				enum: version,
				title: version
			}));
		}

		return licensingVersionsList;
	}

	onSelectSubCategory(subCategory) {
		let {softwareProductCategories, onDataChanged} = this.props;
		let category = SoftwareProductCategoriesHelper.getCurrentCategoryOfSubCategory(subCategory, softwareProductCategories);
		onDataChanged({category, subCategory});
	}

	onFeatureGroupsChanged({featureGroups}) {
		this.onLicensingDataChanged({featureGroups});
	}

	onLicensingDataChanged(deltaData) {
		this.props.onDataChanged({
			licensingData: {
				...this.props.currentSoftwareProduct.licensingData,
				...deltaData
			}
		});
	}

	save(){
		return this.refs.validationForm.handleFormSubmit(new Event('dummy'));
	}
}

export default SoftwareProductDetails;

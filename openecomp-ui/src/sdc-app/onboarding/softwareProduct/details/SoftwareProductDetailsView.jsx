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
import PropTypes from 'prop-types';

import i18n from 'nfvo-utils/i18n/i18n.js';
import sortByStringProperty from 'nfvo-utils/sortByStringProperty.js';
import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import InputOptions from 'nfvo-components/input/validation/InputOptions.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import SoftwareProductCategoriesHelper from 'sdc-app/onboarding/softwareProduct/SoftwareProductCategoriesHelper.js';
import {forms} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';

class GeneralSection extends React.Component {
	static propTypes = {
		vendorId: PropTypes.string,
		name: PropTypes.string,
		description: PropTypes.string,
		subCategory: PropTypes.string,
		softwareProductCategories: PropTypes.array,
		finalizedLicenseModelList: PropTypes.array,
		onDataChanged: PropTypes.func.isRequired,
		onVendorParamChanged: PropTypes.func.isRequired,
		onSelectSubCategory: PropTypes.func.isRequired
	};

	onVendorParamChanged(e) {
		const selectedIndex = e.target.selectedIndex;
		const vendorId = e.target.options[selectedIndex].value;
		this.props.onVendorParamChanged({vendorId}, forms.VENDOR_SOFTWARE_PRODUCT_DETAILS);

	}

	onSelectSubCategory(e) {
		const selectedIndex = e.target.selectedIndex;
		const subCategory = e.target.options[selectedIndex].value;
		this.props.onSelectSubCategory(subCategory);
	}

	render (){
		let {genericFieldInfo} = this.props;
		return (
			<div>
			{genericFieldInfo && <GridSection title={i18n('General')} className='grid-section-general'>
			<GridItem>
				<Input
					data-test-id='vsp-name'
					label={i18n('Name')}
					type='text'
					value={this.props.name}
					isRequired={true}
					errorText={genericFieldInfo.name.errorText}
					isValid={genericFieldInfo.name.isValid}
					onChange={name => name.length <= 25 && this.props.onDataChanged({name}, forms.VENDOR_SOFTWARE_PRODUCT_DETAILS)}/>
				<Input
					data-test-id='vsp-vendor-name'
					label={i18n('Vendor')}
					type='select'
					value={this.props.vendorId}
					onChange={e => this.onVendorParamChanged(e)}>
					{sortByStringProperty(
						this.props.finalizedLicenseModelList,
						'name'
					).map(lm => <option key={lm.id} value={lm.id}>{lm.name}</option>)
					}
				</Input>
				<Input
					data-test-id='vsp-category-name'
					label={i18n('Category')}
					type='select'
					value={this.props.subCategory}
					onChange={e => this.onSelectSubCategory(e)}>
					{
						this.props.softwareProductCategories.map(category =>
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
				</Input>
			</GridItem>
			<GridItem colSpan={2} stretch>
				<Input
					data-test-id='vsp-description'
					label={i18n('Description')}
					type='textarea'
					isRequired={true}
					isValid={genericFieldInfo.description.isValid}
					errorText={genericFieldInfo.description.errorText}
					value={this.props.description}
					onChange={description => this.props.onDataChanged({description}, forms.VENDOR_SOFTWARE_PRODUCT_DETAILS)}/>
			</GridItem>
		</GridSection>}
		</div>);
	}
}
class LicensesSection extends React.Component {
	static propTypes = {
		onVendorParamChanged: PropTypes.func.isRequired,
		vendorId: PropTypes.string,
		licensingVersion: PropTypes.string,
		licensingVersionsList: PropTypes.array,
		licensingData: PropTypes.shape({
			licenceAgreement: PropTypes.string,
			featureGroups: PropTypes.array
		}),
		onFeatureGroupsChanged: PropTypes.func.isRequired,
		onLicensingDataChanged: PropTypes.func.isRequired,
		featureGroupsList: PropTypes.array,
		licenseAgreementList: PropTypes.array
	};

	onVendorParamChanged(e) {
		const selectedIndex = e.target.selectedIndex;
		const licensingVersion = e.target.options[selectedIndex].value;
		this.props.onVendorParamChanged({vendorId: this.props.vendorId, licensingVersion}, forms.VENDOR_SOFTWARE_PRODUCT_DETAILS);
	}

	onLicensingDataChanged(e) {
		const selectedIndex = e.target.selectedIndex;
		const licenseAgreement = e.target.options[selectedIndex].value;
		this.props.onLicensingDataChanged({licenseAgreement, featureGroups: []});
	}

	render(){
		return (
			<GridSection title={i18n('Licenses')}>
				<GridItem>
					<Input
						data-test-id='vsp-licensing-version'
						onChange={e => this.onVendorParamChanged(e)}
						value={this.props.licensingVersion || ''}
						label={i18n('Licensing Version')}
						type='select'>
						{this.props.licensingVersionsList.map(version =>
							<option
								key={version.enum}
								value={version.enum}>{version.title}
							</option>
						)}
					</Input>
				</GridItem>
				<GridItem>
					<Input
						data-test-id='vsp-license-agreement'
						label={i18n('License Agreement')}
						type='select'
						value={this.props.licensingData.licenseAgreement ? this.props.licensingData.licenseAgreement : '' }
						onChange={(e) => this.onLicensingDataChanged(e)}>
						<option key='placeholder' value=''>{i18n('Select...')}</option>
						{this.props.licenseAgreementList.map(la => <option value={la.id} key={la.id}>{la.name}</option>)}
					</Input>
				</GridItem>
				<GridItem>
					{this.props.licensingData.licenseAgreement && (
						<InputOptions
							data-test-id='vsp-feature-group'
							type='select'
							isMultiSelect={true}
							onInputChange={()=>{}}
							onEnumChange={featureGroups => this.props.onFeatureGroupsChanged({featureGroups})}
							multiSelectedEnum={this.props.licensingData.featureGroups}
							name='feature-groups'
							label={i18n('Feature Groups')}
							clearable={false}
							values={this.props.featureGroupsList}/>)
					}
				</GridItem>
			</GridSection>
		);
	}
}
const AvailabilitySection = (props) => (
	<GridSection title={i18n('Availability')}>
		<GridItem colSpan={2}>
			<Input
				data-test-id='vsp-use-availability-zone'
				label={i18n('Use Availability Zones for High Availability')}
				type='checkbox'
				checked={props.dataMap['general/availability/useAvailabilityZonesForHighAvailability']}
				value={props.dataMap['general/availability/useAvailabilityZonesForHighAvailability']}
				onChange={(aZone) => props.onQDataChanged({'general/availability/useAvailabilityZonesForHighAvailability' : aZone})} />
		</GridItem>
	</GridSection>
);
const RegionsSection = (props) => (
	<GridSection title={i18n('Regions')}>
		<GridItem>
			<InputOptions
				data-test-id='vsp-regions'
				type='select'
				isMultiSelect={true}
				onInputChange={()=>{}}
				onEnumChange={(regions) => props.onQDataChanged({'general/regionsData/regions' : regions})}
				multiSelectedEnum={props.dataMap['general/regionsData/regions']}
				name='vsp-regions'
				clearable={false}
				values={props.genericFieldInfo['general/regionsData/regions'].enum} />
		</GridItem>
	</GridSection>
);
const StorageDataReplicationSection = (props) => (
	<GridSection title={i18n('Storage Data Replication')}>
		<GridItem>
			<Input
				data-test-id='vsp-storage-rep-size'
				label={i18n('Storage Replication Size (GB)')}
				type='number'
				isValid={props.genericFieldInfo['general/storageDataReplication/storageReplicationSize'].isValid}
				errorText={props.genericFieldInfo['general/storageDataReplication/storageReplicationSize'].errorText}
				value={props.dataMap['general/storageDataReplication/storageReplicationSize']}
				onChange={(sRep) => props.onQDataChanged({'general/storageDataReplication/storageReplicationSize' : sRep})} />
		</GridItem>
		<GridItem>
			<Input
				data-test-id='vsp-storage-rep-source'
				label={i18n('Storage Replication Source')}
				type='text'
				isValid={props.genericFieldInfo['general/storageDataReplication/storageReplicationSource'].isValid}
				errorText={props.genericFieldInfo['general/storageDataReplication/storageReplicationSource'].errorText}
				value={props.dataMap['general/storageDataReplication/storageReplicationSource']}
				onChange={(sRepSource) => props.onQDataChanged({'general/storageDataReplication/storageReplicationSource' : sRepSource})} />
		</GridItem>
		<GridItem>
			<Input
				data-test-id='vsp-storage-rep-freq'
				label={i18n('Storage Replication Freq. (min)')}
				type='number'
				isValid={props.genericFieldInfo['general/storageDataReplication/storageReplicationFrequency'].isValid}
				errorText={props.genericFieldInfo['general/storageDataReplication/storageReplicationFrequency'].errorText}
				value={props.dataMap['general/storageDataReplication/storageReplicationFrequency']}
				onChange={(sRepFreq) => props.onQDataChanged({'general/storageDataReplication/storageReplicationFrequency' : sRepFreq})} />
		</GridItem>
		<GridItem>
			<Input
				data-test-id='vsp-storage-rep-dest'
				label={i18n('Storage Replication Destination')}
				type='text'
				isValid={props.genericFieldInfo['general/storageDataReplication/storageReplicationDestination'].isValid}
				errorText={props.genericFieldInfo['general/storageDataReplication/storageReplicationDestination'].errorText}
				value={props.dataMap['general/storageDataReplication/storageReplicationDestination']}
				onChange={(sRepDest) => props.onQDataChanged({'general/storageDataReplication/storageReplicationDestination' : sRepDest})} />
		</GridItem>
	</GridSection>
);

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
		onQDataChanged: PropTypes.func.isRequired,
		onVendorParamChanged: PropTypes.func.isRequired
	};

	prepareDataForGeneralSection(){
		let {softwareProductCategories, finalizedLicenseModelList, onDataChanged, currentSoftwareProduct, genericFieldInfo} = this.props;
		let {name, description, vendorId, subCategory} = currentSoftwareProduct;
		return {
			name,
			description,
			vendorId,
			subCategory,
			softwareProductCategories,
			finalizedLicenseModelList,
			onDataChanged,
			onVendorParamChanged: args => this.onVendorParamChanged(args),
			onSelectSubCategory: args => this.onSelectSubCategory(args),
			genericFieldInfo
		};

	}

	prepareDataForLicensesSection(){
		let { featureGroupsList, licenseAgreementList, currentSoftwareProduct } = this.props;
		let {vendorId, licensingVersion, licensingData = {}} = currentSoftwareProduct;
		return {
			onVendorParamChanged: args => this.onVendorParamChanged(args),
			vendorId,
			licensingVersion,
			licensingVersionsList: this.buildLicensingVersionsListItems(),
			licensingData,
			onFeatureGroupsChanged: args => this.onFeatureGroupsChanged(args),
			onLicensingDataChanged: args => this.onLicensingDataChanged(args),
			featureGroupsList,
			licenseAgreementList,
		};

	}

	render() {
		let {currentSoftwareProduct} = this.props;
		let {qdata, onQDataChanged, dataMap, qGenericFieldInfo} = this.props;
		let {isReadOnlyMode} = this.props;

		return (
		<div className='vsp-details-page'>
				<Form
					ref={(validationForm) => this.validationForm = validationForm}
					className='vsp-general-tab'
					hasButtons={false}
					formReady={null}
					isValid={this.props.isFormValid}
					onSubmit={() => this.props.onSubmit(currentSoftwareProduct, qdata)}
					onValidityChanged={(isValidityData) => this.props.onValidityChanged(isValidityData)}
					isReadOnlyMode={isReadOnlyMode}>
					<GeneralSection {...this.prepareDataForGeneralSection()}/>
					<LicensesSection {...this.prepareDataForLicensesSection()}/>
					<AvailabilitySection onQDataChanged={onQDataChanged} dataMap={dataMap} />
					<RegionsSection onQDataChanged={onQDataChanged} dataMap={dataMap} genericFieldInfo={qGenericFieldInfo} />
					<StorageDataReplicationSection onQDataChanged={onQDataChanged} dataMap={dataMap} genericFieldInfo={qGenericFieldInfo} />
				</Form>
			</div>
		);
	}

	onVendorParamChanged({vendorId, licensingVersion}) {
		let {finalizedLicenseModelList, onVendorParamChanged} = this.props;
		if(!licensingVersion) {
			const licensingVersionsList = this.buildLicensingVersionsListItems();
			licensingVersion = licensingVersionsList[0].enum;
		}
		let vendorName = finalizedLicenseModelList.find(licenseModelItem => licenseModelItem.id === vendorId).name || '';
		let deltaData = {
			vendorId,
			vendorName,
			licensingVersion,
			licensingData: {}
		};

		onVendorParamChanged(deltaData, forms.VENDOR_SOFTWARE_PRODUCT_DETAILS);

	}

	buildLicensingVersionsListItems() {
		let {licensingVersionsList} = this.props;

		let licensingVersionsListItems = [{
			enum: '',
			title: i18n('Select...')
		}];

		return licensingVersionsListItems.concat(licensingVersionsList.map(version => ({enum: version.id, title: version.name})));
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
		}, forms.VENDOR_SOFTWARE_PRODUCT_DETAILS);
	}

	onSelectSubCategory(subCategory) {
		let {softwareProductCategories, onDataChanged} = this.props;
		let category = SoftwareProductCategoriesHelper.getCurrentCategoryOfSubCategory(subCategory, softwareProductCategories);
		onDataChanged({category, subCategory}, forms.VENDOR_SOFTWARE_PRODUCT_DETAILS);
	}

	save(){
		return this.validationForm.handleFormSubmit(new Event('dummy'));
	}
}

export default SoftwareProductDetails;

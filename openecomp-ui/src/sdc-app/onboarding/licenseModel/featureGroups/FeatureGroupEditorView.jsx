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
import Tabs from 'nfvo-components/input/validation/Tabs.jsx';
import Tab from 'sdc-ui/lib/react/Tab.js';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {TabsForm as Form} from 'nfvo-components/input/validation/Form.jsx';
import DualListboxView from 'nfvo-components/input/dualListbox/DualListboxView.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Validator from 'nfvo-utils/Validator.js';

import {state as FeatureGroupStateConstants, FG_EDITOR_FORM} from './FeatureGroupsConstants.js';

const FeatureGroupsPropType = PropTypes.shape({
	id: PropTypes.string,
	name: PropTypes.string,
	description: PropTypes.string,
	partNumber: PropTypes.string,
	manufacturerReferenceNumber: PropTypes.string,
	entitlementPoolsIds: PropTypes.arrayOf(PropTypes.string),
	licenseKeyGroupsIds: PropTypes.arrayOf(PropTypes.string)
});

const GeneralTab = ({data = {}, onDataChanged, genericFieldInfo, validateName}) => {
	let {name, description, partNumber, manufacturerReferenceNumber} = data;
	return (
			<GridSection hasLastColSet>
				<GridItem colSpan={2}>
					<Input
						groupClassName='field-section'
						onChange={name => onDataChanged({name}, FG_EDITOR_FORM, {name: validateName})}
						label={i18n('Name')}
						data-test-id='create-fg-name'
						value={name}
						name='feature-group-name'
						type='text'
						isRequired={true}
						isValid={genericFieldInfo.name.isValid}
						errorText={genericFieldInfo.name.errorText} />
				</GridItem>
				<GridItem colSpan={2} lastColInRow>
					<Input
						groupClassName='field-section'
						className='description-field'
						onChange={description => onDataChanged({description}, FG_EDITOR_FORM)}
						data-test-id='create-fg-description'
						label={i18n('Description')}
						value={description}
						name='feature-group-description'
						type='textarea'
						isValid={genericFieldInfo.description.isValid}
						errorText={genericFieldInfo.description.errorText} />
				</GridItem>
				<GridItem colSpan={2}>
					<Input
						groupClassName='field-section'
						onChange={partNumber => onDataChanged({partNumber}, FG_EDITOR_FORM)}
						label={i18n('Part Number')}
						data-test-id='create-fg-part-number'
						value={partNumber}
						isRequired={true}
						type='text'
						isValid={genericFieldInfo.partNumber.isValid}
						errorText={genericFieldInfo.partNumber.errorText} />
				</GridItem>
				<GridItem colSpan={2} lastColInRow>
					<Input
						groupClassName='field-section'
						onChange={manufacturerReferenceNumber => onDataChanged({manufacturerReferenceNumber}, FG_EDITOR_FORM)}
						label={i18n('Manufacturer Reference Number')}
						data-test-id='create-fg-reference-number'
						value={manufacturerReferenceNumber}
						isRequired={true}
						type='text'
						isValid={genericFieldInfo.manufacturerReferenceNumber.isValid}
						errorText={genericFieldInfo.manufacturerReferenceNumber.errorText} />
				</GridItem>
			</GridSection>
		);
};

const EntitlementPoolsTab = ({entitlementPoolsList, data, onDataChanged, isReadOnlyMode}) => {
	const dualBoxFilterTitle = {
		left: i18n('Available Entitlement Pools'),
		right: i18n('Selected Entitlement Pools')
	};
	if (entitlementPoolsList.length > 0) {
		return (
			<DualListboxView
				isReadOnlyMode={isReadOnlyMode}
				filterTitle={dualBoxFilterTitle}
				selectedValuesList={data.entitlementPoolsIds}
				availableList={entitlementPoolsList}
				onChange={ selectedValuesList => onDataChanged( { entitlementPoolsIds: selectedValuesList }, FG_EDITOR_FORM )}/>
		);
	} else {
		return (
			<p>{i18n('There are no available entitlement pools')}</p>
		);
	}
};

const LKGTab = ({licenseKeyGroupsList, data, onDataChanged, isReadOnlyMode}) => {
	const dualBoxFilterTitle = {
		left: i18n('Available License Key Groups'),
		right: i18n('Selected License Key Groups')
	};
	if (licenseKeyGroupsList.length > 0) {
		return (
			<DualListboxView
				isReadOnlyMode={isReadOnlyMode}
				filterTitle={dualBoxFilterTitle}
				selectedValuesList={data.licenseKeyGroupsIds}
				availableList={licenseKeyGroupsList}
				onChange={ selectedValuesList => onDataChanged( { licenseKeyGroupsIds: selectedValuesList }, FG_EDITOR_FORM )}/>
		);
	} else {
		return (
			<p>{i18n('There are no available license key groups')}</p>
		);
	}
};

class FeatureGroupEditorView extends React.Component {


	static propTypes = {
		data: FeatureGroupsPropType,
		previousData: FeatureGroupsPropType,
		isReadOnlyMode: PropTypes.bool,
		FGNames: PropTypes.object,

		onSubmit: PropTypes.func,
		onCancel: PropTypes.func,

		selectedTab: PropTypes.number,
		onTabSelect: PropTypes.func,

		entitlementPoolsList: DualListboxView.propTypes.availableList,
		licenseKeyGroupsList: DualListboxView.propTypes.availableList
	};


	static defaultProps = {
		data: {},
		selectedTab: FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.GENERAL,
	};

	state = {
		localEntitlementPoolsListFilter: '',
		localLicenseKeyGroupsListFilter: ''
	};


	render() {
		let {selectedTab, onTabSelect, isReadOnlyMode, invalidTabs, data, onDataChanged, genericFieldInfo, entitlementPoolsList, licenseKeyGroupsList} = this.props;
		return (
			<div>
			{ genericFieldInfo && <Form
				ref='validationForm'
				hasButtons={true}
				onSubmit={ () => this.submit() }
				isValid={this.props.isFormValid}
				formReady={this.props.formReady}
				onValidateForm={() => this.props.onValidateForm(FG_EDITOR_FORM) }
				onReset={ () => this.props.onCancel() }
				labledButtons={true}
				isReadOnlyMode={isReadOnlyMode}
				name='feature-group-validation-form'
				className='license-model-form feature-group-form'>
				<Tabs activeTab={onTabSelect ? selectedTab : undefined} onTabClick={onTabSelect} invalidTabs={invalidTabs} id='vlmFGValTabs' >
					<Tab tabId={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.GENERAL} title={i18n('General')}  >
						<fieldset disabled={isReadOnlyMode}>
							<GeneralTab data={data} onDataChanged={onDataChanged} genericFieldInfo={genericFieldInfo}  validateName={(value)=> this.validateName(value)}/>
						</fieldset>
					</Tab>
					<Tab
						tabId={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.ENTITLEMENT_POOLS}
						title={i18n('Entitlement Pools')} >
						<fieldset disabled={isReadOnlyMode}>
							<EntitlementPoolsTab isReadOnlyMode={isReadOnlyMode} data={data} onDataChanged={onDataChanged} entitlementPoolsList={entitlementPoolsList} />
						</fieldset>
					</Tab>
					<Tab
						tabId={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.LICENSE_KEY_GROUPS}
						title={i18n('License Key Groups')} >
						<fieldset disabled={isReadOnlyMode}>
							<LKGTab isReadOnlyMode={isReadOnlyMode} data={data} onDataChanged={onDataChanged} licenseKeyGroupsList={licenseKeyGroupsList} />
						</fieldset>
					</Tab>
				</Tabs>

				</Form> }
			</div>
		);
	}

	submit() {
		const {data: featureGroup, previousData: previousFeatureGroup} = this.props;
		this.props.onSubmit(previousFeatureGroup, featureGroup);
	}

	validateName(value) {
		const {data: {id}, FGNames} = this.props;
		const isExists = Validator.isItemNameAlreadyExistsInList({itemId: id, itemName: value, list: FGNames});

		return !isExists ?  {isValid: true, errorText: ''} :
			{isValid: false, errorText: i18n('Feature group by the name \'' + value + '\' already exists. Feature group name must be unique')};
	}
}


export default FeatureGroupEditorView;

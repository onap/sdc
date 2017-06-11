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
import Tabs from 'nfvo-components/input/validation/Tabs.jsx';
import Tab from 'react-bootstrap/lib/Tab.js';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {TabsForm as Form} from 'nfvo-components/input/validation/Form.jsx';
import DualListboxView from 'nfvo-components/input/dualListbox/DualListboxView.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Validator from 'nfvo-utils/Validator.js';

import {state as FeatureGroupStateConstants, FG_EDITOR_FORM} from './FeatureGroupsConstants.js';

const FeatureGroupsPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	partNumber: React.PropTypes.string,
	entitlementPoolsIds: React.PropTypes.arrayOf(React.PropTypes.string),
	licenseKeyGroupsIds: React.PropTypes.arrayOf(React.PropTypes.string)
});

const GeneralTab = ({data = {}, onDataChanged, genericFieldInfo, validateName}) => {
	let {name, description, partNumber} = data;
	return (
			<GridSection>
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
					<Input
						groupClassName='field-section'
						className='description-field'
						onChange={description => onDataChanged({description}, FG_EDITOR_FORM)}
						data-test-id='create-fg-description'
						label={i18n('Description')}
						value={description}
						name='feature-group-description'
						type='textarea'
						isRequired={true}
						isValid={genericFieldInfo.description.isValid}
						errorText={genericFieldInfo.description.errorText} />
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
			<p>{i18n('There is no available entitlement pools')}</p>
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
			<p>{i18n('There is no available licsense key groups')}</p>
		);
	}
};

class FeatureGroupEditorView extends React.Component {


	static propTypes = {
		data: FeatureGroupsPropType,
		previousData: FeatureGroupsPropType,
		isReadOnlyMode: React.PropTypes.bool,
		FGNames: React.PropTypes.object,

		onSubmit: React.PropTypes.func,
		onCancel: React.PropTypes.func,

		selectedTab: React.PropTypes.number,
		onTabSelect: React.PropTypes.func,

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
				className='feature-group-form'>
				<Tabs activeKey={onTabSelect ? selectedTab : undefined} onSelect={onTabSelect} invalidTabs={invalidTabs} id='vlmFGValTabs' >
					<Tab eventKey={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.GENERAL} title={i18n('General')}  >
						<fieldset disabled={isReadOnlyMode}>
							<GeneralTab data={data} onDataChanged={onDataChanged} genericFieldInfo={genericFieldInfo}  validateName={(value)=> this.validateName(value)}/>
						</fieldset>
					</Tab>
					<Tab
						eventKey={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.ENTITLEMENT_POOLS}
						title={i18n('Entitlement Pools')} >
						<fieldset disabled={isReadOnlyMode}>
							<EntitlementPoolsTab isReadOnlyMode={isReadOnlyMode} data={data} onDataChanged={onDataChanged} entitlementPoolsList={entitlementPoolsList} />
						</fieldset>
					</Tab>
					<Tab
						eventKey={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.LICENSE_KEY_GROUPS}
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

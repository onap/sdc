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
import i18n from 'nfvo-utils/i18n/i18n.js';
import Validator from 'nfvo-utils/Validator.js';

import Tabs from 'sdc-ui/lib/react/Tabs.js';
import Tab from 'sdc-ui/lib/react/Tab.js';

import Button from 'sdc-ui/lib/react/Button.js';
import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {optionsInputValues as licenseKeyGroupOptionsInputValues, LKG_FORM_NAME, tabIds} from './LicenseKeyGroupsConstants.js';
import {optionsInputValues as LicenseModelOptionsInputValues} from '../LicenseModelConstants.js';
import {validateStartDate, thresholdValueValidation} from '../LicenseModelValidations.js';
import {other as optionInputOther} from 'nfvo-components/input/validation/InputOptions.jsx';
import InputOptions from 'nfvo-components/input/validation/InputOptions.jsx';

import {DATE_FORMAT} from 'sdc-app/onboarding/OnboardingConstants.js';

import LicenseKeyGroupsLimits from './LicenseKeyGroupsLimits.js';
import {limitType, NEW_LIMIT_TEMP_ID} from '../limits/LimitEditorConstants.js';

 const LicenseKeyGroupPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	increments: React.PropTypes.string,
	operationalScope: React.PropTypes.shape({
		choices: React.PropTypes.array,
		other: React.PropTypes.string
	}),
	type: React.PropTypes.string,
	 thresholdUnits: React.PropTypes.string,
	 thresholdValue: React.PropTypes.number,
	 startDate: React.PropTypes.string,
	 expiryDate: React.PropTypes.string
});

const LicenseKeyGroupFormContent = ({data, onDataChanged, genericFieldInfo, validateName, validateStartDate, thresholdValueValidation}) => {
	let {name, description, increments, operationalScope, type, thresholdUnits, thresholdValue, startDate, expiryDate} = data;
	return (
		<GridSection>
			<GridItem colSpan={2}>
				<Input
					onChange={name => onDataChanged({name}, LKG_FORM_NAME, {name: validateName})}
					label={i18n('Name')}
					data-test-id='create-lkg-name'
					value={name}
					isValid={genericFieldInfo.name.isValid}
					errorText={genericFieldInfo.name.errorText}
					isRequired={true}
					type='text'/>
			</GridItem>
			<GridItem colSpan={2}>
				<InputOptions
					onInputChange={()=>{}}
					isMultiSelect={true}
					onEnumChange={operationalScope => onDataChanged({operationalScope:{choices: operationalScope, other: ''}},
						LKG_FORM_NAME)}
					onOtherChange={operationalScope => onDataChanged({operationalScope:{choices: [optionInputOther.OTHER],
						other: operationalScope}}, LKG_FORM_NAME)}
					label={i18n('Operational Scope')}
					data-test-id='create-lkg-operational-scope'
					type='select'
					multiSelectedEnum={operationalScope && operationalScope.choices}
					otherValue={operationalScope && operationalScope.other}
					values={licenseKeyGroupOptionsInputValues.OPERATIONAL_SCOPE}
					isValid={genericFieldInfo.operationalScope.isValid}
					errorText={genericFieldInfo.operationalScope.errorText} />
			</GridItem>
			<GridItem colSpan={2}>
				<Input
					onChange={description => onDataChanged({description}, LKG_FORM_NAME)}
					label={i18n('Description')}
					data-test-id='create-lkg-description'
					value={description}
					isValid={genericFieldInfo.description.isValid}
					errorText={genericFieldInfo.description.errorText}
					type='textarea'
					overlayPos='bottom' />
			</GridItem>
			<GridItem colSpan={2}>
				<Input
					isRequired={true}
					onChange={e => { const selectedIndex = e.target.selectedIndex;
						const val = e.target.options[selectedIndex].value;
						onDataChanged({type: val}, LKG_FORM_NAME);}}
					value={type}
					label={i18n('Type')}
					data-test-id='create-lkg-type'
					isValid={genericFieldInfo.type.isValid}
					errorText={genericFieldInfo.type.errorText}
					groupClassName='bootstrap-input-options'
					className='input-options-select'
					type='select' >
					{
						licenseKeyGroupOptionsInputValues.TYPE.map(type =>
						(<option key={type.enum} value={type.enum}>{type.title}</option>))
					}
				</Input>
			</GridItem>
			<GridItem>
				<Input
					onChange={e => {
						// setting the unit to the correct value
						const selectedIndex = e.target.selectedIndex;
						const val = e.target.options[selectedIndex].value;
						onDataChanged({thresholdUnits: val}, LKG_FORM_NAME);
						// TODO make sure that the value is valid too
						onDataChanged({thresholdValue: thresholdValue}, LKG_FORM_NAME,{thresholdValue : thresholdValueValidation});}

					}
					value={thresholdUnits}
					label={i18n('Threshold Units')}
					data-test-id='create-ep-threshold-units'
					isValid={genericFieldInfo.thresholdUnits.isValid}
					errorText={genericFieldInfo.thresholdUnits.errorText}
					groupClassName='bootstrap-input-options'
					className='input-options-select'
					type='select' >
					{LicenseModelOptionsInputValues.THRESHOLD_UNITS.map(mtype =>
						<option key={mtype.enum} value={mtype.enum}>{`${mtype.title}`}</option>)}
				</Input>
			</GridItem>
			<GridItem>
				<Input
					className='entitlement-pools-form-row-threshold-value'
					onChange={thresholdValue => onDataChanged({thresholdValue}, LKG_FORM_NAME,
						{thresholdValue : thresholdValueValidation})}
					label={i18n('Threshold Value')}
					isValid={genericFieldInfo.thresholdValue.isValid}
					errorText={genericFieldInfo.thresholdValue.errorText}
					data-test-id='create-ep-threshold-value'
					value={thresholdValue}
					type='text'/>
			</GridItem>
				<GridItem>
				<Input
					type='date'
					label={i18n('Start Date')}
					value={startDate}
					dateFormat={DATE_FORMAT}
					startDate={startDate}
					endDate={expiryDate}
					onChange={startDate => onDataChanged(
						{startDate: startDate ? startDate.format(DATE_FORMAT) : ''},
						LKG_FORM_NAME,
						{startDate: validateStartDate}
					)}
					isValid={genericFieldInfo.startDate.isValid}
					errorText={genericFieldInfo.startDate.errorText}
					selectsStart/>
			</GridItem>
			<GridItem>
				<Input
					type='date'
					label={i18n('Expiry Date')}
					value={expiryDate}
					dateFormat={DATE_FORMAT}
					startDate={startDate}
					endDate={expiryDate}
					onChange={expiryDate => {
						onDataChanged({expiryDate: expiryDate ? expiryDate.format(DATE_FORMAT) : ''}, LKG_FORM_NAME);
						onDataChanged({startDate}, LKG_FORM_NAME, {startDate: validateStartDate});
					}}
					isValid={genericFieldInfo.expiryDate.isValid}
					errorText={genericFieldInfo.expiryDate.errorText}
					selectsEnd/>
			</GridItem>
			<GridItem colSpan={2}>
				<Input
					onChange={increments => onDataChanged({increments}, LKG_FORM_NAME)}
					label={i18n('Increments')}
					value={increments}
					data-test-id='create-ep-increments'
					type='text'/>
			</GridItem>
		</GridSection>
	);
};

class LicenseKeyGroupsEditorView extends React.Component {
	static propTypes = {
		data: LicenseKeyGroupPropType,
		previousData: LicenseKeyGroupPropType,
		LKGNames: React.PropTypes.object,
		isReadOnlyMode: React.PropTypes.bool,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired
	};

	static defaultProps = {
		data: {}
	};

	componentDidUpdate(prevProps) {				
		if (this.props.formReady && this.props.formReady !== prevProps.formReady) { // if form validation succeeded -> continue with submit
			this.submit();
		}
	}

	state = {
		localFeatureGroupsListFilter: '',
		selectedTab: tabIds.GENERAL,
		selectedLimit: ''
	};

	render() {
		let {data = {}, onDataChanged, isReadOnlyMode, onCloseLimitEditor, genericFieldInfo, limitsList = []} = this.props;
		let {selectedTab} = this.state;
		const isTabsDisabled = !data.id || !this.props.isFormValid;
		return (
			<div className='license-keygroup-editor'>
				<Tabs
					type='menu' 
					activeTab={selectedTab} 
					onTabClick={(tabIndex)=>{
						if (tabIndex === tabIds.ADD_LIMIT_BUTTON)  {
							this.onAddLimit();
						} else {
							this.setState({selectedTab: tabIndex});
							onCloseLimitEditor();
							this.setState({selectedLimit: ''});
						}
					}} 
					invalidTabs={[]}>
					<Tab tabId={tabIds.GENERAL} data-test-id='general-tab' title={i18n('General')}>
						{ genericFieldInfo &&
							<Form
								ref='validationForm'
								hasButtons={false}
								isValid={this.props.isFormValid}
								formReady={this.props.formReady}
								onValidateForm={() => this.props.onValidateForm(LKG_FORM_NAME) }
								labledButtons={true}
								isReadOnlyMode={isReadOnlyMode}
								className='license-model-form license-key-groups-form'>
									<LicenseKeyGroupFormContent
										data={data}
										onDataChanged={onDataChanged}
										genericFieldInfo={genericFieldInfo}
										validateName={(value)=> this.validateName(value)}
										validateStartDate={(value, state)=> validateStartDate(value, state)}
										thresholdValueValidation={(value, state) => thresholdValueValidation(value, state)}/>
							</Form>}

					</Tab>
					<Tab tabId={tabIds.SP_LIMITS} disabled={isTabsDisabled} data-test-id='general-tab' title={i18n('SP Limits')}>
						{selectedTab === tabIds.SP_LIMITS &&
							<LicenseKeyGroupsLimits 
								limitType={limitType.SERVICE_PROVIDER} 
								limitsList={limitsList.filter(item => item.type === limitType.SERVICE_PROVIDER)}
								selectedLimit={this.state.selectedLimit}
								onCloseLimitEditor={() => this.onCloseLimitEditor()}
								onSelectLimit={limit => this.onSelectLimit(limit)}
								isReadOnlyMode={isReadOnlyMode} />}
					</Tab>
					<Tab tabId={tabIds.VENDOR_LIMITS} disabled={isTabsDisabled} data-test-id='general-tab' title={i18n('Vendor Limits')}>
						{selectedTab === tabIds.VENDOR_LIMITS && 
							<LicenseKeyGroupsLimits 
								limitType={limitType.VENDOR} 
								limitsList={limitsList.filter(item => item.type === limitType.VENDOR)}
								selectedLimit={this.state.selectedLimit}
								onCloseLimitEditor={() => this.onCloseLimitEditor()}
								onSelectLimit={limit => this.onSelectLimit(limit)}
								isReadOnlyMode={isReadOnlyMode} />}
					</Tab>
					{selectedTab !== tabIds.GENERAL ? 
							<Button
								className='add-limit-button'
								tabId={tabIds.ADD_LIMIT_BUTTON}
								btnType='link'
								iconName='plus'
								disabled={this.state.selectedLimit || isReadOnlyMode}>
								{i18n('Add Limit')}
							</Button>
						:
						<div></div> // Render empty div to not break tabs
					}
				</Tabs>
				
				<GridSection className='license-model-modal-buttons license-key-group-editor-buttons'>
					{!this.state.selectedLimit &&
						<Button btnType='default' disabled={!this.props.isFormValid || isReadOnlyMode} onClick={() => this.submit()} type='reset'>
							{i18n('Save')}
						</Button>
					}
					<Button btnType={this.state.selectedLimit ? 'default' : 'outline'} onClick={() => this.props.onCancel()} type='reset'>
						{i18n('Cancel')}
					</Button>
				</GridSection>
			</div>

		);
	}

	submit() {
		const {data: licenseKeyGroup, previousData: previousLicenseKeyGroup, formReady, onValidateForm, onSubmit} = this.props;
		if (!formReady) {
			onValidateForm(LKG_FORM_NAME);
		} else {
			onSubmit({licenseKeyGroup, previousLicenseKeyGroup});
		}
	}

	validateName(value) {
		const {data: {id}, LKGNames} = this.props;
		const isExists = Validator.isItemNameAlreadyExistsInList({itemId: id, itemName: value, list: LKGNames});

		return !isExists ?  {isValid: true, errorText: ''} :
			{isValid: false, errorText: i18n('License key group by the name \'' + value + '\' already exists. License key group name must be unique')};
	}

	onSelectLimit(limit) {
		if (limit.id === this.state.selectedLimit) {
			this.setState({selectedLimit: ''});
			return;
		}
		this.setState({selectedLimit: limit.id});
		this.props.onOpenLimitEditor(limit);
	}

	onCloseLimitEditor() {
		this.setState({selectedLimit: ''});
		this.props.onCloseLimitEditor();
	}

	onAddLimit() {
		this.setState({selectedLimit: NEW_LIMIT_TEMP_ID});
		this.props.onOpenLimitEditor();
	}
}

export default LicenseKeyGroupsEditorView;

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
import InputOptions from 'nfvo-components/input/validation/InputOptions.jsx';
import Form from 'nfvo-components/input/validation/Form.jsx';
import Button from 'sdc-ui/lib/react/Button.js';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {optionsInputValues as  EntitlementPoolsOptionsInputValues, SP_ENTITLEMENT_POOL_FORM, tabIds}  from  './EntitlementPoolsConstants.js';
import {optionsInputValues as LicenseModelOptionsInputValues} from '../LicenseModelConstants.js';
import {validateStartDate, thresholdValueValidation} from '../LicenseModelValidations.js';
import {DATE_FORMAT} from 'sdc-app/onboarding/OnboardingConstants.js';
import {other as optionInputOther} from 'nfvo-components/input/validation/InputOptions.jsx';
import Tabs from 'sdc-ui/lib/react/Tabs.js';
import Tab from 'sdc-ui/lib/react/Tab.js';
import EntitlementPoolsLimits from './EntitlementPoolsLimits.js';
import {limitType, NEW_LIMIT_TEMP_ID} from '../limits/LimitEditorConstants.js';

const EntitlementPoolPropType = PropTypes.shape({
	id: PropTypes.string,
	name: PropTypes.string,
	description: PropTypes.string,
	operationalScope: PropTypes.shape({
		choices: PropTypes.array,
		other: PropTypes.string
	}),
	thresholdUnits: PropTypes.string,
	thresholdValue: PropTypes.string,
	increments: PropTypes.string,
	startDate: PropTypes.string,
	expiryDate: PropTypes.string
});

const EntitlementPoolsFormContent = ({data, genericFieldInfo, onDataChanged, validateName,
	 thresholdValueValidation, validateStartDate}) => {

	let {name, description, operationalScope, thresholdUnits, thresholdValue,
		increments, startDate, expiryDate} = data;
	return (
		<GridSection hasLastColSet>
			<GridItem colSpan={2}>
				<Input
					onChange={name => onDataChanged({name}, SP_ENTITLEMENT_POOL_FORM, {name: validateName})}
					isValid={genericFieldInfo.name.isValid}
					isRequired={true}
					errorText={genericFieldInfo.name.errorText}
					label={i18n('Name')}
					value={name}
					data-test-id='create-ep-name'
					type='text'/>
			</GridItem>
			<GridItem colSpan={2} lastColInRow>
				<InputOptions
					onInputChange={()=>{}}
					isMultiSelect={true}
					onEnumChange={operationalScope => onDataChanged({operationalScope:{choices: operationalScope, other: ''}},
						SP_ENTITLEMENT_POOL_FORM)}
					onOtherChange={operationalScope => onDataChanged({operationalScope:{choices: [optionInputOther.OTHER],
						other: operationalScope}}, SP_ENTITLEMENT_POOL_FORM)}
					label={i18n('Operational Scope')}
					data-test-id='create-ep-operational-scope'
					type='select'
					multiSelectedEnum={operationalScope && operationalScope.choices}
					otherValue={operationalScope && operationalScope.other}
					values={EntitlementPoolsOptionsInputValues.OPERATIONAL_SCOPE}
					isValid={genericFieldInfo.operationalScope.isValid}
					errorText={genericFieldInfo.operationalScope.errorText} />
			</GridItem>
			<GridItem colSpan={2} stretch>
				<Input
					onChange={description => onDataChanged({description}, SP_ENTITLEMENT_POOL_FORM)}
					isValid={genericFieldInfo.description.isValid}
					errorText={genericFieldInfo.description.errorText}
					label={i18n('Description')}
					value={description}
					data-test-id='create-ep-description'
					type='textarea'/>
			</GridItem>
			<GridItem colSpan={2} lastColInRow>
				<div className='threshold-section'>
					<Input
						onChange={e => {
							// setting the unit to the correct value
							const selectedIndex = e.target.selectedIndex;
							const val = e.target.options[selectedIndex].value;
							onDataChanged({thresholdUnits: val}, SP_ENTITLEMENT_POOL_FORM);
							// TODO make sure that the value is valid too
							if(thresholdValue && thresholdValue !== '') {
								onDataChanged({thresholdValue: thresholdValue}, SP_ENTITLEMENT_POOL_FORM,{thresholdValue : thresholdValueValidation});
							}}

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

					<Input
						className='entitlement-pools-form-row-threshold-value'
						onChange={thresholdValue => onDataChanged({thresholdValue}, SP_ENTITLEMENT_POOL_FORM,
							{thresholdValue : thresholdValueValidation})}
						label={i18n('Threshold Value')}
						isValid={genericFieldInfo.thresholdValue.isValid}
						errorText={genericFieldInfo.thresholdValue.errorText}
						data-test-id='create-ep-threshold-value'
						value={thresholdValue}
						type='text'/>
				</div>
				<Input
					onChange={increments => onDataChanged({increments}, SP_ENTITLEMENT_POOL_FORM)}
					label={i18n('Increments')}
					value={increments}
					data-test-id='create-ep-increments'
					type='text'/>
				<div className='date-section'>
					<Input
						type='date'
						label={i18n('Start Date')}
						value={startDate}
						dateFormat={DATE_FORMAT}
						startDate={startDate}
						endDate={expiryDate}
						onChange={startDate => onDataChanged(
							{startDate: startDate ? startDate.format(DATE_FORMAT) : ''},
							SP_ENTITLEMENT_POOL_FORM,
							{startDate: validateStartDate}
						)}
						isValid={genericFieldInfo.startDate.isValid}
						errorText={genericFieldInfo.startDate.errorText}
						selectsStart/>
					<Input
						type='date'
						label={i18n('Expiry Date')}
						value={expiryDate}
						dateFormat={DATE_FORMAT}
						startDate={startDate}
						endDate={expiryDate}
						onChange={expiryDate => {
							onDataChanged({expiryDate: expiryDate ? expiryDate.format(DATE_FORMAT) : ''}, SP_ENTITLEMENT_POOL_FORM);
							onDataChanged({startDate}, SP_ENTITLEMENT_POOL_FORM, {startDate: validateStartDate});
						}}
						isValid={genericFieldInfo.expiryDate.isValid}
						errorText={genericFieldInfo.expiryDate.errorText}
						selectsEnd/>
				</div>
			</GridItem>
		</GridSection>
	);
};

class EntitlementPoolsEditorView extends React.Component {

	static propTypes = {
		data: EntitlementPoolPropType,
		previousData: EntitlementPoolPropType,
		EPNames: PropTypes.object,
		isReadOnlyMode: PropTypes.bool,
		onDataChanged: PropTypes.func.isRequired,
		onSubmit: PropTypes.func.isRequired,
		onCancel: PropTypes.func.isRequired
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
		selectedTab: tabIds.GENERAL,
		selectedLimit: ''
	};

	render() {
		let {data = {}, onDataChanged, isReadOnlyMode, genericFieldInfo, onCloseLimitEditor, limitsList = []} = this.props;
		const {selectedTab} = this.state;
		const isTabsDisabled = !data.id || !this.props.isFormValid;

		return (
			<div>
			<Tabs
				type='menu'
				activeTab={selectedTab}
				onTabClick={(tabIndex)=>{
					if (tabIndex === tabIds.ADD_LIMIT_BUTTON)  {
						this.onAddLimit();
					} else {
						this.setState({selectedTab: tabIndex});
						this.setState({selectedLimit: ''});
						onCloseLimitEditor();
					}
				}}
				invalidTabs={[]}>
				<Tab tabId={tabIds.GENERAL} data-test-id='general-tab' title={i18n('General')}>
					{
						genericFieldInfo && <Form
							ref='validationForm'
							hasButtons={false}
							labledButtons={false}
							isReadOnlyMode={isReadOnlyMode}
							isValid={this.props.isFormValid}
							formReady={this.props.formReady}
							onValidateForm={() => this.props.onValidateForm(SP_ENTITLEMENT_POOL_FORM) }
							className='license-model-form entitlement-pools-form'>
							<EntitlementPoolsFormContent
								data={data}
								genericFieldInfo={genericFieldInfo}
								onDataChanged={onDataChanged}
								validateName={(value) => this.validateName(value)}
								validateStartDate={(value, state) => validateStartDate(value, state)}
								thresholdValueValidation={(value, state) => thresholdValueValidation(value, state)}/>
						</Form>
					}
				</Tab>
				<Tab disabled={isTabsDisabled} tabId={tabIds.SP_LIMITS} data-test-id='sp-limits-tab' title={i18n('SP Limits')}>
					{selectedTab === tabIds.SP_LIMITS &&
						<EntitlementPoolsLimits
							isReadOnlyMode={isReadOnlyMode}
							limitType={limitType.SERVICE_PROVIDER}
							limitsList={limitsList.filter(item => item.type === limitType.SERVICE_PROVIDER)}
							selectedLimit={this.state.selectedLimit}
							onCloseLimitEditor={() => this.onCloseLimitEditor()}
							onSelectLimit={limit => this.onSelectLimit(limit)}/>}
				</Tab>
				<Tab disabled={isTabsDisabled} tabId={tabIds.VENDOR_LIMITS} data-test-id='vendor-limits-tab' title={i18n('Vendor Limits')}>
					{selectedTab === tabIds.VENDOR_LIMITS &&
						<EntitlementPoolsLimits
							isReadOnlyMode={isReadOnlyMode}
							limitType={limitType.VENDOR}
							limitsList={limitsList.filter(item => item.type === limitType.VENDOR)}
							selectedLimit={this.state.selectedLimit}
							onCloseLimitEditor={() => this.onCloseLimitEditor()}
							onSelectLimit={limit => this.onSelectLimit(limit)}/>}
				</Tab>
				{
					selectedTab !== tabIds.GENERAL ?
						<Button
							disabled={this.state.selectedLimit || isReadOnlyMode}
							className='add-limit-button'
							tabId={tabIds.ADD_LIMIT_BUTTON}
							btnType='link'
							iconName='plus'>
							{i18n('Add Limit')}
						</Button>
					:
						<div></div> // Render empty div to not break tabs
				}
			</Tabs>
			<GridSection className='license-model-modal-buttons entitlement-pools-editor-buttons'>
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
		const {data: entitlementPool, previousData: previousEntitlementPool, formReady} = this.props;
		if (!formReady) {
			this.props.onValidateForm(SP_ENTITLEMENT_POOL_FORM);
		} else {
			this.props.onSubmit({entitlementPool, previousEntitlementPool});
		}
	}

	validateName(value) {
		const {data: {id}, EPNames} = this.props;
		const isExists = Validator.isItemNameAlreadyExistsInList({itemId: id, itemName: value, list: EPNames});

		return !isExists ?  {isValid: true, errorText: ''} :
		{isValid: false, errorText: i18n('Entitlement pool by the name \'' + value + '\' already exists. Entitlement pool name must be unique')};
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

export default EntitlementPoolsEditorView;

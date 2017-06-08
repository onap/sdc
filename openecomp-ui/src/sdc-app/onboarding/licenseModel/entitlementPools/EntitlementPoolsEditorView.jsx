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

import Input from 'nfvo-components/input/validation/Input.jsx';
import InputOptions from 'nfvo-components/input/validation/InputOptions.jsx';
import Form from 'nfvo-components/input/validation/Form.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {optionsInputValues as  EntitlementPoolsOptionsInputValues, thresholdUnitType, SP_ENTITLEMENT_POOL_FORM}  from  './EntitlementPoolsConstants.js';
import {other as optionInputOther} from 'nfvo-components/input/inputOptions/InputOptions.jsx';

const EntitlementPoolPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	manufacturerReferenceNumber: React.PropTypes.string,
	operationalScope: React.PropTypes.shape({
		choices: React.PropTypes.array,
		other: React.PropTypes.string
	}),
	aggregationFunction: React.PropTypes.shape({
		choice: React.PropTypes.string,
		other: React.PropTypes.string
	}),
	increments: React.PropTypes.string,
	time: React.PropTypes.shape({
		choice: React.PropTypes.string,
		other: React.PropTypes.string
	}),
	entitlementMetric: React.PropTypes.shape({
		choice: React.PropTypes.string,
		other: React.PropTypes.string
	})
});

const EntitlementPoolsFormContent = ({data, genericFieldInfo, onDataChanged, validateName, validateChoiceWithOther, validateTimeOtherValue, thresholdValueValidation}) => {
	let {
		name, description, manufacturerReferenceNumber, operationalScope , aggregationFunction,  thresholdUnits, thresholdValue,
		increments, time, entitlementMetric} = data;

	return (
		<GridSection>
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
			<GridItem colSpan={2}>
				<InputOptions
					onInputChange={()=>{}}
					isMultiSelect={true}

					isRequired={true}
					onEnumChange={operationalScope => onDataChanged({operationalScope:{choices: operationalScope, other: ''}},
						SP_ENTITLEMENT_POOL_FORM, {operationalScope: validateChoiceWithOther})}
					onOtherChange={operationalScope => onDataChanged({operationalScope:{choices: [optionInputOther.OTHER],
						other: operationalScope}}, SP_ENTITLEMENT_POOL_FORM, {operationalScope: validateChoiceWithOther})}
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
					isRequired={true}
					data-test-id='create-ep-description'
					type='textarea'/>
			</GridItem>
			<GridItem colSpan={2}>
				<div className='threshold-section'>
					<Input
						isRequired={true}
						onChange={e => {
							// setting the unit to the correct value
							const selectedIndex = e.target.selectedIndex;
							const val = e.target.options[selectedIndex].value;
							onDataChanged({thresholdUnits: val}, SP_ENTITLEMENT_POOL_FORM);
							// TODO make sure that the value is valid too
							onDataChanged({thresholdValue: thresholdValue}, SP_ENTITLEMENT_POOL_FORM,{thresholdValue : thresholdValueValidation});}

						}
						value={thresholdUnits}
						label={i18n('Threshold Units')}
						data-test-id='create-ep-threshold-units'
						isValid={genericFieldInfo.thresholdUnits.isValid}
						errorText={genericFieldInfo.thresholdUnits.errorText}
						groupClassName='bootstrap-input-options'
						className='input-options-select'
						type='select' >
						{EntitlementPoolsOptionsInputValues.THRESHOLD_UNITS.map(mtype =>
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
						isRequired={true}
						type='text'/>
				</div>
				<InputOptions
					onInputChange={()=>{}}
					isMultiSelect={false}
					isRequired={true}
					onEnumChange={entitlementMetric => onDataChanged({entitlementMetric:{choice: entitlementMetric, other: ''}},
						SP_ENTITLEMENT_POOL_FORM, {entitlementMetric: validateChoiceWithOther})}
					onOtherChange={entitlementMetric => onDataChanged({entitlementMetric:{choice: optionInputOther.OTHER,
						other: entitlementMetric}}, SP_ENTITLEMENT_POOL_FORM, {entitlementMetric: validateChoiceWithOther})}
					label={i18n('Entitlement Metric')}
					data-test-id='create-ep-entitlement-metric'
					type='select'
					required={true}
					selectedEnum={entitlementMetric && entitlementMetric.choice}
					otherValue={entitlementMetric && entitlementMetric.other}
					values={EntitlementPoolsOptionsInputValues.ENTITLEMENT_METRIC}
					isValid={genericFieldInfo.entitlementMetric.isValid}
					errorText={genericFieldInfo.entitlementMetric.errorText} />
				<InputOptions
					onInputChange={()=>{}}
					isMultiSelect={false}
					isRequired={true}
					onEnumChange={aggregationFunction => onDataChanged({aggregationFunction:{choice: aggregationFunction, other: ''}},
						SP_ENTITLEMENT_POOL_FORM, {aggregationFunction: validateChoiceWithOther})}
					onOtherChange={aggregationFunction => onDataChanged({aggregationFunction:{choice: optionInputOther.OTHER,
						other: aggregationFunction}}, SP_ENTITLEMENT_POOL_FORM, {aggregationFunction: validateChoiceWithOther})}
					label={i18n('Aggregate Function')}
					data-test-id='create-ep-aggregate-function'
					type='select'
					required={true}
					selectedEnum={aggregationFunction && aggregationFunction.choice}
					otherValue={aggregationFunction && aggregationFunction.other}
					values={EntitlementPoolsOptionsInputValues.AGGREGATE_FUNCTION}
					isValid={genericFieldInfo.aggregationFunction.isValid}
					errorText={genericFieldInfo.aggregationFunction.errorText} />
			</GridItem>
			<GridItem colSpan={2}>
				<Input
					onChange={manufacturerReferenceNumber => onDataChanged({manufacturerReferenceNumber}, SP_ENTITLEMENT_POOL_FORM)}
					label={i18n('Manufacturer Reference Number')}
					value={manufacturerReferenceNumber}
					isRequired={true}
					data-test-id='create-ep-reference-number'
					type='text'/>
			</GridItem>
			<GridItem colSpan={2}>
				<InputOptions
					onInputChange={()=>{}}
					isMultiSelect={false}
					isRequired={true}
					onEnumChange={time => onDataChanged({time:{choice: time, other: ''}},
						SP_ENTITLEMENT_POOL_FORM, {time: validateChoiceWithOther})}
					onOtherChange={time => onDataChanged({time:{choice: optionInputOther.OTHER,
						other: time}}, SP_ENTITLEMENT_POOL_FORM, {time: validateTimeOtherValue})}
					label={i18n('Time')}
					data-test-id='create-ep-time'
					type='select'
					required={true}
					selectedEnum={time && time.choice}
					otherValue={time && time.other}
					values={EntitlementPoolsOptionsInputValues.TIME}
					isValid={genericFieldInfo.time.isValid}
					errorText={genericFieldInfo.time.errorText} />
			</GridItem>
			<GridItem colSpan={2}>
				<Input
					onChange={increments => onDataChanged({increments}, SP_ENTITLEMENT_POOL_FORM)}
					label={i18n('Increments')}
					value={increments}
					data-test-id='create-ep-increments'
					type='text'/>
			</GridItem>
		</GridSection>
	);
};

class EntitlementPoolsEditorView extends React.Component {

	static propTypes = {
		data: EntitlementPoolPropType,
		previousData: EntitlementPoolPropType,
		EPNames: React.PropTypes.object,
		isReadOnlyMode: React.PropTypes.bool,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired
	};

	static defaultProps = {
		data: {}
	};

	render() {
		let {data = {}, onDataChanged, isReadOnlyMode, genericFieldInfo} = this.props;


		return (
			<div>
				{
					genericFieldInfo && <Form
						ref='validationForm'
						hasButtons={true}
						onSubmit={ () => this.submit() }
						onReset={ () => this.props.onCancel() }
						labledButtons={true}
						isReadOnlyMode={isReadOnlyMode}
						isValid={this.props.isFormValid}
						formReady={this.props.formReady}
						onValidateForm={() => this.props.onValidateForm(SP_ENTITLEMENT_POOL_FORM) }
						className='entitlement-pools-form'>
						<EntitlementPoolsFormContent
							data={data}
							genericFieldInfo={genericFieldInfo}
							onDataChanged={onDataChanged}
							validateName={(value)=> this.validateName(value)}
							validateTimeOtherValue ={(value)=> this.validateTimeOtherValue(value)}
							validateChoiceWithOther={(value)=> this.validateChoiceWithOther(value)}
							thresholdValueValidation={(value, state)=> this.thresholdValueValidation(value, state)}/>
					</Form>
				}
			</div>
		);
	}

	submit() {
		const {data: entitlementPool, previousData: previousEntitlementPool} = this.props;
		this.props.onSubmit({entitlementPool, previousEntitlementPool});
	}

	validateName(value) {
		const {data: {id}, EPNames} = this.props;
		const isExists = Validator.isItemNameAlreadyExistsInList({itemId: id, itemName: value, list: EPNames});

		return !isExists ?  {isValid: true, errorText: ''} :
		{isValid: false, errorText: i18n('Entitlement pool by the name \'' + value + '\' already exists. Entitlement pool name must be unique')};
	}

	validateTimeOtherValue(value) {
		return Validator.validate('time', value.other, [{type: 'required', data: true}, {type: 'numeric', data: true}]);
	}

	validateChoiceWithOther(value) {
		let chosen = value.choice;
		// if we have an empty multiple select we have a problem since it's required
		if (value.choices) {
			if (value.choices.length === 0) {
				return  Validator.validate('field', '', [{type: 'required', data: true}]);
			} else {
				// continuing validation with the first chosen value in case we have the 'Other' field
				chosen = value.choices[0];
			}
		}
		if (chosen !== optionInputOther.OTHER) {
			return  Validator.validate('field', chosen, [{type: 'required', data: true}]);
		} else { // when 'Other' was chosen, validate other value
			return  Validator.validate('field', value.other, [{type: 'required', data: true}]);
		}
	}

	thresholdValueValidation(value, state) {

		let  unit = state.data.thresholdUnits;
		if (unit === thresholdUnitType.PERCENTAGE) {
			return Validator.validate('thresholdValue', value, [
				{type: 'required', data: true},
				{type: 'numeric', data: true},
				{type: 'maximum', data: 100},
				{type: 'minimum', data: 0}]);
		} else {
			return Validator.validate('thresholdValue', value, [
				{type: 'numeric', data: true},
				{type: 'required', data: true}]);
		}
	}

}

export default EntitlementPoolsEditorView;

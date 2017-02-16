import React from 'react';


import i18n from 'nfvo-utils/i18n/i18n.js';

import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';
import {optionsInputValues as  EntitlementPoolsOptionsInputValues, thresholdUnitType}  from  './EntitlementPoolsConstants.js';
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

class EntitlementPoolsEditorView extends React.Component {

	static propTypes = {
		data: EntitlementPoolPropType,
		previousData: EntitlementPoolPropType,
		isReadOnlyMode: React.PropTypes.bool,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired
	};

	static defaultProps = {
		data: {}
	};

	render() {
		let {data = {}, onDataChanged, isReadOnlyMode} = this.props;
		let {
			name, description, manufacturerReferenceNumber, operationalScope, aggregationFunction, thresholdUnits, thresholdValue,
			increments, time, entitlementMetric} = data;
		let thresholdValueValidation = thresholdUnits === thresholdUnitType.PERCENTAGE ? {numeric: true, required: true, maxValue: 100} : {numeric: true, required: true};
		let timeValidation = time && time.choice === optionInputOther.OTHER ? {numeric: true, required: true} : {required: true};

		return (
			<ValidationForm
				ref='validationForm'
				hasButtons={true}
				onSubmit={ () => this.submit() }
				onReset={ () => this.props.onCancel() }
				labledButtons={true}
				isReadOnlyMode={isReadOnlyMode}
				className='entitlement-pools-form'>
				<div className='entitlement-pools-form-row'>
					<ValidationInput
						onChange={name => onDataChanged({name})}
						label={i18n('Name')}
						value={name}
						validations={{maxLength: 120, required: true}}
						type='text'/>

					<ValidationInput
						isMultiSelect={true}
						onEnumChange={operationalScope => onDataChanged({operationalScope:{choices: operationalScope, other: ''}})}
						onOtherChange={operationalScope => onDataChanged({operationalScope:{choices: [optionInputOther.OTHER], other: operationalScope}})}
						multiSelectedEnum={operationalScope && operationalScope.choices}
						label={i18n('Operational Scope')}
						otherValue={operationalScope && operationalScope.other}
						validations={{required: true}}
						values={EntitlementPoolsOptionsInputValues.OPERATIONAL_SCOPE}/>

				</div>
				<div className='entitlement-pools-form-row'>
					<ValidationInput
						onChange={description => onDataChanged({description})}
						label={i18n('Description')}
						value={description}
						validations={{maxLength: 1000, required: true}}
						type='textarea'/>
					<div className='entitlement-pools-form-row-group'>
						<div className='entitlement-pools-form-row'>
							<ValidationInput
								onEnumChange={thresholdUnits => onDataChanged({thresholdUnits})}
								selectedEnum={thresholdUnits}
								label={i18n('Threshold Value')}
								type='select'
								values={EntitlementPoolsOptionsInputValues.THRESHOLD_UNITS}
								validations={{required: true}}/>
							<ValidationInput
								className='entitlement-pools-form-row-threshold-value'
								onChange={thresholdValue => onDataChanged({thresholdValue})}
								value={thresholdValue}
								validations={thresholdValueValidation}
								type='text'/>
						</div>

						<ValidationInput
							onEnumChange={entitlementMetric => onDataChanged({entitlementMetric:{choice: entitlementMetric, other: ''}})}
							onOtherChange={entitlementMetric => onDataChanged({entitlementMetric:{choice: optionInputOther.OTHER, other: entitlementMetric}})}
							selectedEnum={entitlementMetric && entitlementMetric.choice}
							otherValue={entitlementMetric && entitlementMetric.other}
							label={i18n('Entitlement Metric')}
							validations={{required: true}}
							values={EntitlementPoolsOptionsInputValues.ENTITLEMENT_METRIC}/>
						<ValidationInput
							onEnumChange={aggregationFunction => onDataChanged({aggregationFunction:{choice: aggregationFunction, other: ''}})}
							onOtherChange={aggregationFunction => onDataChanged({aggregationFunction:{choice: optionInputOther.OTHER, other: aggregationFunction}})}
							selectedEnum={aggregationFunction && aggregationFunction.choice}
							otherValue={aggregationFunction && aggregationFunction.other}
							validations={{required: true}}
							label={i18n('Aggregate Function')}
							values={EntitlementPoolsOptionsInputValues.AGGREGATE_FUNCTION}/>

					</div>
				</div>
				<div className='entitlement-pools-form-row'>

					<ValidationInput
						onChange={manufacturerReferenceNumber => onDataChanged({manufacturerReferenceNumber})}
						label={i18n('Manufacturer Reference Number')}
						value={manufacturerReferenceNumber}
						validations={{maxLength: 100, required: true}}
						type='text'/>

					<ValidationInput
						onEnumChange={time => onDataChanged({time:{choice: time, other: ''}})}
						onOtherChange={time => onDataChanged({time:{choice: optionInputOther.OTHER, other: time}})}
						selectedEnum={time && time.choice}
						otherValue={time && time.other}
						validations={timeValidation}
						label={i18n('Time')}
						values={EntitlementPoolsOptionsInputValues.TIME}/>
				</div>
				<div className='entitlement-pools-form-row'>
					<ValidationInput
						onChange={increments => onDataChanged({increments})}
						label={i18n('Increments')}
						value={increments}
						validations={{maxLength: 120}}
						type='text'/>

				</div>
			</ValidationForm>
		);
	}

	submit() {
		const {data: entitlementPool, previousData: previousEntitlementPool} = this.props;
		this.props.onSubmit({entitlementPool, previousEntitlementPool});
	}
}

export default EntitlementPoolsEditorView;

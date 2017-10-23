import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {LIMITS_FORM_NAME, selectValues} from './LimitEditorConstants.js';
import Button from 'sdc-ui/lib/react/Button.js';
import Validator from 'nfvo-utils/Validator.js';
import {other as optionInputOther} from 'nfvo-components/input/validation/InputOptions.jsx';
import InputOptions from 'nfvo-components/input/validation/InputOptions.jsx';

const LimitPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	metric: React.PropTypes.shape({
		choice: React.PropTypes.string,
		other: React.PropTypes.string
	}),
	value: React.PropTypes.string,
	aggregationFunction: React.PropTypes.string,
	time: React.PropTypes.string,
	unit: React.PropTypes.shape({
		choice: React.PropTypes.string,
		other: React.PropTypes.string
	})
});

class LimitEditor extends React.Component {
	static propTypes = {
		data: LimitPropType,
		limitsNames: React.PropTypes.object,
		isReadOnlyMode: React.PropTypes.bool,
		isFormValid: React.PropTypes.bool,
		formReady: React.PropTypes.bool,
		genericFieldInfo: React.PropTypes.object.isRequired,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onValidateForm: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired
	};

	componentDidUpdate(prevProps) {
		if (this.props.formReady && this.props.formReady !== prevProps.formReady) {
			this.submit();
		}
	}

	render() {
		let {data = {}, onDataChanged, isReadOnlyMode, genericFieldInfo, onCancel, isFormValid, formReady, onValidateForm} = this.props;
		let {name, description, metric, value, aggregationFunction, time, unit} = data;
		return (
			<div className='limit-editor'>
			{!data.id &&
			<div className='limit-editor-title'>
				{data.name ? data.name : i18n('NEW LIMIT')}
			</div>}
			{
				genericFieldInfo &&
				<Form
					ref='validationForm'
					hasButtons={false}
					isValid={isFormValid}
					formReady={formReady}
					onValidateForm={() => onValidateForm(LIMITS_FORM_NAME) }
					labledButtons={false}
					isReadOnlyMode={isReadOnlyMode}
					className='limit-editor-form'>
					<GridSection className='limit-editor-form-grid-section'>
						<GridItem colSpan={2}>
							<Input
								onChange={name => onDataChanged({name}, LIMITS_FORM_NAME, {name: () => this.validateName(name)})}
								label={i18n('Name')}
								data-test-id='limit-editor-name'
								value={name}
								isValid={genericFieldInfo.name.isValid}
								errorText={genericFieldInfo.name.errorText}
								isRequired={true}
								type='text'/>
						</GridItem>
						<GridItem colSpan={2}>
							<Input
								onChange={description => onDataChanged({description}, LIMITS_FORM_NAME)}
								label={i18n('Description')}
								data-test-id='limit-editor-description'
								value={description}
								isValid={genericFieldInfo.description.isValid}
								errorText={genericFieldInfo.description.errorText}
								isRequired={false}
								type='text'/>
						</GridItem>
						<GridItem colSpan={2}>
							<InputOptions
								onInputChange={()=>{}}
								isMultiSelect={false}
								isRequired={true}
								onEnumChange={metric => onDataChanged({metric:{choice: metric, other: ''}},
									LIMITS_FORM_NAME)}
								onOtherChange={metric => onDataChanged({metric:{choice: optionInputOther.OTHER,
									other: metric}}, LIMITS_FORM_NAME)}
								label={i18n('Metric')}
								data-test-id='limit-editor-metric'
								type='select'
								required={true}
								selectedEnum={metric && metric.choice}
								otherValue={metric && metric.other}
								values={selectValues.METRIC}
								isValid={genericFieldInfo.metric.isValid}
								errorText={genericFieldInfo.metric.errorText} />
						</GridItem>
						<GridItem>
							<Input
								onChange={value => onDataChanged({value}, LIMITS_FORM_NAME)}
								label={i18n('Metric value')}
								data-test-id='limit-editor-metric-value'
								value={value}
								isValid={genericFieldInfo.value.isValid}
								errorText={genericFieldInfo.value.errorText}
								isRequired={true}
								type='text'/>
						</GridItem>
						<GridItem>
							<InputOptions
								onInputChange={()=>{}}
								isMultiSelect={false}
								isRequired={false}
								onEnumChange={unit => onDataChanged({unit:{choice: unit, other: ''}},
									LIMITS_FORM_NAME)}
								onOtherChange={unit => onDataChanged({unit:{choice: optionInputOther.OTHER,
									other: unit}}, LIMITS_FORM_NAME)}
								label={i18n('Units')}
								data-test-id='limit-editor-units'
								type='select'
								required={false}
								selectedEnum={unit && unit.choice}
								otherValue={unit && unit.other}
								values={selectValues.UNIT}
								isValid={genericFieldInfo.unit.isValid}
								errorText={genericFieldInfo.unit.errorText} />
						</GridItem>
						<GridItem colSpan={2}>
							<Input
								onChange={e => {
									const selectedIndex = e.target.selectedIndex;
									const val = e.target.options[selectedIndex].value;
									onDataChanged({aggregationFunction: val}, LIMITS_FORM_NAME);}
								}
								value={aggregationFunction}
								label={i18n('Aggregation Function')}
								data-test-id='limit-editor-aggregation-function'
								isValid={genericFieldInfo.aggregationFunction.isValid}
								errorText={genericFieldInfo.aggregationFunction.errorText}
								groupClassName='bootstrap-input-options'
								className='input-options-select'
								type='select' >
								{selectValues.AGGREGATION_FUNCTION.map(mtype =>
									<option key={mtype.enum} value={mtype.enum}>{`${mtype.title}`}</option>)}
							</Input>
						</GridItem>
						<GridItem>
							<Input
								onChange={e => {
									const selectedIndex = e.target.selectedIndex;
									const val = e.target.options[selectedIndex].value;
									onDataChanged({time: val}, LIMITS_FORM_NAME);}
								}
								value={time}
								label={i18n('Time')}
								data-test-id='limit-editor-time'
								isValid={genericFieldInfo.time.isValid}
								errorText={genericFieldInfo.time.errorText}
								groupClassName='bootstrap-input-options'
								className='input-options-select'
								type='select' >
								{selectValues.TIME.map(mtype =>
									<option key={mtype.enum} value={mtype.enum}>{`${mtype.title}`}</option>)}
							</Input>
						</GridItem>
					</GridSection>
					<GridSection className='limit-editor-buttons'>
						<Button btnType='outline' disabled={!isFormValid || isReadOnlyMode} onClick={() => this.submit()} type='reset'>{i18n('Save')}</Button>
						<Button btnType='outline' color='gray' onClick={onCancel} type='reset'>{i18n('Cancel')}</Button>
					</GridSection>
				</Form>
			}
			</div>
		);
	}

	validateName(value) {
		const {data: {id}, limitsNames} = this.props;
		const isExists = Validator.isItemNameAlreadyExistsInList({itemId: id, itemName: value, list: limitsNames});

		return !isExists ?  {isValid: true, errorText: ''} :
		{isValid: false, errorText: i18n('Limit by the name \'' + value + '\' already exists. Limit name must be unique')};
	}


	submit() {
		if (!this.props.formReady) {
			this.props.onValidateForm(LIMITS_FORM_NAME);
		} else {
			this.props.onSubmit();
		}
	}
}

export default LimitEditor;

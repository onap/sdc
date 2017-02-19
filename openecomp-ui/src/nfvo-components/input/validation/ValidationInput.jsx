/**
 * Used for inputs on a validation form.
 * All properties will be passed on to the input element.
 *
 * The following properties can be set for OOB validations and callbacks:
 - required: Boolean:  Should be set to true if the input must have a value
 - numeric: Boolean : Should be set to true id the input should be an integer
 - onChange : Function :  Will be called to validate the value if the default validations are not sufficient, should return a boolean value
 indicating whether the value is valid
 - didUpdateCallback :Function: Will be called after the state has been updated and the component has rerendered. This can be used if
 there are dependencies between inputs in a form.
 *
 * The following properties of the state can be set to determine
 * the state of the input from outside components:
 - isValid : Boolean - whether the value is valid
 - value : value for the input field,
 - disabled : Boolean,
 - required : Boolean - whether the input value must be filled out.
 */
import React from 'react';
import ReactDOM from 'react-dom';
import Validator from 'validator';
import FormGroup from 'react-bootstrap/lib/FormGroup.js';
import Input from 'react-bootstrap/lib/Input.js';
import Overlay from 'react-bootstrap/lib/Overlay.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';
import isEqual from 'lodash/isEqual.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import JSONSchema from 'nfvo-utils/json/JSONSchema.js';
import JSONPointer from 'nfvo-utils/json/JSONPointer.js';


import InputOptions  from '../inputOptions/InputOptions.jsx';

const globalValidationFunctions = {
	required: value => value !== '',
	maxLength: (value, length) => Validator.isLength(value, {max: length}),
	minLength: (value, length) => Validator.isLength(value, {min: length}),
	pattern: (value, pattern) => Validator.matches(value, pattern),
	numeric: value => {
		if (value === '') {
			// to allow empty value which is not zero
			return true;
		}
		return Validator.isNumeric(value);
	},
	maxValue: (value, maxValue) => value < maxValue,
	minValue: (value, minValue) => value >= minValue,
	alphanumeric: value => Validator.isAlphanumeric(value),
	alphanumericWithSpaces: value => Validator.isAlphanumeric(value.replace(/ /g, '')),
	validateName: value => Validator.isAlphanumeric(value.replace(/\s|\.|\_|\-/g, ''), 'en-US'),
	validateVendorName: value => Validator.isAlphanumeric(value.replace(/[\x7F-\xFF]|\s/g, ''), 'en-US'),
	freeEnglishText: value => Validator.isAlphanumeric(value.replace(/\s|\.|\_|\-|\,|\(|\)|\?/g, ''), 'en-US'),
	email: value => Validator.isEmail(value),
	ip: value => Validator.isIP(value),
	url: value => Validator.isURL(value)
};

const globalValidationMessagingFunctions = {
	required: () => i18n('Field is required'),
	maxLength: (value, maxLength) => i18n('Field value has exceeded it\'s limit, {maxLength}. current length: {length}', {
		length: value.length,
		maxLength
	}),
	minLength: (value, minLength) => i18n('Field value should contain at least {minLength} characters.', {minLength}),
	pattern: (value, pattern) => i18n('Field value should match the pattern: {pattern}.', {pattern}),
	numeric: () => i18n('Field value should contain numbers only.'),
	maxValue: (value, maxValue) => i18n('Field value should be less than: {maxValue}.', {maxValue}),
	minValue: (value, minValue) => i18n('Field value should be at least: {minValue}.', {minValue}),
	alphanumeric: () => i18n('Field value should contain letters or digits only.'),
	alphanumericWithSpaces: () => i18n('Field value should contain letters, digits or spaces only.'),
	validateName: ()=> i18n('Field value should contain English letters, digits , spaces, underscores, dashes and dots only.'),
	validateVendorName: ()=> i18n('Field value should contain English letters digits and spaces only.'),
	freeEnglishText: ()=> i18n('Field value should contain  English letters, digits , spaces, underscores, dashes and dots only.'),
	email: () => i18n('Field value should be a valid email address.'),
	ip: () => i18n('Field value should be a valid ip address.'),
	url: () => i18n('Field value should be a valid url address.'),
	general: () => i18n('Field value is invalid.')
};

class ValidationInput extends React.Component {

	static contextTypes = {
		validationParent: React.PropTypes.any,
		isReadOnlyMode: React.PropTypes.bool,
		validationSchema: React.PropTypes.instanceOf(JSONSchema),
		validationData: React.PropTypes.object
	};

	static defaultProps = {
		onChange: null,
		disabled: null,
		didUpdateCallback: null,
		validations: {},
		value: ''
	};

	static propTypes = {
		type: React.PropTypes.string.isRequired,
		onChange: React.PropTypes.func,
		disabled: React.PropTypes.bool,
		didUpdateCallback: React.PropTypes.func,
		validations: React.PropTypes.object,
		isMultiSelect: React.PropTypes.bool,
		onOtherChange: React.PropTypes.func,
		pointer: React.PropTypes.string
	};


	state = {
		isValid: true,
		style: null,
		value: this.props.value,
		error: {},
		previousErrorMessage: '',
		wasInvalid: false,
		validations: this.props.validations,
		isMultiSelect: this.props.isMultiSelect
	};

	componentWillMount() {
		if (this.context.validationSchema) {
			let {validationSchema: schema, validationData: data} = this.context,
				{pointer} = this.props;

			if (!schema.exists(pointer)) {
				console.error(`Field doesn't exists in the schema ${pointer}`);
			}

			let value = JSONPointer.getValue(data, pointer);
			if (value === undefined) {
				value = schema.getDefault(pointer);
				if (value === undefined) {
					value = '';
				}
			}
			this.setState({value});

			let enums = schema.getEnum(pointer);
			if (enums) {
				let values = enums.map(value => ({enum: value, title: value, groupName: pointer})),
					isMultiSelect = schema.isArray(pointer);

				if (!isMultiSelect && this.props.type !== 'radiogroup') {
					values = [{enum: '', title: i18n('Select...')}, ...values];
				}
				if (isMultiSelect && Array.isArray(value) && value.length === 0) {
					value = '';
				}

				this.setState({
					isMultiSelect,
					values,
					onEnumChange: value => this.changedInputOptions(value),
					value
				});
			}

			this.setState({validations: this.extractValidationsFromSchema(schema, pointer, this.props)});
		}
	}

	extractValidationsFromSchema(schema, pointer, props) {
		/* props are here to get precedence over the scheme definitions */
		let validations = {};

		if (schema.isRequired(pointer)) {
			validations.required = true;
		}

		if (schema.isNumber(pointer)) {
			validations.numeric = true;

			const maxValue = props.validations.maxValue || schema.getMaxValue(pointer);
			if (maxValue !== undefined) {
				validations.maxValue = maxValue;
			}

			const minValue = props.validations.minValue || schema.getMinValue(pointer);
			if (minValue !== undefined) {
				validations.minValue = minValue;
			}
		}


		if (schema.isString(pointer)) {

			const pattern = schema.getPattern(pointer);
			if (pattern) {
				validations.pattern = pattern;
			}

			const maxLength = schema.getMaxLength(pointer);
			if (maxLength !== undefined) {
				validations.maxLength = maxLength;
			}

			const minLength = schema.getMinLength(pointer);
			if (minLength !== undefined) {
				validations.minLength = minLength;
			}
		}

		return validations;
	}

	componentWillReceiveProps({value: nextValue, validations: nextValidations, pointer: nextPointer}, nextContext) {
		const {validations, value} = this.props;
		const validationsChanged = !isEqual(validations, nextValidations);
		if (nextContext.validationSchema) {
			if (this.props.pointer !== nextPointer ||
				this.context.validationData !== nextContext.validationData) {
				let currentValue = JSONPointer.getValue(this.context.validationData, this.props.pointer),
					nextValue = JSONPointer.getValue(nextContext.validationData, nextPointer);
				if(nextValue === undefined) {
					nextValue = '';
				}
				if (this.state.isMultiSelect && Array.isArray(nextValue) && nextValue.length === 0) {
					nextValue = '';
				}
				if (currentValue !== nextValue) {
					this.setState({value: nextValue});
				}
				if (validationsChanged) {
					this.setState({
						validations: this.extractValidationsFromSchema(nextContext.validationSchema, nextPointer, {validations: nextValidations})
					});
				}
			}
		} else {
			if (validationsChanged) {
				this.setState({validations: nextValidations});
			}
			if (this.state.wasInvalid && (value !== nextValue || validationsChanged)) {
				this.validate(nextValue, nextValidations);
			} else if (value !== nextValue) {
				this.setState({value: nextValue});
			}
		}
	}

	shouldTypeBeNumberBySchemeDefinition(pointer) {
		return this.context.validationSchema &&
			this.context.validationSchema.isNumber(pointer);
	}

	hasEnum(pointer) {
		return this.context.validationSchema &&
			this.context.validationSchema.getEnum(pointer);
	}

	render() {
		let {value, isMultiSelect, values, onEnumChange, style, isValid, validations} = this.state;
		let {onOtherChange, type, pointer} = this.props;
		if (this.shouldTypeBeNumberBySchemeDefinition(pointer) && !this.hasEnum(pointer)) {
			type = 'number';
		}
		let props = {...this.props};

		let groupClasses = this.props.groupClassName || '';
		if (validations.required) {
			groupClasses += ' required';
		}
		let isReadOnlyMode = this.context.isReadOnlyMode;

		if (value === true && (type === 'checkbox' || type === 'radio')) {
			props.checked = true;
		}
		return (
			<div className='validation-input-wrapper'>
				{
					!isMultiSelect && !onOtherChange && type !== 'select' && type !== 'radiogroup'
					&& <Input
						{...props}
						type={type}
						groupClassName={groupClasses}
						ref={'_myInput'}
						value={value}
						disabled={isReadOnlyMode || Boolean(this.props.disabled)}
						bsStyle={style}
						onChange={() => this.changedInput()}
						onBlur={() => this.blurInput()}>
						{this.props.children}
					</Input>
				}
				{
					type === 'radiogroup'
					&& <FormGroup>
						{
							values.map(val =>
								<Input disabled={isReadOnlyMode || Boolean(this.props.disabled)}
									inline={true}
									ref={'_myInput' + (typeof val.enum === 'string' ? val.enum.replace(/\W/g, '_') : val.enum)}
									value={val.enum} checked={value === val.enum}
									type='radio' label={val.title}
									name={val.groupName}
									onChange={() => this.changedInput()}/>
							)
						}
					</FormGroup>
				}
				{
					(isMultiSelect || onOtherChange || type === 'select')
					&& <InputOptions
						onInputChange={() => this.changedInput()}
						onBlur={() => this.blurInput()}
						hasError={!isValid}
						ref={'_myInput'}
						isMultiSelect={isMultiSelect}
						values={values}
						onEnumChange={onEnumChange}
						selectedEnum={value}
						multiSelectedEnum={value}
						{...props} />
				}
				{this.renderOverlay()}
			</div>
		);
	}

	renderOverlay() {
		let position = 'right';
		if (this.props.type === 'text'
			|| this.props.type === 'email'
			|| this.props.type === 'number'
			|| this.props.type === 'password'

		) {
			position = 'bottom';
		}

		let validationMessage = this.state.error.message || this.state.previousErrorMessage;
		return (
			<Overlay
				show={!this.state.isValid}
				placement={position}
				target={() => {
					let target = ReactDOM.findDOMNode(this.refs._myInput);
					return target.offsetParent ? target : undefined;
				}}
				container={this}>
				<Tooltip
					id={`error-${validationMessage.replace(' ', '-')}`}
					className='validation-error-message'>
					{validationMessage}
				</Tooltip>
			</Overlay>
		);
	}

	componentDidMount() {
		if (this.context.validationParent) {
			this.context.validationParent.register(this);
		}
	}

	componentDidUpdate(prevProps, prevState) {
		if (this.context.validationParent) {
			if (prevState.isValid !== this.state.isValid) {
				this.context.validationParent.childValidStateChanged(this, this.state.isValid);
			}
		}
		if (this.props.didUpdateCallback) {
			this.props.didUpdateCallback();
		}

	}

	componentWillUnmount() {
		if (this.context.validationParent) {
			this.context.validationParent.unregister(this);
		}
	}

	isNumberInputElement() {
		return this.props.type === 'number' || this.refs._myInput.props.type === 'number';
	}

	/***
	 * Adding same method as the actual input component
	 * @returns {*}
	 */
	getValue() {
		if (this.props.type === 'checkbox') {
			return this.refs._myInput.getChecked();
		}
		if (this.props.type === 'radiogroup') {
			for (let key in this.refs) { // finding the value of the radio button that was checked
				if (this.refs[key].getChecked()) {
					return this.refs[key].getValue();
				}
			}
		}
		if (this.isNumberInputElement()) {
			return Number(this.refs._myInput.getValue());
		}

		return this.refs._myInput.getValue();
	}

	resetValue() {
		this.setState({value: this.props.value});
	}


	/***
	 * internal method that validated the value. includes callback to the onChange method
	 * @param value
	 * @param validations - map containing validation id and the limitation describing the validation.
	 * @returns {object}
	 */
	validateValue = (value, validations) => {
		let {customValidationFunction} = validations;
		let error = {};
		let isValid = true;
		for (let validation in validations) {
			if ('customValidationFunction' !== validation) {
				if (validations[validation]) {
					if (!globalValidationFunctions[validation](value, validations[validation])) {
						error.id = validation;
						error.message = globalValidationMessagingFunctions[validation](value, validations[validation]);
						isValid = false;
						break;
					}
				}
			} else {
				let customValidationResult = customValidationFunction(value);

				if (customValidationResult !== true) {
					error.id = 'custom';
					isValid = false;
					if (typeof customValidationResult === 'string') {//custom validation error message supplied.
						error.message = customValidationResult;
					} else {
						error.message = globalValidationMessagingFunctions.general();
					}
					break;
				}


			}
		}

		return {
			isValid,
			error
		};
	};

	/***
	 * Internal method that handles the change event of the input. validates and updates the state.
	 */
	changedInput() {

		let {isValid, error} = this.state.wasInvalid ? this.validate() : this.state;
		let onChange = this.props.onChange;
		if (onChange) {
			onChange(this.getValue(), isValid, error);
		}
		if (this.context.validationSchema) {
			let value = this.getValue();
			if (this.state.isMultiSelect && value === '') {
				value = [];
			}
			if (this.shouldTypeBeNumberBySchemeDefinition(this.props.pointer)) {
				value = Number(value);
			}
			this.context.validationParent.onValueChanged(this.props.pointer, value, isValid, error);
		}
	}

	changedInputOptions(value) {
		this.context.validationParent.onValueChanged(this.props.pointer, value, true);
	}

	blurInput() {
		if (!this.state.wasInvalid) {
			this.setState({wasInvalid: true});
		}

		let {isValid, error} = !this.state.wasInvalid ? this.validate() : this.state;
		let onBlur = this.props.onBlur;
		if (onBlur) {
			onBlur(this.getValue(), isValid, error);
		}
	}

	validate(value = this.getValue(), validations = this.state.validations) {
		let validationStatus = this.validateValue(value, validations);
		let {isValid, error} = validationStatus;
		let _style = isValid ? null : 'error';
		this.setState({
			isValid,
			error,
			value,
			previousErrorMessage: this.state.error.message || '',
			style: _style,
			wasInvalid: !isValid || this.state.wasInvalid
		});

		return validationStatus;
	}

	isValid() {
		return this.state.isValid;
	}

}
export default ValidationInput;

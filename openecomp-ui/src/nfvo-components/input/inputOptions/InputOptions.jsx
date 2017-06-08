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
import classNames from 'classnames';
import Select from 'nfvo-components/input/SelectInput.jsx';

export const other = {OTHER: 'Other'};

class InputOptions extends React.Component {

	static propTypes = {
		values: React.PropTypes.arrayOf(React.PropTypes.shape({
			enum: React.PropTypes.string,
			title: React.PropTypes.string
		})),
		isEnabledOther: React.PropTypes.bool,
		label: React.PropTypes.string,
		selectedValue: React.PropTypes.string,
		multiSelectedEnum: React.PropTypes.oneOfType([
			React.PropTypes.string,
			React.PropTypes.array
		]),
		selectedEnum: React.PropTypes.string,
		otherValue: React.PropTypes.string,
		onEnumChange: React.PropTypes.func,
		onOtherChange: React.PropTypes.func,
		onBlur: React.PropTypes.func,
		isRequired: React.PropTypes.bool,
		isMultiSelect: React.PropTypes.bool,
		hasError: React.PropTypes.bool,
		disabled: React.PropTypes.bool
	};


	static contextTypes = {
		isReadOnlyMode: React.PropTypes.bool
	};

	state = {
		otherInputDisabled: !this.props.otherValue
	};

	oldProps = {
		selectedEnum: '',
		otherValue: '',
		multiSelectedEnum: []
	};

	render() {
		let {label, isRequired, values, otherValue, onOtherChange, isMultiSelect, onBlur, multiSelectedEnum, selectedEnum, hasError, validations, children} = this.props;
		const dataTestId = this.props['data-test-id'] ? {'data-test-id': this.props['data-test-id']} : {};
		let currentMultiSelectedEnum = [];
		let currentSelectedEnum = '';
		let {otherInputDisabled} = this.state;
		if (isMultiSelect) {
			currentMultiSelectedEnum = multiSelectedEnum;
			if(!otherInputDisabled) {
				currentSelectedEnum = multiSelectedEnum ? multiSelectedEnum.toString() : undefined;
			}
		}
		else if(selectedEnum){
			currentSelectedEnum = selectedEnum;
		}
		if (!onBlur) {
			onBlur = () => {};
		}

		let isReadOnlyMode = this.context.isReadOnlyMode;

		return(
			<div className={classNames('form-group', {'required' : (validations && validations.required) || isRequired, 'has-error' : hasError})}>
				{label && <label className='control-label'>{label}</label>}
				{isMultiSelect && otherInputDisabled ?
					<Select
						{...dataTestId}
						ref='_myInput'
						value={currentMultiSelectedEnum}
						className='options-input'
						clearable={false}
						required={isRequired}
						disabled={isReadOnlyMode || Boolean(this.props.disabled)}
						onBlur={() => onBlur()}
						onMultiSelectChanged={value => this.multiSelectEnumChanged(value)}
						options={this.renderMultiSelectOptions(values)}
						multi/> :
					<div className={classNames('input-options',{'has-error' : hasError})}>
						<select
							{...dataTestId}
							ref={'_myInput'}
							label={label}
							className='form-control input-options-select'
							value={currentSelectedEnum}
							style={{'width' : otherInputDisabled ? '100%' : '100px'}}
							onBlur={() => onBlur()}
							disabled={isReadOnlyMode || Boolean(this.props.disabled)}
							onChange={ value => this.enumChanged(value)}
							type='select'>
							{children || (values && values.length && values.map((val, index) => this.renderOptions(val, index)))}
							{onOtherChange && <option key='other' value={other.OTHER}>{i18n(other.OTHER)}</option>}
						</select>

						{!otherInputDisabled && <div className='input-options-separator'/>}
						<input
							className='form-control input-options-other'
							placeholder={i18n('other')}
							ref='_otherValue'
							style={{'display' : otherInputDisabled ? 'none' : 'block'}}
							disabled={isReadOnlyMode || Boolean(this.props.disabled)}
							value={otherValue || ''}
							onBlur={() => onBlur()}
							onChange={() => this.changedOtherInput()}/>
					</div>
				}
			</div>
		);
	}

	renderOptions(val, index){
		return (
			<option key={index} value={val.enum}>{val.title}</option>
		);
	}


	renderMultiSelectOptions(values) {
		let {onOtherChange} = this.props;
		let optionsList = [];
		if (onOtherChange) {
			optionsList = values.map(option => {
				return {
					label: option.title,
					value: option.enum,
				};
			}).concat([{
				label: i18n(other.OTHER),
				value: i18n(other.OTHER),
			}]);
		}
		else {
			optionsList = values.map(option => {
				return {
					label: option.title,
					value: option.enum,
				};
			});
		}
		if (optionsList.length > 0 && optionsList[0].value === '') {
			optionsList.shift();
		}
		return optionsList;
	}

	getValue() {
		let res = '';
		let {isMultiSelect} = this.props;
		let {otherInputDisabled} = this.state;

		if (otherInputDisabled) {
			res = isMultiSelect ? this.refs._myInput.getValue() : this.refs._myInput.value;
		} else {
			res = this.refs._otherValue.value;
		}
		return res;
	}

	enumChanged() {
		let enumValue = this.refs._myInput.value;
		let {onEnumChange, onOtherChange, isMultiSelect, onChange} = this.props;
		this.setState({
			otherInputDisabled: !Boolean(onOtherChange) || enumValue !== other.OTHER
		});

		let value = isMultiSelect ? [enumValue] : enumValue;
		if (onEnumChange) {
			onEnumChange(value);
		}
		if (onChange) {
			onChange(value);
		}
	}

	multiSelectEnumChanged(enumValue) {
		let {onEnumChange, onOtherChange} = this.props;
		let selectedValues = enumValue.map(enumVal => {
			return enumVal.value;
		});

		if (this.state.otherInputDisabled === false) {
			selectedValues.shift();
		}
		else if (selectedValues.includes(i18n(other.OTHER))) {
			selectedValues = [i18n(other.OTHER)];
		}

		this.setState({
			otherInputDisabled: !Boolean(onOtherChange) || !selectedValues.includes(i18n(other.OTHER))
		});
		onEnumChange(selectedValues);
	}

	changedOtherInput() {
		let {onOtherChange} = this.props;
		onOtherChange(this.refs._otherValue.value);
	}

	componentDidUpdate() {
		let {otherValue, selectedEnum, onInputChange, multiSelectedEnum} = this.props;
		if (this.oldProps.otherValue !== otherValue
			|| this.oldProps.selectedEnum !== selectedEnum
			|| this.oldProps.multiSelectedEnum !== multiSelectedEnum) {
			this.oldProps = {
				otherValue,
				selectedEnum,
				multiSelectedEnum
			};
			onInputChange();
		}
	}

	static getTitleByName(values, name) {
		for (let key of Object.keys(values)) {
			let option = values[key].find(option => option.enum === name);
			if (option) {
				return option.title;
			}
		}
		return name;
	}

}

export default InputOptions;

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
import ReactDOM from 'react-dom';
import i18n from 'nfvo-utils/i18n/i18n.js';
import classNames from 'classnames';
import Select from 'nfvo-components/input/SelectInput.jsx';
import Overlay from 'react-bootstrap/lib/Overlay.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';

export const other = {OTHER: 'Other'};

class InputOptions extends React.Component {

	static propTypes = {
		values: PropTypes.arrayOf(PropTypes.shape({
			enum: PropTypes.string,
			title: PropTypes.string
		})),
		isEnabledOther: PropTypes.bool,
		label: PropTypes.string,
		selectedValue: PropTypes.string,
		multiSelectedEnum: PropTypes.oneOfType([
			PropTypes.string,
			PropTypes.array
		]),
		selectedEnum: PropTypes.string,
		otherValue: PropTypes.string,
		overlayPos: PropTypes.string,
		onEnumChange: PropTypes.func,
		onOtherChange: PropTypes.func,
		onBlur: PropTypes.func,
		isRequired: PropTypes.bool,
		isMultiSelect: PropTypes.bool,
		isValid: PropTypes.bool,
		disabled: PropTypes.bool
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
		let {label, isRequired, values, otherValue, onOtherChange, isMultiSelect, onBlur, multiSelectedEnum, selectedEnum, isValid, children, isReadOnlyMode} = this.props;
		const dataTestId = this.props['data-test-id'] ? {'data-test-id': this.props['data-test-id']} : {};
		let currentMultiSelectedEnum = [];
		let currentSelectedEnum = '';
		let otherInputDisabled = (isMultiSelect && (multiSelectedEnum === undefined || multiSelectedEnum.length === 0 || multiSelectedEnum[0] !== other.OTHER))
			|| (!isMultiSelect && (selectedEnum === undefined || selectedEnum !== other.OTHER));
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

		return(
			<div className='validation-input-wrapper' >
				<div className={classNames('form-group', {'required' : isRequired, 'has-error' : !isValid})} >
					{label && <label className='control-label'>{label}</label>}
					{isMultiSelect && otherInputDisabled ?
						<Select
							{...dataTestId}
							ref={(input) => this.input = input}
							value={currentMultiSelectedEnum}
							className='options-input'
							clearable={false}
							required={isRequired}
							disabled={isReadOnlyMode || Boolean(this.props.disabled)}
							onBlur={() => onBlur()}
							onMultiSelectChanged={value => this.multiSelectEnumChanged(value)}
							options={this.renderMultiSelectOptions(values)}
							multi/> :
						<div className={classNames('input-options',{'has-error' : !isValid})} >
							<select
								{...dataTestId}
								ref={(input) => this.input = input}
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
								ref={(otherValue) => this.otherValue = otherValue}
								style={{'display' : otherInputDisabled ? 'none' : 'block'}}
								disabled={isReadOnlyMode || Boolean(this.props.disabled)}
								value={otherValue || ''}
								onBlur={() => onBlur()}
								onChange={() => this.changedOtherInput()}/>
						</div>
					}
					</div>
				{ this.renderErrorOverlay() }
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

	renderErrorOverlay() {
		let position = 'right';
		const {errorText = '', isValid = true, type, overlayPos} = this.props;

		if (overlayPos) {
			position = overlayPos;
		}
		else if (type === 'text'
			|| type === 'email'
			|| type === 'number'
			|| type === 'password') {
			position = 'bottom';
		}

		return (
			<Overlay
				show={!isValid}
				placement={position}
				target={() => {
					let {otherInputDisabled} = this.state;
					let target = otherInputDisabled ? ReactDOM.findDOMNode(this.input) :  ReactDOM.findDOMNode(this.otherValue);
					return target.offsetParent ? target : undefined;
				}}
				container={this}>
				<Tooltip
					id={`error-${errorText.replace(' ', '-')}`}
					className='validation-error-message'>
					{errorText}
				</Tooltip>
			</Overlay>
		);
	}

	getValue() {
		let res = '';
		let {isMultiSelect} = this.props;
		let {otherInputDisabled} = this.state;

		if (otherInputDisabled) {
			res = isMultiSelect ? this.input.getValue() : this.input.value;
		} else {
			res = this.otherValue.value;
		}
		return res;
	}

	enumChanged() {
		let enumValue = this.input.value;
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
		onOtherChange(this.otherValue.value);
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

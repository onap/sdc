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
import ReactDOM from 'react-dom';
import classNames from 'classnames';
import Checkbox from 'react-bootstrap/lib/Checkbox.js';
import Radio from 'react-bootstrap/lib/Radio.js';
import FormGroup from 'react-bootstrap/lib/FormGroup.js';
import FormControl from 'react-bootstrap/lib/FormControl.js';
import Overlay from 'react-bootstrap/lib/Overlay.js';
import Tooltip from 'react-bootstrap/lib/Tooltip.js';

class Input extends React.Component {

	state = {
		value: this.props.value,
		checked: this.props.checked,
		selectedValues: []
	}

	render() {
		const {label, isReadOnlyMode, value, onBlur, onKeyDown, type, disabled, checked, name} = this.props;
		// eslint-disable-next-line no-unused-vars
		const {groupClassName, isValid = true, errorText, isRequired,  ...inputProps} = this.props;
		let wrapperClassName = (type !== 'radio') ? 'validation-input-wrapper' : 'form-group';
		if (disabled) {
			wrapperClassName += ' disabled';
		}
		return(
			<div className={wrapperClassName}>
				<FormGroup className={classNames('form-group', [groupClassName], {'required' : isRequired , 'has-error' : !isValid})} >
					{(label && (type !== 'checkbox' && type !== 'radio')) && <label className='control-label'>{label}</label>}
					{(type === 'text' || type === 'number') &&
					<FormControl
						bsClass={'form-control input-options-other'}
						onChange={(e) => this.onChange(e)}
						disabled={isReadOnlyMode || Boolean(disabled)}
						onBlur={onBlur}
						onKeyDown={onKeyDown}
						value={value || ''}
						inputRef={(input) => this.input = input}
						type={type}
						data-test-id={this.props['data-test-id']}/>}

					{type === 'textarea' &&
					<FormControl
						className='form-control input-options-other'
						disabled={isReadOnlyMode || Boolean(disabled)}
						value={value || ''}
						onBlur={onBlur}
						onKeyDown={onKeyDown}
						componentClass={type}
						onChange={(e) => this.onChange(e)}
						inputRef={(input) => this.input = input}
						data-test-id={this.props['data-test-id']}/>}

					{type === 'checkbox' &&
					<Checkbox
						className={classNames({'required' : isRequired , 'has-error' : !isValid})}
						onChange={(e)=>this.onChangeCheckBox(e)}
						disabled={isReadOnlyMode || Boolean(disabled)}
						checked={value}
						data-test-id={this.props['data-test-id']}>{label}</Checkbox>}

					{type === 'radio' &&
					<Radio name={name}
						   checked={checked}
						   disabled={isReadOnlyMode || Boolean(disabled)}
						   value={value}
						   onChange={(e)=>this.onChangeRadio(e)}
						   data-test-id={this.props['data-test-id']}>{label}</Radio>}
					{type === 'select' &&
					<FormControl onClick={ (e) => this.optionSelect(e) }
						 componentClass={type}
						 inputRef={(input) => this.input = input}
						 name={name} {...inputProps}
						 data-test-id={this.props['data-test-id']}/>}
				</FormGroup>
				{ this.renderErrorOverlay() }
			</div>
		);
	}

	getValue() {
		return this.props.type !== 'select' ? this.state.value : this.state.selectedValues;
	}

	getChecked() {
		return this.state.checked;
	}

	optionSelect(e) {
		let selectedValues = [];
		if (e.target.value) {
			selectedValues.push(e.target.value);
		}
		this.setState({
			selectedValues
		});
	}

	onChange(e) {
		const {onChange, type} = this.props;
		let value = e.target.value;
		if (type === 'number') {
			value = Number(value);
		}
		this.setState({
			value
		});
		onChange(value);
	}

	onChangeCheckBox(e) {
		let {onChange} = this.props;
		this.setState({
			checked: e.target.checked
		});
		onChange(e.target.checked);
	}

	onChangeRadio(e) {
		let {onChange} = this.props;
		this.setState({
			checked: e.target.checked
		});
		onChange(this.state.value);
	}

	focus() {
		ReactDOM.findDOMNode(this.input).focus();
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
					let target = ReactDOM.findDOMNode(this.input);
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

}
export default  Input;

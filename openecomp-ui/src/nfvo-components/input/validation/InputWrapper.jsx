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
import Radio from 'sdc-ui/lib/react/Radio.js';
import FormGroup from 'react-bootstrap/lib/FormGroup.js';
import FormControl from 'react-bootstrap/lib/FormControl.js';

class InputWrapper extends React.Component {

	state = {
		value: this.props.value,
		checked: this.props.checked,
		selectedValues: []
	}

	render() {
		const {label, hasError, validations = {}, isReadOnlyMode, value, onBlur, onKeyDown, type, disabled, checked, name} = this.props;
		const {groupClassName, ...inputProps} = this.props;
		return(
			<FormGroup className={classNames('form-group', [groupClassName], {'required' : validations.required , 'has-error' : hasError})} >
				{(label && (type !== 'checkbox' && type !== 'radio')) && <label className='control-label'>{label}</label>}
				{(type === 'text' || type === 'number') &&
					<FormControl
						bsClass={'form-control input-options-other'}
						onChange={(e) => this.onChange(e)}
						disabled={isReadOnlyMode || Boolean(disabled)}
						onBlur={onBlur}
						onKeyDown={onKeyDown}
						value={value || ''}
						ref={(input) => this.inputWrapper = input}
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
						data-test-id={this.props['data-test-id']}/>}

				{type === 'checkbox' &&
					<Checkbox
						className={classNames({'required' : validations.required , 'has-error' : hasError})}
						onChange={(e)=>this.onChangeCheckBox(e)}
						disabled={isReadOnlyMode || Boolean(disabled)}
						checked={value}
						data-test-id={this.props['data-test-id']}>{label}</Checkbox>}

				{type === 'radio' &&
					<Radio name={name}
						checked={checked}
						disabled={isReadOnlyMode || Boolean(disabled)}
						value={value}
						ref={(input) => this.inputWrapper = input}
						onChange={(isChecked)=>this.onChangeRadio(isChecked)} label={label}
						data-test-id={this.props['data-test-id']} />}
				{type === 'select' &&
					<FormControl onClick={ (e) => this.optionSelect(e) }
						componentClass={type}
						name={name} {...inputProps}
						data-test-id={this.props['data-test-id']}/>}

			</FormGroup>

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
		let {onChange} = this.props;
		this.setState({
			value: e.target.value
		});
		onChange(e.target.value);
	}

	onChangeCheckBox(e) {
		let {onChange} = this.props;
		this.setState({
			checked: e.target.checked
		});
		onChange(e.target.checked);
	}

	onChangeRadio(isChecked) {
		let {onChange} = this.props;
		this.setState({
			checked: isChecked
		});
		onChange(this.state.value);
	}

	focus() {
		ReactDOM.findDOMNode(this.inputWrapper).focus();
	}

}
export default  InputWrapper;

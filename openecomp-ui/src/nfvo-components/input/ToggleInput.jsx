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

export default
class ToggleInput extends React.Component {

	static propTypes = {
		label: PropTypes.node,
		value: PropTypes.bool,
		onChange: PropTypes.func,
		disabled: PropTypes.bool
	}

	static defaultProps = {
		value: false,
		label: ''
	}

	state = {
		value: this.props.value
	}

	status() {
		return this.state.value ? 'on' : 'off';
	}

	render() {
		let {label, disabled} = this.props;
		let checked = this.status() === 'on';
		//TODO check onclick
		return (
			<div className='toggle-input-wrapper form-group' onClick={!disabled && this.click}>
				<div className='toggle-input-label'>{label}</div>
				<div className='toggle-switch'>
					<input className='toggle toggle-round-flat' type='checkbox' checked={checked} readOnly/>
					<label></label>
				</div>
			</div>
		);
	}

	click = () => {
		let value = !this.state.value;
		this.setState({value});

		let onChange = this.props.onChange;
		if (onChange) {
			onChange(value);
		}
	}

	getValue() {
		return this.state.value;
	}
}

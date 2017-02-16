import React from 'react';

export default
class ToggleInput extends React.Component {

	static propTypes = {
		label: React.PropTypes.node,
		value: React.PropTypes.bool,
		onChange: React.PropTypes.func,
		disabled: React.PropTypes.bool
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

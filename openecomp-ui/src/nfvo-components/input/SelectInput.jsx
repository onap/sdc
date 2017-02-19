/**
 * The HTML structure here is aligned with bootstrap HTML structure for form elements.
 * In this way we have proper styling and it is aligned with other form elements on screen.
 *
 * Select and MultiSelect options:
 *
 * label - the label to be shown which paired with the input
 *
 * all other "react-select" props - as documented on
 * http://jedwatson.github.io/react-select/
 * or
 * https://github.com/JedWatson/react-select
 */
import React, {Component} from 'react';
import Select from 'react-select';

class SelectInput extends Component {

	inputValue = [];

	render() {
		let {label, value, ...other} = this.props;
		return (
			<div className='validation-input-wrapper dropdown-multi-select'>
				<div className='form-group'>
					{label && <label className='control-label'>{label}</label>}
					<Select ref='_myInput' onChange={value => this.onSelectChanged(value)} {...other} value={value} />
				</div>
			</div>
		);
	}

	getValue() {
		return this.inputValue && this.inputValue.length ? this.inputValue : '';
	}

	onSelectChanged(value) {
		this.props.onMultiSelectChanged(value);
	}

	componentDidMount() {
		let {value} = this.props;
		this.inputValue = value ? value : [];
	}
	componentDidUpdate() {
		if (this.inputValue !== this.props.value) {
			this.inputValue = this.props.value;
		}
	}
}

export default SelectInput;

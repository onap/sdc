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
		title: React.PropTypes.string,
		selectedValue: React.PropTypes.string,
		multiSelectedEnum: React.PropTypes.array,
		selectedEnum: React.PropTypes.string,
		otherValue: React.PropTypes.string,
		onEnumChange: React.PropTypes.func,
		onOtherChange: React.PropTypes.func,
		isRequired: React.PropTypes.bool,
		isMultiSelect: React.PropTypes.bool
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

		let isReadOnlyMode = this.context.isReadOnlyMode;

		return(
			<div className={classNames('form-group', {'required' : validations.required , 'has-error' : hasError})}>
				{label && <label className='control-label'>{label}</label>}
				{isMultiSelect && otherInputDisabled ?
					<Select
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
							ref={'_myInput'}
							label={label}
							className='form-control input-options-select'
							value={currentSelectedEnum}
							style={{'width' : otherInputDisabled ? '100%' : '95px'}}
							onBlur={() => onBlur()}
							disabled={isReadOnlyMode || Boolean(this.props.disabled)}
							onChange={ value => this.enumChanged(value)}
							type='select'>
							{values && values.length && values.map(val => this.renderOptions(val))}
							{onOtherChange && <option key='other' value={other.OTHER}>{i18n(other.OTHER)}</option>}
							{children}
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

	renderOptions(val){
		return(
			<option key={val.enum} value={val.enum}>{val.title}</option>
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
		let {onEnumChange, isMultiSelect, onChange} = this.props;
		this.setState({
			otherInputDisabled: enumValue !== other.OTHER
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
		let {onEnumChange} = this.props;
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
			otherInputDisabled: !selectedValues.includes(i18n(other.OTHER))
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

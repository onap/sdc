import React from 'react';
import SelectInput from 'nfvo-components/input/SelectInput.jsx';

const SelectActionTableCell = ({options, selected, disabled, onChange, clearable = true, placeholder}) => {
	return (
		<div className='select-action-table-cell'>
			<SelectInput
				placeholder={placeholder}
				type='select'
				value={selected}
				data-test-id='select-action-table-dropdown'
				disabled={disabled}
				onChange={option => onChange(option ? option.value : null)}
				clearable={clearable}
				options={options} />
		</div>
	);
};

export default SelectActionTableCell;

import React from 'react';
import PropTypes from 'prop-types';
import DatePicker from 'react-datepicker';
import moment from 'moment';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';

class CustomInput extends React.Component {

	static propTypes = {
		placeHolderText: PropTypes.string,
		onChange: PropTypes.func,
		onClick: PropTypes.func,
		value: PropTypes.string
	};

	render() {
		const {placeholderText, onClick, onClear, inputRef, value: date} = this.props;
		const text = date ? date : placeholderText;
		const textStyle = date ? '' : 'placeholder';
		return (
			<div onClick={onClick} ref={inputRef} className='datepicker-custom-input'>
				<div  className={`datepicker-text ${textStyle}`}>{text}</div>
				{date && <SVGIcon onClick={e => {e.stopPropagation(); onClear();}} name='close' className='clear-input'/>}
				<SVGIcon name='calendar'/>
			</div>
		);
	}
};

const parseDate = (date, format) => {
	return typeof date === 'number' ? moment.unix(date) : moment(date, format);
};

class Datepicker extends React.Component {
	static propTypes = {
		date: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
		format: PropTypes.string,
		onChange: PropTypes.func,
		selectsStart: PropTypes.bool,
		selectsEnd: PropTypes.bool,
		startDate: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
		endDate: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
		disabled: PropTypes.bool,
		label: PropTypes.string,
		isRequired: PropTypes.bool
	}
	render() {
		let {date,  format, onChange, selectsStart = false, startDate = null, endDate = null, selectsEnd = false,
			disabled = false, inputRef} = this.props;
		const placeholderText =  'Enter a date';
		const props = {
			format,
			onChange,
			disabled,
			selected: date ? parseDate(date, format) : date,
			selectsStart,
			selectsEnd,
			placeholderText,
			startDate: startDate ? parseDate(startDate, format) : startDate,
			endDate: endDate ? parseDate(endDate, format) : endDate
		};

		return (
			<div className='customized-date-picker'>
				<DatePicker
					calendarClassName='customized-date-picker-calendar'
					customInput={<CustomInput inputRef={inputRef} onClear={() => onChange(undefined)} placeholderText={placeholderText}/>}
					minDate={selectsEnd && props.startDate}
					maxDate={selectsStart && props.endDate}
					{...props}/>
			</div>
		);
	}
}

export default Datepicker;

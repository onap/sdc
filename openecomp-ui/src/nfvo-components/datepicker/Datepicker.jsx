import React from 'react';
import DatePicker from 'react-datepicker';
import moment from 'moment';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';

class CustomInput extends React.Component {

	static propTypes = {
		placeHolderText: React.PropTypes.string,
		onChange: React.PropTypes.func,
		onClick: React.PropTypes.func,
		value: React.PropTypes.string
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
		date: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]),
		format: React.PropTypes.string,
		onChange: React.PropTypes.func,
		selectsStart: React.PropTypes.bool,
		selectsEnd: React.PropTypes.bool,
		startDate: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]),
		endDate: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]),
		disabled: React.PropTypes.bool,
		label: React.PropTypes.string,
		isRequired: React.PropTypes.bool
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

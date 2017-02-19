import React from 'react';
import FontAwesome from 'react-fontawesome';
import Input from 'react-bootstrap/lib/Input.js';

class DualListboxView extends React.Component {

	static propTypes = {

		availableList: React.PropTypes.arrayOf(React.PropTypes.shape({
			id: React.PropTypes.string.isRequired,
			name: React.PropTypes.string.isRequired
		})),
		filterTitle: React.PropTypes.shape({
			left: React.PropTypes.string,
			right: React.PropTypes.string
		}),
		selectedValuesList: React.PropTypes.arrayOf(React.PropTypes.string),

		onChange: React.PropTypes.func.isRequired
	};

	static defaultProps = {
		selectedValuesList: [],
		availableList: [],
		filterTitle: {
			left: '',
			right: ''
		}
	};

	state = {
		availableListFilter: '',
		selectedValuesListFilter: ''
	};

	static contextTypes = {
		isReadOnlyMode: React.PropTypes.bool
	};

	render() {
		let {availableList, selectedValuesList, filterTitle} = this.props;
		let {availableListFilter, selectedValuesListFilter} = this.state;
		let isReadOnlyMode = this.context.isReadOnlyMode;

		let unselectedList = availableList.filter(availableItem => !selectedValuesList.find(value => value === availableItem.id));
		let selectedList = availableList.filter(availableItem => selectedValuesList.find(value => value === availableItem.id));
		selectedList = selectedList.sort((a, b) => selectedValuesList.indexOf(a.id) - selectedValuesList.indexOf(b.id));

		return (
			<div className='dual-list-box'>
				{this.renderListbox(filterTitle.left, unselectedList, {
					value: availableListFilter,
					ref: 'availableListFilter',
					disabled: isReadOnlyMode,
					onChange: () => this.setState({availableListFilter: this.refs.availableListFilter.getValue()})
				}, {ref: 'availableValues', disabled: isReadOnlyMode})}
				{this.renderOperationsBar(isReadOnlyMode)}
				{this.renderListbox(filterTitle.right, selectedList, {
					value: selectedValuesListFilter,
					ref: 'selectedValuesListFilter',
					disabled: isReadOnlyMode,
					onChange: () => this.setState({selectedValuesListFilter: this.refs.selectedValuesListFilter.getValue()})
				}, {ref: 'selectedValues', disabled: isReadOnlyMode})}
			</div>
		);
	}

	renderListbox(filterTitle, list, filterProps, props) {
		let regExFilter = new RegExp(escape(filterProps.value), 'i');
		let matchedItems = list.filter(item => item.name.match(regExFilter));
		let unMatchedItems = list.filter(item => !item.name.match(regExFilter));


		return (
			<div className='dual-search-multi-select-section'>
				<p>{filterTitle}</p>
				<div className='dual-text-box-search search-wrapper'>
					<Input name='search-input-control' type='text' groupClassName='search-input-control' {...filterProps}/>
					<FontAwesome name='search' className='search-icon'/>
				</div>
				<Input
					multiple
					groupClassName='dual-list-box-multi-select'
					type='select'
					name='dual-list-box-multi-select'
					{...props}>
					{matchedItems.map(item => this.renderOption(item.id, item.name))}
					{matchedItems.length && unMatchedItems.length && <option style={{pointerEvents: 'none'}}>--------------------</option>}
					{unMatchedItems.map(item => this.renderOption(item.id, item.name))}
				</Input>
			</div>
		);
	}

	renderOption(value, name) {
		return (<option className='dual-list-box-multi-select-text' key={value} value={value}>{name}</option>);
	}

	renderOperationsBar(isReadOnlyMode) {
		return (
			<div className={`dual-list-options-bar${isReadOnlyMode ? ' disabled' : ''}`}>
				{this.renderOperationBarButton(() => this.addToSelectedList(), 'angle-right')}
				{this.renderOperationBarButton(() => this.removeFromSelectedList(), 'angle-left')}
				{this.renderOperationBarButton(() => this.addAllToSelectedList(), 'angle-double-right')}
				{this.renderOperationBarButton(() => this.removeAllFromSelectedList(), 'angle-double-left')}
			</div>
		);
	}

	renderOperationBarButton(onClick, fontAwesomeIconName){
		return (<div className='dual-list-option' onClick={onClick}><FontAwesome name={fontAwesomeIconName}/></div>);
	}

	addToSelectedList() {
		this.props.onChange(this.props.selectedValuesList.concat(this.refs.availableValues.getValue()));
	}

	removeFromSelectedList() {
		const selectedValues = this.refs.selectedValues.getValue();
		this.props.onChange(this.props.selectedValuesList.filter(value => !selectedValues.find(selectedValue => selectedValue === value)));
	}

	addAllToSelectedList() {
		this.props.onChange(this.props.availableList.map(item => item.id));
	}

	removeAllFromSelectedList() {
		this.props.onChange([]);
	}
}

export default DualListboxView;

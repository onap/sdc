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
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import Input from 'nfvo-components/input/validation/InputWrapper.jsx';

class DualListboxView extends React.Component {

	static propTypes = {

		availableList: PropTypes.arrayOf(PropTypes.shape({
			id: PropTypes.string.isRequired,
			name: PropTypes.string.isRequired
		})),
		filterTitle: PropTypes.shape({
			left: PropTypes.string,
			right: PropTypes.string
		}),
		selectedValuesList: PropTypes.arrayOf(PropTypes.string),

		onChange: PropTypes.func.isRequired
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
		selectedValuesListFilter: '',
		selectedValues: []
	};

	render() {
		let {availableList, selectedValuesList, filterTitle, isReadOnlyMode} = this.props;
		let {availableListFilter, selectedValuesListFilter} = this.state;

		let unselectedList = availableList.filter(availableItem => !selectedValuesList.find(value => value === availableItem.id));
		let selectedList = availableList.filter(availableItem => selectedValuesList.find(value => value === availableItem.id));
		selectedList = selectedList.sort((a, b) => selectedValuesList.indexOf(a.id) - selectedValuesList.indexOf(b.id));
		return (
			<div className='dual-list-box'>
				{this.renderListbox(filterTitle.left, unselectedList, {
					value: availableListFilter,
					ref: 'availableListFilter',
					disabled: isReadOnlyMode,
					onChange: (value) => this.setState({availableListFilter: value})
				}, {ref: 'availableValues', disabled: isReadOnlyMode, testId: 'available',})}
				{this.renderOperationsBar(isReadOnlyMode)}
				{this.renderListbox(filterTitle.right, selectedList, {
					value: selectedValuesListFilter,
					ref: 'selectedValuesListFilter',
					disabled: isReadOnlyMode,
					onChange: (value) => this.setState({selectedValuesListFilter: value})
				}, {ref: 'selectedValues', disabled: isReadOnlyMode, testId: 'selected'})}
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
					<Input data-test-id={`${props.testId}-search-input`}
						   name='search-input-control' type='text'
						   groupClassName='search-input-control'
						   {...filterProps}/>
					<SVGIcon name='search' className='search-icon'/>
				</div>
				<Input
					multiple
					onChange={(event) => this.onSelectItems(event.target.selectedOptions)}
					groupClassName='dual-list-box-multi-select'
					type='select'
					name='dual-list-box-multi-select'
					data-test-id={`${props.testId}-select-input`}
					disabled={props.disabled}
					ref={props.ref}>
					{matchedItems.map(item => this.renderOption(item.id, item.name))}
					{matchedItems.length && unMatchedItems.length && <option style={{pointerEvents: 'none'}}>--------------------</option>}
					{unMatchedItems.map(item => this.renderOption(item.id, item.name))}
				</Input>
			</div>
		);
	}

	onSelectItems(selectedOptions) {
		let selectedValues = Object.keys(selectedOptions).map((k) => selectedOptions[k].value);
		this.setState({selectedValues});
	}

	renderOption(value, name) {
		return (<option className='dual-list-box-multi-select-text' key={value} value={value}>{name}</option>);
	}

	renderOperationsBar(isReadOnlyMode) {
		return (
			<div className={`dual-list-options-bar${isReadOnlyMode ? ' disabled' : ''}`}>
				{this.renderOperationBarButton(() => this.addToSelectedList(), 'angleRight')}
				{this.renderOperationBarButton(() => this.removeFromSelectedList(), 'angleLeft')}
				{this.renderOperationBarButton(() => this.addAllToSelectedList(), 'angleDoubleRight')}
				{this.renderOperationBarButton(() => this.removeAllFromSelectedList(), 'angleDoubleLeft')}
			</div>
		);
	}

	renderOperationBarButton(onClick, iconName){
		return (<div className='dual-list-option' data-test-id={`operation-icon-${iconName}`} onClick={onClick}><SVGIcon name={iconName}/></div>);
	}

	addToSelectedList() {
		this.props.onChange(this.props.selectedValuesList.concat(this.state.selectedValues));
		this.setState({selectedValues: []});
	}

	removeFromSelectedList() {
		const selectedValues = this.state.selectedValues;
		this.props.onChange(this.props.selectedValuesList.filter(value => !selectedValues.find(selectedValue => selectedValue === value)));
		this.setState({selectedValues: []});
	}

	addAllToSelectedList() {
		this.props.onChange(this.props.availableList.map(item => item.id));
	}

	removeAllFromSelectedList() {
		this.props.onChange([]);
	}
}

export default DualListboxView;

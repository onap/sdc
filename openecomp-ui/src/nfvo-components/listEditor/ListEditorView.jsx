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
import Button from 'sdc-ui/lib/react/Button.js';
import classnames from 'classnames';
import ExpandableInput from 'nfvo-components/input/ExpandableInput.jsx';

const ListEditorHeader = ({onAdd, isReadOnlyMode, title, plusButtonTitle}) => {
	 return (
		 <div className='list-editor-view-header'>
			 {title && <div className='list-editor-view-title'>{title}</div>}
			 <div>
				 { onAdd &&
					 <Button data-test-id='add-button' iconName='plusThin' btnType='link' onClick={onAdd} disabled={isReadOnlyMode === true}>{plusButtonTitle}</Button>
				 }
			 </div>
		 </div>
	 );
};

const ListEditorScroller = ({children, twoColumns}) => {
	return (
		<div className='list-editor-view-list-scroller'>
			<div className={classnames('list-editor-view-list', {'two-columns': twoColumns})}>
				{children}
			</div>
		</div>
	);
};

const FilterWrapper = ({onFilter, filterValue}) => {
	return (
		<div className='expandble-search-wrapper'>
			<ExpandableInput
				onChange={onFilter}
				iconType='search'
				value={filterValue}/>
		</div>
	);
};

class ListEditorView extends React.Component {

	static defaultProps = {
		className: '',
		twoColumns: false
	};

	static propTypes = {
		title: PropTypes.string,
		plusButtonTitle: PropTypes.string,
		children: PropTypes.node,
		filterValue: PropTypes.string,
		onFilter: PropTypes.func,
		className: PropTypes.string,
		isReadOnlyMode: PropTypes.bool,
		placeholder: PropTypes.string,
		twoColumns: PropTypes.bool
	};

	render() {
		let {title, plusButtonTitle, onAdd, children, onFilter, className, isReadOnlyMode, twoColumns, filterValue} = this.props;
		return (
			<div className={classnames('list-editor-view', className)}>
				<ListEditorHeader onAdd={onAdd} isReadOnlyMode={isReadOnlyMode} plusButtonTitle={plusButtonTitle} title={title}/>
				{onFilter && (children.length || filterValue) && <FilterWrapper onFilter={onFilter} filterValue={filterValue}/>}
				<ListEditorScroller children={children} twoColumns={twoColumns}/>
			</div>
		);
	}

}
export default ListEditorView;

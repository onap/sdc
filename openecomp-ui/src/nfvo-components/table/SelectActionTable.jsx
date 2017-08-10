import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import Button from 'sdc-ui/lib/react/Button.js';
import uuid from 'uuid-js';

export default class SelectActionTable extends React.Component {

	render() {
		let {columns, onAdd, isReadOnlyMode, children, onAddItem, numOfIcons} = this.props;
		return (
			<div className={`select-action-table-view ${isReadOnlyMode ? 'disabled' : ''}`}>
				<div className='select-action-table-controllers'>
					{onAdd && onAddItem &&
					<Button btnType='link' disabled={isReadOnlyMode === true} color='primary' iconName='plus' data-test-id='select-action-table-add' onClick={onAdd}>{onAddItem}</Button>}
					<SVGIcon name='trashO' className='dummy-icon' />
				</div>
				<div className='select-action-table'>
					<div className='select-action-table-headers'>
						{columns.map(column => <div key={uuid.create()} className='select-action-table-header'>{i18n(column)}</div>)}
						{Array(numOfIcons).fill().map((e, i) => <SVGIcon name='trash-o' key={i} className='dummy-icon' />)}
					</div>
					<div className='select-action-table-body'>
						{children}
					</div>
				</div>
			</div>
		);
	}
}

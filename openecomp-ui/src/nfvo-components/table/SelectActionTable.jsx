import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'nfvo-components/icon/SVGIcon.jsx';
import uuid from 'uuid-js';

export default class SelectActionTable extends React.Component {

	render() {
		let {columns, onAdd, isReadOnlyMode, children, onAddItem} = this.props;
		return (
			<div className={`select-action-table-view ${isReadOnlyMode ? 'disabled' : ''}`}>
				<div className='select-action-table-controllers'>
					{onAdd && onAddItem && <div data-test-id='select-action-table-add' onClick={onAdd}>{onAddItem}</div>}
					<SVGIcon name='trash-o' className='dummy-icon' />
				</div>
				<div className='select-action-table'>
					<div className='select-action-table-headers'>
						{columns.map(column => <div key={uuid.create()} className='select-action-table-header'>{i18n(column)}</div>)}
						<SVGIcon name='trash-o' className='dummy-icon' />
						<SVGIcon name='trash-o' className='dummy-icon' />
					</div>
					<div className='select-action-table-body'>
						{children}
					</div>
				</div>
			</div>
		);
	}
}

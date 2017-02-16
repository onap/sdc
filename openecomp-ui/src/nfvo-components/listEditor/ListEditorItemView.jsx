import React from 'react';
import FontAwesome from 'react-fontawesome';
import store from 'sdc-app/AppStore.js';
import NotificationConstants from 'nfvo-components/notifications/NotificationConstants.js';

class ListEditorItem extends React.Component {
	static propTypes = {
		onSelect: React.PropTypes.func,
		onDelete: React.PropTypes.func,
		onEdit: React.PropTypes.func,
		children: React.PropTypes.node,
		isReadOnlyMode: React.PropTypes.bool
	}

	render() {
		let {onDelete, onSelect, onEdit, children, isReadOnlyMode} = this.props;
		let isAbilityToDelete = isReadOnlyMode === undefined ? true : !isReadOnlyMode;
		return (
			<div className='list-editor-item-view'>
				<div className='list-editor-item-view-content' onClick={onSelect}>
					{children}
				</div>
				<div className='list-editor-item-view-controller'>
					{onEdit && <FontAwesome name='sliders' onClick={() => this.onClickedItem(onEdit)}/>}
					{onDelete && isAbilityToDelete && <FontAwesome name='trash-o' onClick={() => this.onClickedItem(onDelete)}/>}
				</div>
			</div>
		);
	}

	onClickedItem(callBackFunc) {
		if(typeof callBackFunc === 'function') {
			let {isCheckedOut} = this.props;
			if (isCheckedOut === false) {
				store.dispatch({
					type: NotificationConstants.NOTIFY_ERROR,
					data: {title: 'Error', msg: 'This item is checkedin/submitted, Click Check Out to continue'}
				});
			}
			else {
				callBackFunc();
			}
		}
	}
}

export default ListEditorItem;

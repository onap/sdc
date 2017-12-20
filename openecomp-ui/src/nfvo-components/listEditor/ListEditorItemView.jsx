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
import classnames from 'classnames';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import store from 'sdc-app/AppStore.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';

class ListEditorItem extends React.Component {
	static propTypes = {
		onSelect:  PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
		onDelete:  PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
		onEdit:  PropTypes.oneOfType([PropTypes.func, PropTypes.bool]),
		children: PropTypes.node,
		isReadOnlyMode: PropTypes.bool
	};

	render() {
		let {onDelete, onSelect, onEdit, children, isReadOnlyMode} = this.props;
		let isAbilityToDelete = isReadOnlyMode === undefined ? true : !isReadOnlyMode;
		return (
			<div className={classnames('list-editor-item-view', {'selectable': Boolean(onSelect)})} data-test-id='list-editor-item'>
				<div className='list-editor-item-view-content' onClick={onSelect}>
					{children}
				</div>
				{(onEdit || onDelete) && <div className='list-editor-item-view-controller'>
					{onEdit && <SVGIcon name='sliders' onClick={() => this.onClickedItem(onEdit)}/>}
					{onDelete && isAbilityToDelete && <SVGIcon name='trashO' data-test-id='delete-list-item' onClick={() => this.onClickedItem(onDelete)}/>}
				</div>}
			</div>
		);
	}

	onClickedItem(callBackFunc) {
		if(typeof callBackFunc === 'function') {
			let {isCheckedOut} = this.props;
			if (isCheckedOut === false) {
				store.dispatch({
					type: modalActionTypes.GLOBAL_MODAL_WARNING,
					data: {
						title: i18n('Error'),
						msg: i18n('This item is checkedin/submitted, Click Check Out to continue')
					}
				});
			}
			else {
				callBackFunc();
			}
		}
	}
}

export default ListEditorItem;

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
import {connect} from 'react-redux';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import ComputeFlavorActionHelper from 'sdc-app/onboarding/softwareProduct/components/compute/ComputeFlavorActionHelper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';

const mapActionsToProps = (dispatch, {softwareProductId, componentId, version}) => {
	return {
		onAddComputeClick: (isReadOnlyMode) => ComputeFlavorActionHelper.openComputeEditor(dispatch, {props: {softwareProductId, componentId, isReadOnlyMode, version}}),
		onEditCompute: ({computeId, isReadOnlyMode}) => ComputeFlavorActionHelper.loadCompute(dispatch, {softwareProductId, componentId, version, computeId, isReadOnlyMode}),
		onDeleteCompute: ({id, name}) => dispatch({
			type: modalActionTypes.GLOBAL_MODAL_WARNING,
			data:{
				msg: i18n('Are you sure you want to delete "{name}"?', {name: name}),
				onConfirmed: () => ComputeFlavorActionHelper.deleteCompute(dispatch, {softwareProductId, componentId, computeId: id, version})
			}
		})
	};
};

const computeItemPropType = PropTypes.shape({
	id: PropTypes.string,
	name: PropTypes.string,
	description: PropTypes.string
});

class ComputeFlavors extends React.Component {

	static propTypes = {
		isReadOnlyMode: PropTypes.bool,
		isManual: PropTypes.bool,
		onAddComputeClick: PropTypes.func,
		computeFlavorsList: PropTypes.arrayOf(computeItemPropType)
	};

	state = {
		localFilter: ''
	};

	render() {
		const {localFilter} = this.state;
		const {isReadOnlyMode, isManual, onAddComputeClick, onEditCompute, onDeleteCompute} = this.props;
		return (
			<div className='computes-list'>
				<ListEditorView
					title={i18n('Computes')}
					plusButtonTitle={i18n('Add Compute')}
					onAdd={isManual ? () => onAddComputeClick(isReadOnlyMode) : null}
					isReadOnlyMode={isReadOnlyMode}
					onFilter={isManual ? value => this.setState({localFilter: value}) : null}
					filterValue={localFilter}
					twoColumns>
					{this.filterList().map(computeItem =>
						<ComputeItem key={computeItem.id}
							computeItem={computeItem} isReadOnlyMode={isReadOnlyMode} isManual={isManual}
							onEditCompute={onEditCompute} onDeleteCompute={onDeleteCompute}/>)
					}
				</ListEditorView>
			</div>
		);
	}

	filterList() {
		const {computeFlavorsList = []} = this.props;

		const {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return computeFlavorsList.filter(({name = '', description = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return computeFlavorsList;
		}
	}
}

const ComputeItem = ({computeItem, isReadOnlyMode, isManual, onEditCompute, onDeleteCompute}) => {
	const {id, name, description} = computeItem;
	return (
		<ListEditorItemView
			key={'item_' + id}
			className='list-editor-item-view'
			isReadOnlyMode={isReadOnlyMode}
			onSelect={() => onEditCompute({computeId: id, isReadOnlyMode})}
			onDelete={isManual ? () => onDeleteCompute({id, name}) : null}>

			<div className='list-editor-item-view-field'>
				<div className='name'>{name}</div>
			</div>
			<div className='list-editor-item-view-field'>
				<div className='description'>{description}</div>
			</div>
		</ListEditorItemView>
	);
};

export default connect(null, mapActionsToProps, null, {withRef: true})(ComputeFlavors);

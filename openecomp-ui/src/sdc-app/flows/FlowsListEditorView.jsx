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
import React, {Component} from 'react';
import PropTypes from 'prop-types';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import FlowRelatedView from './ImportantLogic.jsx';
import FlowsEditorModal from './FlowsEditorModal.js';
import SequenceDiagram from './SequenceDiagram.jsx';

class FlowsListEditorView extends Component {

	static propTypes = {
		flowList: PropTypes.array,
		currentFlow: PropTypes.object,
		isDisplayModal: PropTypes.bool,
		isModalInEditMode: PropTypes.bool,
		isCheckedOut: PropTypes.bool,
		shouldShowWorkflowsEditor: PropTypes.bool,
		readonly: PropTypes.bool,

		onAddWorkflowClick: PropTypes.func,
		onEditFlowDetailsClick: PropTypes.func,
		onEditFlowDiagramClick: PropTypes.func,
		onDeleteFlowClick: PropTypes.func,
		onSequenceDiagramSaveClick: PropTypes.func,
		onSequenceDiagramCloseClick: PropTypes.func
	};

	state = {
		localFilter: ''
	};

	render() {
		let CurrentView = null;
		if (this.props.shouldShowWorkflowsEditor) {
			CurrentView = this.renderWorkflowsEditor();
		}
		else {
			CurrentView = this.renderSequenceDiagramTool();
		}

		return CurrentView;
	}

	renderWorkflowsEditor() {
		let {isDisplayModal, onAddWorkflowClick, isCheckedOut} = this.props;
		const {localFilter} = this.state;

		return (
			<div className='workflows license-agreement-list-editor'>
				<FlowRelatedView display={localFilter}/>
				<ListEditorView
					plusButtonTitle={i18n('Add Workflow')}
					onAdd={onAddWorkflowClick}
					filterValue={localFilter}
					onFilter={filter => this.setState({localFilter: filter})}
					isReadOnlyMode={!isCheckedOut}>
					{this.filterList().map(flow => this.renderWorkflowListItem(flow, isCheckedOut))}
				</ListEditorView>

				{isDisplayModal && this.renderWorkflowEditorModal()}

			</div>
		);
	}

	renderWorkflowEditorModal() {
		let { isDisplayModal, isModalInEditMode} = this.props;
		return (
			<Modal show={isDisplayModal} animation={true} className='onborading-modal workflows-editor-modal'>
				<Modal.Header>
					<Modal.Title>
						{`${isModalInEditMode ? i18n('Edit Workflow') : i18n('Create New Workflow')}`}
					</Modal.Title>
				</Modal.Header>
				<Modal.Body>
					<FlowsEditorModal isNewArtifact={!isModalInEditMode}/>
				</Modal.Body>
			</Modal>
		);
	}

	renderSequenceDiagramTool() {
		let {onSequenceDiagramSaveClick, onSequenceDiagramCloseClick, currentFlow} = this.props;
		return (
			<SequenceDiagram
				onSave={sequenceDiagramModel => onSequenceDiagramSaveClick({...currentFlow, sequenceDiagramModel})}
				onClose={onSequenceDiagramCloseClick}
				model={currentFlow.sequenceDiagramModel}/>
		);
	}

	filterList() {
		let {flowList} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return flowList.filter(({name = '', description = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return flowList;
		}
	}

	renderWorkflowListItem(flow, isCheckedOut) {
		let {uniqueId, artifactName, description} = flow;
		let {onEditFlowDetailsClick, onEditFlowDiagramClick, onDeleteFlowClick} = this.props;
		return (
			<ListEditorItemView
				key={uniqueId}
				onSelect={() => onEditFlowDetailsClick(flow)}
				onDelete={() => onDeleteFlowClick(flow)}
				onEdit={() => onEditFlowDiagramClick(flow)}
				className='list-editor-item-view'
				isCheckedOut={isCheckedOut}>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Name')}</div>
					<div className='text name'>{artifactName}</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Description')}</div>
					<div className='text description'>{description}</div>
				</div>
			</ListEditorItemView>
		);
	}

}

export default FlowsListEditorView;

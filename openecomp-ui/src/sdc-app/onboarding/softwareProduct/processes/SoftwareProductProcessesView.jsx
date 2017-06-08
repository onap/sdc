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
import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

import SoftwareProductProcessesEditor from './SoftwareProductProcessesEditor.js';



class SoftwareProductProcessesView extends React.Component {

	state = {
		localFilter: ''
	};

	static propTypes = {
		onAddProcess: React.PropTypes.func.isRequired,
		onEditProcess: React.PropTypes.func.isRequired,
		onDeleteProcess: React.PropTypes.func.isRequired,
		isDisplayEditor: React.PropTypes.bool.isRequired,
		isReadOnlyMode: React.PropTypes.bool.isRequired,
		currentSoftwareProduct:React.PropTypes.object
	};

	render() {
		return (
			<div className='software-product-landing-view-right-side vsp-processes-page'>
				{this.renderEditor()}
				{this.renderProcessList()}
			</div>
		);
	}

	renderEditor() {
		let {currentSoftwareProduct: {id, version}, isModalInEditMode, isReadOnlyMode, isDisplayEditor} = this.props;
		return (

			<Modal show={isDisplayEditor} bsSize='large' animation={true} className='onborading-modal'>
				<Modal.Header>
					<Modal.Title>{isModalInEditMode ? i18n('Edit Process Details') : i18n('Create New Process Details')}</Modal.Title>
				</Modal.Header>
				<Modal.Body className='edit-process-modal'>
					<SoftwareProductProcessesEditor softwareProductId={id} version={version} isReadOnlyMode={isReadOnlyMode}/>
				</Modal.Body>
			</Modal>
		);
	}

	renderProcessList() {
		const {localFilter} = this.state;
		let {onAddProcess, isReadOnlyMode} = this.props;

		return (
			<ListEditorView
				plusButtonTitle={i18n('Add Process Details')}
				filterValue={localFilter}
				placeholder={i18n('Filter Process')}
				onAdd={onAddProcess}
				isReadOnlyMode={isReadOnlyMode}
				title={i18n('Process Details')}
				onFilter={value => this.setState({localFilter: value})}>
				{this.filterList().map(processes => this.renderProcessListItem(processes, isReadOnlyMode))}
			</ListEditorView>
		);
	}

	renderProcessListItem(process, isReadOnlyMode) {
		let {id, name, description, artifactName = ''} = process;
		let {currentSoftwareProduct: {version}, onEditProcess, onDeleteProcess} =  this.props;
		return (
			<ListEditorItemView
				key={id}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}
				onSelect={() => onEditProcess(process)}
				onDelete={() => onDeleteProcess(process, version)}>

				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Name')}</div>
					<div className='name'>{name}</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Artifact name')}</div>
					<div className='artifact-name'>{artifactName}</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Notes')}</div>
					<div className='description'>{description}</div>
				</div>
			</ListEditorItemView>
		);
	}

	filterList() {
		let {processesList} = this.props;
		let {localFilter} = this.state;

		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return processesList.filter(({name = '', description = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return processesList;
		}
	}
}

export default SoftwareProductProcessesView;

import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

import SoftwareProductProcessesEditor from './SoftwareProductProcessesEditor.js';
import SoftwareProductProcessesConfirmationModal  from './SoftwareProductProcessesConfirmationModal.jsx';


class SoftwareProductProcessesView extends React.Component {

	state = {
		localFilter: ''
	};

	static propTypes = {
		onAddProcess: React.PropTypes.func.isRequired,
		onEditProcess: React.PropTypes.func.isRequired,
		onDeleteProcess: React.PropTypes.func.isRequired,
		isDisplayEditor: React.PropTypes.bool.isRequired,
		isReadOnlyMode: React.PropTypes.bool.isRequired
	};

	render() {
		let { currentSoftwareProduct} = this.props;
		return (
			<div className='software-product-landing-view-right-side vsp-processes-page'>
				{this.renderEditor()}
				{this.renderProcessList()}
				<SoftwareProductProcessesConfirmationModal softwareProductId={currentSoftwareProduct.id}/>
			</div>
		);
	}

	renderEditor() {
		let {currentSoftwareProduct: {id}, isModalInEditMode, isReadOnlyMode, isDisplayEditor} = this.props;
		return (

			<Modal show={isDisplayEditor} bsSize='large' animation={true}>
				<Modal.Header>
					<Modal.Title>{isModalInEditMode ? i18n('Edit Process Details') : i18n('Create New Process Details')}</Modal.Title>
				</Modal.Header>
				<Modal.Body className='edit-process-modal'>
					<SoftwareProductProcessesEditor softwareProductId={id} isReadOnlyMode={isReadOnlyMode}/>
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
				onFilter={filter => this.setState({localFilter: filter})}>
				{this.filterList().map(processes => this.renderProcessListItem(processes, isReadOnlyMode))}
			</ListEditorView>
		);
	}

	renderProcessListItem(process, isReadOnlyMode) {
		let {id, name, description, artifactName = ''} = process;
		let {onEditProcess, onDeleteProcess} =  this.props;
		return (
			<ListEditorItemView
				key={id}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}
				onSelect={() => onEditProcess(process)}
				onDelete={() => onDeleteProcess(process)}>

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

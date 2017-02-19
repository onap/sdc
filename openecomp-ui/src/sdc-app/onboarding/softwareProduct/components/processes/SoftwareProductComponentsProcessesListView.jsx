import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

import SoftwareProductProcessesEditor from './SoftwareProductComponentProcessesEditor.js';
import SoftwareProductComponentsProcessesConfirmationModal from './SoftwareProductComponentsProcessesConfirmationModal.jsx';

class SoftwareProductProcessesView extends React.Component {

	state = {
		localFilter: ''
	};

	static propTypes = {
		onAddProcess: React.PropTypes.func,
		onEditProcessClick: React.PropTypes.func,
		onDeleteProcessClick: React.PropTypes.func,
		isDisplayModal: React.PropTypes.bool,
		isModalInEditMode: React.PropTypes.bool,
		onStorageSelect: React.PropTypes.func,
		componentId: React.PropTypes.string,
		softwareProductId: React.PropTypes.string
	};

	render() {
		let { softwareProductId, componentId} = this.props;

		return (
			<div className='vsp-processes-page'>
				<div className='software-product-view'>
					<div className='software-product-landing-view-right-side flex-column'>
						{this.renderEditor()}
						{this.renderProcessList()}
					</div>
					<SoftwareProductComponentsProcessesConfirmationModal
						componentId={componentId}
						softwareProductId={softwareProductId}/>
				</div>
			</div>
		);
	}

	renderEditor() {
		let {softwareProductId, componentId, isReadOnlyMode, isDisplayModal, isModalInEditMode} = this.props;
		return (
			<Modal show={isDisplayModal} bsSize='large' animation={true}>
				<Modal.Header>
					<Modal.Title>{isModalInEditMode ? i18n('Edit Process Details') : i18n('Create New Process Details')}</Modal.Title>
				</Modal.Header>
				<Modal.Body className='edit-process-modal'>
					<SoftwareProductProcessesEditor
						componentId={componentId}
						softwareProductId={softwareProductId}
						isReadOnlyMode={isReadOnlyMode}/>
				</Modal.Body>
			</Modal>

		);
	}

	renderProcessList() {
		const {localFilter} = this.state;
		let {onAddProcess, isReadOnlyMode} = this.props;
		return (
			<div className='processes-list'>
				<ListEditorView
					plusButtonTitle={i18n('Add Component Process Details')}
					filterValue={localFilter}
					placeholder={i18n('Filter Process')}
					onAdd={onAddProcess}
					isReadOnlyMode={isReadOnlyMode}
					onFilter={filter => this.setState({localFilter: filter})}>
					{this.filterList().map(processes => this.renderProcessListItem(processes, isReadOnlyMode))}
				</ListEditorView>
			</div>
		);
	}

	renderProcessListItem(process, isReadOnlyMode) {
		let {id, name, description, artifactName = ''} = process;
		let {onEditProcessClick, onDeleteProcessClick} =  this.props;
		return (
			<ListEditorItemView
				key={id}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}
				onSelect={() => onEditProcessClick(process)}
				onDelete={() => onDeleteProcessClick(process)}>

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

import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';

import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import ValidationInput from'nfvo-components/input/validation/ValidationInput.jsx';
import Modal from 'nfvo-components/modal/Modal.jsx';

import SoftwareProductComponentsNICEditor from './SoftwareProductComponentsNICEditor.js';

class SoftwareProductComponentsNetworkView extends React.Component {

	state = {
		localFilter: ''
	};

	render() {
		let {qdata, qschema, onQDataChanged, isModalInEditMode, isDisplayModal, softwareProductId, componentId, isReadOnlyMode} = this.props;

		return(
			<div className='vsp-components-network'>
				<div className='network-data'>
					<div>
						<ValidationForm
							onDataChanged={onQDataChanged}
							data={qdata}
							isReadOnlyMode={isReadOnlyMode}
							schema={qschema}
							hasButtons={false}>
							<h3 className='section-title'>{i18n('Network Capacity')}</h3>
							<div className='rows-section'>
								<div className='row-flex-components input-row'>
									<div className='single-col'>
										<ValidationInput
											label={i18n('Protocol with Highest Traffic Profile across all NICs')}
											type='select'
											pointer='/network/networkCapacity/protocolWithHighestTrafficProfileAcrossAllNICs'/>
									</div>
									<div className='single-col add-line-break'>
										<ValidationInput
											label={i18n('Network Transactions per Second')}
											type='text'
											pointer='/network/networkCapacity/networkTransactionsPerSecond'/>
									</div>
									<div className='empty-two-col' />
								</div>
							</div>

						</ValidationForm>
					</div>
					{this.renderNicList()}
				</div>
				<Modal show={isDisplayModal} bsSize='large' animation={true} className='network-nic-modal'>
					<Modal.Header>
						<Modal.Title>{isModalInEditMode ? i18n('Edit NIC') : i18n('Create New NIC')}</Modal.Title>
					</Modal.Header>
					<Modal.Body>
						{
							<SoftwareProductComponentsNICEditor
								softwareProductId={softwareProductId}
								componentId={componentId}
								isReadOnlyMode={isReadOnlyMode}/>
						}
					</Modal.Body>
				</Modal>
			</div>
		);
	}

	renderNicList() {
		const {localFilter} = this.state;
		let {onAddNIC, manualMode, isReadOnlyMode} = this.props;
		let onAdd = manualMode ? onAddNIC : false;
		return (
			<ListEditorView
				title={i18n('Interfaces')}
				plusButtonTitle={i18n('Add NIC')}
				filterValue={localFilter}
				placeholder={i18n('Filter NICs by Name')}
				onAdd={onAdd}
				isReadOnlyMode={isReadOnlyMode}
				onFilter={filter => this.setState({localFilter: filter})}>
				{!manualMode && this.filterList().map(nic => this.renderNicListItem(nic, isReadOnlyMode))}
			</ListEditorView>
		);
	}

	renderNicListItem(nic, isReadOnlyMode) {
		let {id, name, description, networkName = ''} = nic;
		let {onEditNicClick, version} =  this.props;
		return (
			<ListEditorItemView
				key={id}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}
				onSelect={() => onEditNicClick(nic, version)}>

				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Name')}</div>
					<div className='name'>{name}</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Purpose of NIC')}</div>
					<div className='description'>{description}</div>
				</div>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Network')}</div>
					<div className='artifact-name'>{networkName}</div>
				</div>

			</ListEditorItemView>
		);
	}

	filterList() {
		let {nicList} = this.props;
		let {localFilter} = this.state;
		if (localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return nicList.filter(({name = '', description = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return nicList;
		}
	}

	save() {
		let {onSubmit, qdata} = this.props;
		return onSubmit({qdata});
	}
}

export default SoftwareProductComponentsNetworkView;

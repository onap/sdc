import React from 'react';

import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorItemView from 'nfvo-components/listEditor/ListEditorItemView.jsx';

import EntitlementPoolsEditor from './EntitlementPoolsEditor.js';
import InputOptions, {other as optionInputOther} from 'nfvo-components/input/inputOptions/InputOptions.jsx';
import {optionsInputValues} from './EntitlementPoolsConstants';
import EntitlementPoolsConfirmationModal from './EntitlementPoolsConfirmationModal.jsx';


class EntitlementPoolsListEditorView extends React.Component {
	static propTypes = {
		vendorName: React.PropTypes.string,
		licenseModelId: React.PropTypes.string.isRequired,
		entitlementPoolsList: React.PropTypes.array,
		isReadOnlyMode: React.PropTypes.bool.isRequired,
		isDisplayModal: React.PropTypes.bool,
		isModalInEditMode: React.PropTypes.bool,
		onAddEntitlementPoolClick: React.PropTypes.func,
		onEditEntitlementPoolClick: React.PropTypes.func,
		onDeleteEntitlementPool: React.PropTypes.func,
	};

	static defaultProps = {
		entitlementPoolsList: []
	};

	state = {
		localFilter: ''
	};

	render() {
		let {licenseModelId, vendorName, isReadOnlyMode, isDisplayModal, isModalInEditMode} = this.props;
		let {onAddEntitlementPoolClick} = this.props;
		const {localFilter} = this.state;

		return (
			<div className='entitlement-pools-list-editor'>
				<ListEditorView
					title={i18n('Entitlement Pools for {vendorName} License Model', {vendorName})}
					plusButtonTitle={i18n('Add Entitlement Pool')}
					onAdd={onAddEntitlementPoolClick}
					filterValue={localFilter}
					onFilter={filter => this.setState({localFilter: filter})}
					isReadOnlyMode={isReadOnlyMode}>
					{this.filterList().map(entitlementPool => this.renderEntitlementPoolListItem(entitlementPool, isReadOnlyMode))}
				</ListEditorView>
				<Modal show={isDisplayModal} bsSize='large' animation={true} className='entitlement-pools-modal'>
					<Modal.Header>
						<Modal.Title>{`${isModalInEditMode ? i18n('Edit Entitlement Pool') : i18n('Create New Entitlement Pool')}`}</Modal.Title>
					</Modal.Header>
					<Modal.Body>
						{
							isDisplayModal && (
								<EntitlementPoolsEditor  licenseModelId={licenseModelId} isReadOnlyMode={isReadOnlyMode}/>
							)
						}
					</Modal.Body>
				</Modal>

				<EntitlementPoolsConfirmationModal licenseModelId={licenseModelId}/>
			</div>
		);
	}

	filterList() {
		let {entitlementPoolsList} = this.props;
		let {localFilter} = this.state;
		if(localFilter.trim()) {
			const filter = new RegExp(escape(localFilter), 'i');
			return entitlementPoolsList.filter(({name = '', description = ''}) => {
				return escape(name).match(filter) || escape(description).match(filter);
			});
		}
		else {
			return entitlementPoolsList;
		}
	}

	renderEntitlementPoolListItem(entitlementPool, isReadOnlyMode) {
		let {id, name, description, thresholdValue, thresholdUnits, entitlementMetric, aggregationFunction,
			manufacturerReferenceNumber, time} = entitlementPool;
		let {onEditEntitlementPoolClick, onDeleteEntitlementPool} = this.props;
		return (
			<ListEditorItemView
				key={id}
				onSelect={() => onEditEntitlementPoolClick(entitlementPool)}
				onDelete={() => onDeleteEntitlementPool(entitlementPool)}
				className='list-editor-item-view'
				isReadOnlyMode={isReadOnlyMode}>
				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Name')}</div>
					<div className='text name'>{name}</div>
				</div>

				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Entitlement')}</div>
					<div className='entitlement-parameters'>{`${this.extractValue(aggregationFunction)} ${this.extractValue(entitlementMetric)} per  ${this.extractValue(time)}`}</div>
					<div className='entitlement-pools-count'>{`${thresholdValue ? thresholdValue : ''} ${this.extractUnits(thresholdUnits)}`}</div>
				</div>

				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Manufacturer Reference Number')}</div>
					<div className='text contract-number'>{manufacturerReferenceNumber}</div>
				</div>

				<div className='list-editor-item-view-field'>
					<div className='title'>{i18n('Description')}</div>
					<div className='text description'>{description}</div>
				</div>
			</ListEditorItemView>
		);
	}



	extractUnits(units) {
		if (units === undefined) {return '';} //TODO fix it later
		return units === 'Absolute' ? '' : '%';
	}

	extractValue(item) {
		if (item === undefined) {return '';} //TODO fix it later

		return  item ? item.choice === optionInputOther.OTHER ? item.other : InputOptions.getTitleByName(optionsInputValues, item.choice) : '';
	}
}

export default EntitlementPoolsListEditorView;

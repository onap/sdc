import React from 'react';
import ValidationTabs from 'nfvo-components/input/validation/ValidationTabs.jsx';
import ValidationTab from 'nfvo-components/input/validation/ValidationTab.jsx';
import ButtonGroup from 'react-bootstrap/lib/ButtonGroup.js';
import Button from 'react-bootstrap/lib/Button.js';

import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import DualListboxView from 'nfvo-components/input/dualListbox/DualListboxView.jsx';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorViewItem from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';

import {state as FeatureGroupStateConstants} from './FeatureGroupsConstants.js';

const FeatureGroupsPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	partNumber: React.PropTypes.string,
	entitlementPoolsIds: React.PropTypes.array(React.PropTypes.string),
	licenseKeyGroupsIds: React.PropTypes.array(React.PropTypes.string)
});

class FeatureGroupEditorView extends React.Component {


	static propTypes = {
		data: FeatureGroupsPropType,
		previousData: FeatureGroupsPropType,
		isReadOnlyMode: React.PropTypes.bool,

		onSubmit: React.PropTypes.func,
		onCancel: React.PropTypes.func,

		selectedTab: React.PropTypes.number,
		onTabSelect: React.PropTypes.func,

		selectedEntitlementPoolsButtonTab: React.PropTypes.number,
		selectedLicenseKeyGroupsButtonTab: React.PropTypes.number,
		onEntitlementPoolsButtonTabSelect: React.PropTypes.func,
		onLicenseKeyGroupsButtonTabSelect: React.PropTypes.func,

		entitlementPoolsList: DualListboxView.propTypes.availableList,
		licenseKeyGroupsList: DualListboxView.propTypes.availableList
	};


	static defaultProps = {
		data: {},
		selectedTab: FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.GENERAL,
		selectedEntitlementPoolsButtonTab: FeatureGroupStateConstants.SELECTED_ENTITLEMENT_POOLS_BUTTONTAB.ASSOCIATED_ENTITLEMENT_POOLS,
		selectedLicenseKeyGroupsButtonTab: FeatureGroupStateConstants.SELECTED_LICENSE_KEY_GROUPS_BUTTONTAB.ASSOCIATED_LICENSE_KEY_GROUPS
	};

	state = {
		localEntitlementPoolsListFilter: '',
		localLicenseKeyGroupsListFilter: ''
	};


	render() {
		let {selectedTab, onTabSelect, isReadOnlyMode} = this.props;
		return (
			<ValidationForm
				ref='validationForm'
				hasButtons={true}
				onSubmit={ () => this.submit() }
				onReset={ () => this.props.onCancel() }
				labledButtons={true}
				isReadOnlyMode={isReadOnlyMode}
				name='feature-group-validation-form'
				className='feature-group-form'>
				<ValidationTabs activeKey={onTabSelect ? selectedTab : undefined} onSelect={onTabSelect}>
					{this.renderGeneralTab()}
					{this.renderEntitlementPoolsTab()}
					{this.renderLicenseKeyGroupsTab()}
				</ValidationTabs>

			</ValidationForm>
		);
	}

	submit() {
		const {data: featureGroup, previousData: previousFeatureGroup} = this.props;
		this.props.onSubmit(previousFeatureGroup, featureGroup);
	}

	renderGeneralTab() {
		let {data = {}, onDataChanged} = this.props;
		let {name, description, partNumber} = data;
		return (
			<ValidationTab eventKey={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.GENERAL} title={i18n('General')}>
				<div>
					<ValidationInput
						groupClassName='field-section'
						onChange={name => onDataChanged({name})}
						ref='name'
						label={i18n('Name')}
						value={name}
						name='feature-group-name'
						validations={{maxLength: 120, required: true}}
						type='text'/>
					<ValidationInput
						groupClassName='field-section'
						className='description-field'
						onChange={description => onDataChanged({description})}
						ref='description'
						label={i18n('Description')}
						value={description}
						name='feature-group-description'
						validations={{maxLength: 1000, required: true}}
						type='textarea'/>
					<ValidationInput
						groupClassName='field-section'
						onChange={partNumber => onDataChanged({partNumber})}
						label={i18n('Part Number')}
						value={partNumber}
						validations={{required: true}}
						type='text'/>
				</div>
			</ValidationTab>
		);
	}

	renderEntitlementPoolsTab() {
		let {selectedEntitlementPoolsButtonTab, onEntitlementPoolsButtonTabSelect, entitlementPoolsList} = this.props;
		if (entitlementPoolsList.length > 0) {
			return (
				<ValidationTab
					eventKey={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.ENTITLEMENT_POOLS}
					title={i18n('Entitlement Pools')}>
					<ButtonGroup>
						{
							this.renderButtonsTab(
								FeatureGroupStateConstants.SELECTED_ENTITLEMENT_POOLS_BUTTONTAB.ASSOCIATED_ENTITLEMENT_POOLS,
								selectedEntitlementPoolsButtonTab,
								i18n('Associated Entitlement Pools'),
								onEntitlementPoolsButtonTabSelect
							)
						}
						{
							this.renderButtonsTab(
								FeatureGroupStateConstants.SELECTED_ENTITLEMENT_POOLS_BUTTONTAB.AVAILABLE_ENTITLEMENT_POOLS,
								selectedEntitlementPoolsButtonTab,
								i18n('Available Entitlement Pools'),
								onEntitlementPoolsButtonTabSelect
							)
						}
					</ButtonGroup>
					{this.renderEntitlementPoolsButtonTabContent(selectedEntitlementPoolsButtonTab)}
				</ValidationTab>
			);
		} else {
			return (
				<ValidationTab
					eventKey={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.ENTITLEMENT_POOLS}
					title={i18n('Entitlement Pools')}>
					<p>{i18n('There is no available entitlement pools.')}</p>
				</ValidationTab>
			);
		}
	}

	renderLicenseKeyGroupsTab() {
		let {selectedLicenseKeyGroupsButtonTab, onLicenseKeyGroupsButtonTabSelect, licenseKeyGroupsList} = this.props;
		if (licenseKeyGroupsList.length > 0) {
			return (
				<ValidationTab
					eventKey={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.LICENCE_KEY_GROUPS}
					title={i18n('License Key Groups')}>
					<ButtonGroup>
						{
							this.renderButtonsTab(
								FeatureGroupStateConstants.SELECTED_LICENSE_KEY_GROUPS_BUTTONTAB.ASSOCIATED_LICENSE_KEY_GROUPS,
								selectedLicenseKeyGroupsButtonTab,
								i18n('Associated License Key Groups'),
								onLicenseKeyGroupsButtonTabSelect
							)
						}
						{
							this.renderButtonsTab(
								FeatureGroupStateConstants.SELECTED_LICENSE_KEY_GROUPS_BUTTONTAB.AVAILABLE_LICENSE_KEY_GROUPS,
								selectedLicenseKeyGroupsButtonTab,
								i18n('Available License Key Groups'),
								onLicenseKeyGroupsButtonTabSelect
							)
						}
					</ButtonGroup>
					{this.renderLicenseKeyGroupsTabContent(selectedLicenseKeyGroupsButtonTab)}
				</ValidationTab>
			);
		} else {
			return (
				<ValidationTab
					eventKey={FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.LICENCE_KEY_GROUPS}
					title={i18n('License Key Groups')}>
					<p>{i18n('There is no available license key groups')}</p>
				</ValidationTab>);
		}
	}

	renderButtonsTab(buttonTab, selectedButtonTab, title, onClick) {
		const isSelected = buttonTab === selectedButtonTab;
		return (
			<Button
				className='button-tab'
				active={isSelected}
				onClick={() => !isSelected && onClick(buttonTab)}>
				{ title }
			</Button>
		);
	}

	renderEntitlementPoolsButtonTabContent(selectedFeatureGroupsButtonTab) {
		const {entitlementPoolsList = [], data: {entitlementPoolsIds = []}} = this.props;
		let dualBoxTitle = {
			left: i18n('Available Entitlement Pools'),
			right: i18n('Selected Entitlement Pools')
		};

		if (entitlementPoolsList.length) {
			const {localEntitlementPoolsListFilter} = this.state;
			let selectedEntitlementPools = entitlementPoolsIds.map(entitlementPoolId => entitlementPoolsList.find(entitlementPool => entitlementPool.id === entitlementPoolId));

			switch (selectedFeatureGroupsButtonTab) {
				case FeatureGroupStateConstants.SELECTED_ENTITLEMENT_POOLS_BUTTONTAB.ASSOCIATED_ENTITLEMENT_POOLS:
					if (selectedEntitlementPools.length) {
						return (
							<ListEditorView
								className='thinner-list'
								filterValue={localEntitlementPoolsListFilter}
								onFilter={localEntitlementPoolsListFilter => this.setState({localEntitlementPoolsListFilter})}>
								{this.filterAssociatedItems(selectedEntitlementPools, localEntitlementPoolsListFilter)
									.map(entitlementPool => this.renderAssociatedListItem(entitlementPool
										, FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.ENTITLEMENT_POOLS))}
							</ListEditorView>
						);
					}
					else {
						return (
							<div>
								<br/>{i18n('There are currently no entitlement pools associated with this feature group. Click "Available Entitlement Pools" to associate.')}
							</div>
						);
					}
				case FeatureGroupStateConstants.SELECTED_ENTITLEMENT_POOLS_BUTTONTAB.AVAILABLE_ENTITLEMENT_POOLS:
					return (
						<DualListboxView
							filterTitle={dualBoxTitle}
							selectedValuesList={entitlementPoolsIds}
							availableList={entitlementPoolsList}
							onChange={ selectedValuesList => this.props.onDataChanged( { entitlementPoolsIds: selectedValuesList } )}/>
					);
			}
		}
	}

	renderLicenseKeyGroupsTabContent(selectedFeatureGroupsButtonTab) {
		const {licenseKeyGroupsList = [], data: {licenseKeyGroupsIds = []}} = this.props;
		let dualBoxFilterTitle = {
			left: i18n('Available License Key Groups'),
			right: i18n('Selected License Key Groups')
		};

		if (licenseKeyGroupsList.length) {
			const {localLicenseKeyGroupsListFilter} = this.state;
			let selectedLicenseKeyGroups = licenseKeyGroupsIds.map(licenseKeyGroupId => licenseKeyGroupsList.find(licenseKeyGroup => licenseKeyGroup.id === licenseKeyGroupId));

			switch (selectedFeatureGroupsButtonTab) {
				case FeatureGroupStateConstants.SELECTED_LICENSE_KEY_GROUPS_BUTTONTAB.ASSOCIATED_LICENSE_KEY_GROUPS:
					if (selectedLicenseKeyGroups.length) {
						return (
							<ListEditorView
								className='thinner-list'
								filterValue={localLicenseKeyGroupsListFilter}
								onFilter={localLicenseKeyGroupsListFilter => this.setState({localLicenseKeyGroupsListFilter})}>
								{this.filterAssociatedItems(selectedLicenseKeyGroups, localLicenseKeyGroupsListFilter)
									.map(licenseKeyGroup => this.renderAssociatedListItem(licenseKeyGroup
										, FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.LICENCE_KEY_GROUPS))}
							</ListEditorView>
						);
					} else {
						return (
							<div className='no-items-msg'>
								{i18n('There are currently no license key groups associated with this feature group. Click "Available License Key Groups" to associate.')}
							</div>
						);
					}
				case FeatureGroupStateConstants.SELECTED_LICENSE_KEY_GROUPS_BUTTONTAB.AVAILABLE_LICENSE_KEY_GROUPS:
					return (
						<DualListboxView
							filterTitle={dualBoxFilterTitle}
							selectedValuesList={this.props.data.licenseKeyGroupsIds}
							availableList={this.props.licenseKeyGroupsList}
							onChange={ selectedValuesList => this.props.onDataChanged( { licenseKeyGroupsIds: selectedValuesList } )}/>
					);
			}
		}
	}


	renderAssociatedListItem(listItem, itemType) {
		let {isReadOnlyMode} = this.props;
		return (
			<ListEditorViewItem
				key={listItem.id}
				onDelete={() => this.deleteAssociatedItem(listItem.id, itemType)}
				isReadOnlyMode={isReadOnlyMode}>
				<div className='name'>{listItem.name}</div>
				<div className='description'>{listItem.description}</div>
			</ListEditorViewItem>
		);
	}

	filterAssociatedItems(list, localList) {
		if (localList) {
			const filter = new RegExp(escape(localList), 'i');
			return list.filter(({name = '', description = ''}) => name.match(filter) || description.match(filter));
		}
		else {
			return list;
		}
	}

	deleteAssociatedItem(id, type) {
		const {data: {licenseKeyGroupsIds = [], entitlementPoolsIds = []}} = this.props;
		if (type === FeatureGroupStateConstants.SELECTED_FEATURE_GROUP_TAB.LICENCE_KEY_GROUPS) {
			this.props.onDataChanged({licenseKeyGroupsIds: licenseKeyGroupsIds.filter(listId => listId !== id)});
		} else {
			this.props.onDataChanged({entitlementPoolsIds: entitlementPoolsIds.filter(listId => listId !== id)});
		}

	}
}


export default FeatureGroupEditorView;


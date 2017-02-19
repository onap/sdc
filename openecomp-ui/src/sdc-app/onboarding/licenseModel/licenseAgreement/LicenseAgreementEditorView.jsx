import React from 'react';
import ButtonGroup from 'react-bootstrap/lib/ButtonGroup.js';
import Button from 'react-bootstrap/lib/Button.js';
import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationTabs from 'nfvo-components/input/validation/ValidationTabs.jsx';
import ValidationTab from 'nfvo-components/input/validation/ValidationTab.jsx';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';
import DualListboxView from 'nfvo-components/input/dualListbox/DualListboxView.jsx';
import ListEditorView from 'nfvo-components/listEditor/ListEditorView.jsx';
import ListEditorViewItem from 'nfvo-components/listEditor/ListEditorItemView.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';

import {enums as LicenseAgreementEnums, optionsInputValues as LicenseAgreementOptionsInputValues} from './LicenseAgreementConstants.js';


const LicenseAgreementPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	requirementsAndConstrains: React.PropTypes.string,
	licenseTerm: React.PropTypes.object,
	featureGroupsIds: React.PropTypes.arrayOf(React.PropTypes.string)
});

class LicenseAgreementEditorView extends React.Component {

	static propTypes = {
		data: LicenseAgreementPropType,
		previousData: LicenseAgreementPropType,
		isReadOnlyMode: React.PropTypes.bool,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired,

		selectedTab: React.PropTypes.number,
		onTabSelect: React.PropTypes.func,

		selectedFeatureGroupsButtonTab: React.PropTypes.number,
		onFeatureGroupsButtonTabSelect: React.PropTypes.func,
		featureGroupsList: DualListboxView.propTypes.availableList
	};

	static defaultProps = {
		selectedTab: LicenseAgreementEnums.SELECTED_LICENSE_AGREEMENT_TAB.GENERAL,
		selectedFeatureGroupsButtonTab: LicenseAgreementEnums.SELECTED_FEATURE_GROUPS_BUTTONTAB.AVAILABLE_FEATURE_GROUPS,
		data: {}
	};

	state = {
		localFeatureGroupsListFilter: ''
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
				className='license-agreement-form'>
				<ValidationTabs activeKey={onTabSelect ? selectedTab : undefined} onSelect={onTabSelect}>
					{this.renderGeneralTab()}
					{this.renderFeatureGroupsTab()}
				</ValidationTabs>
			</ValidationForm>
		);
	}

	submit() {
		const {data: licenseAgreement, previousData: previousLicenseAgreement} = this.props;
		this.props.onSubmit({licenseAgreement, previousLicenseAgreement});
	}

	renderGeneralTab() {
		let {data = {}, onDataChanged} = this.props;
		let {name, description, requirementsAndConstrains, licenseTerm} = data;
		return (
			<ValidationTab
				eventKey={LicenseAgreementEnums.SELECTED_LICENSE_AGREEMENT_TAB.GENERAL}
				title={i18n('General')}>
				<div className='license-agreement-form-row'>
					<div className='license-agreement-form-col'>
						<ValidationInput
							onChange={name => onDataChanged({name})}
							label={i18n('Name')}
							value={name}
							name='license-agreement-name'
							validations={{maxLength: 25, required: true}}
							type='text'/>
						<ValidationInput
							onChange={requirementsAndConstrains => onDataChanged({requirementsAndConstrains})}
							label={i18n('Requirements and Constraints')}
							value={requirementsAndConstrains}
							name='license-agreement-requirements-and-constraints'
							validations={{maxLength: 1000}}
							type='textarea'/>
					</div>
					<ValidationInput
						onChange={description => onDataChanged({description})}
						label={i18n('Description')}
						value={description}
						name='license-agreement-description'
						validations={{maxLength: 1000, required: true}}
						type='textarea'/>
				</div>
				<div className='license-agreement-form-row'>
					<ValidationInput
						onEnumChange={licenseTerm => onDataChanged({licenseTerm:{choice: licenseTerm, other: ''}})}
						selectedEnum={licenseTerm && licenseTerm.choice}
						validations={{required: true}}
						type='select'
						label={i18n('License Term')}
						values={LicenseAgreementOptionsInputValues.LICENSE_MODEL_TYPE}/>
				</div>
			</ValidationTab>
		);
	}

	renderFeatureGroupsTab() {
		let {onFeatureGroupsButtonTabSelect, selectedFeatureGroupsButtonTab, featureGroupsList} = this.props;
		if (featureGroupsList.length > 0) {
			return (
				<ValidationTab
					eventKey={LicenseAgreementEnums.SELECTED_LICENSE_AGREEMENT_TAB.FEATURE_GROUPS}
					title={i18n('Feature Groups')}>
					<ButtonGroup>
						{
							this.renderFeatureGroupsButtonTab(
								LicenseAgreementEnums.SELECTED_FEATURE_GROUPS_BUTTONTAB.ASSOCIATED_FEATURE_GROUPS,
								selectedFeatureGroupsButtonTab,
								i18n('Associated Feature Groups'),
								onFeatureGroupsButtonTabSelect
							)
						}
						{
							this.renderFeatureGroupsButtonTab(
								LicenseAgreementEnums.SELECTED_FEATURE_GROUPS_BUTTONTAB.AVAILABLE_FEATURE_GROUPS,
								selectedFeatureGroupsButtonTab,
								i18n('Available Feature Groups'),
								onFeatureGroupsButtonTabSelect
							)
						}
					</ButtonGroup>
					{this.renderFeatureGroupsButtonTabContent(selectedFeatureGroupsButtonTab)}
				</ValidationTab>
			);
		} else {
			return (
				<ValidationTab
					eventKey={LicenseAgreementEnums.SELECTED_LICENSE_AGREEMENT_TAB.FEATURE_GROUPS}
					title={i18n('Feature Groups')}>
					<p>{i18n('There is no available feature groups')}</p>
				</ValidationTab>
			);
		}
	}

	renderFeatureGroupsButtonTabContent(selectedFeatureGroupsButtonTab) {
		const {featureGroupsList = [], data: {featureGroupsIds = []}} = this.props;
		const {localFeatureGroupsListFilter} = this.state;
		let selectedFeatureGroups = featureGroupsIds.map(featureGroupId => featureGroupsList.find(featureGroup => featureGroup.id === featureGroupId));

		const dualBoxFilterTitle = {
			left: i18n('Available Feature Groups'),
			right: i18n('Selected Feature Groups')
		};

		switch (selectedFeatureGroupsButtonTab) {
			case LicenseAgreementEnums.SELECTED_FEATURE_GROUPS_BUTTONTAB.ASSOCIATED_FEATURE_GROUPS:
				if (!selectedFeatureGroups.length) {
					return (
						<div className='no-items-msg'>
							{i18n('There are currently no feature groups associated with this license agreement. Click "Available Feature Groups" to associate.')}
						</div>
					);
				}
				if (featureGroupsList.length) {
					return (
						<ListEditorView
							className='thinner-list'
							filterValue={localFeatureGroupsListFilter}
							onFilter={localFeatureGroupsListFilter => this.setState({localFeatureGroupsListFilter})}>
							{this.filterAssociatedFeatureGroupsList(selectedFeatureGroups).map(featureGroup => this.renderAssociatedFeatureGroupListItem(featureGroup))}
						</ListEditorView>
					);
				}
				return;
			case LicenseAgreementEnums.SELECTED_FEATURE_GROUPS_BUTTONTAB.AVAILABLE_FEATURE_GROUPS:
				return (
					<DualListboxView
						filterTitle={dualBoxFilterTitle}
						selectedValuesList={this.props.data.featureGroupsIds}
						availableList={this.props.featureGroupsList}
						onChange={ selectedValuesList => this.props.onDataChanged( { featureGroupsIds: selectedValuesList } )}/>
				);
		}
	}

	renderFeatureGroupsButtonTab(buttonTab, selectedButtonTab, title, onClick) {
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

	renderAssociatedFeatureGroupListItem({id, name, entitlementPoolsIds = [], licenseKeyGroupsIds = []}) {
		const {onDataChanged, data: {featureGroupsIds}, isReadOnlyMode} = this.props;
		return (
			<ListEditorViewItem
				key={id}
				onDelete={() => onDataChanged({featureGroupsIds: featureGroupsIds.filter(featureGroupId => featureGroupId !== id)})}
				isReadOnlyMode={isReadOnlyMode}>
				<div className='name'>{name}</div>
				<div className='inner-objects-count'>{
					i18n(
						'Entitlement Pools({entitlementPoolsCounter}), License Key Groups({licenseKeyGroupsCount})',
						{
							entitlementPoolsCounter: entitlementPoolsIds.length,
							licenseKeyGroupsCount: licenseKeyGroupsIds.length
						}
					)
				}</div>
			</ListEditorViewItem>
		);
	}

	filterAssociatedFeatureGroupsList(featureGroupsList) {
		let {localFeatureGroupsListFilter} = this.state;
		if (localFeatureGroupsListFilter) {
			const filter = new RegExp(escape(localFeatureGroupsListFilter), 'i');
			return featureGroupsList.filter(({name}) => name.match(filter));
		}
		else {
			return featureGroupsList;
		}
	}
}

export default LicenseAgreementEditorView;

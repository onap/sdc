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

import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {TabsForm as Form} from 'nfvo-components/input/validation/Form.jsx';
import Tabs from 'nfvo-components/input/validation/Tabs.jsx';
import Tab from 'react-bootstrap/lib/Tab.js';
import Input from 'nfvo-components/input/validation/Input.jsx';
import DualListboxView from 'nfvo-components/input/dualListbox/DualListboxView.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Validator from 'nfvo-utils/Validator.js';

import {enums as LicenseAgreementEnums, optionsInputValues as LicenseAgreementOptionsInputValues, LA_EDITOR_FORM} from './LicenseAgreementConstants.js';

const dualBoxFilterTitle = {
	left: i18n('Available Feature Groups'),
	right: i18n('Selected Feature Groups')
};

const LicenseAgreementPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	requirementsAndConstrains: React.PropTypes.string,
	licenseTerm: React.PropTypes.object,
	featureGroupsIds: React.PropTypes.arrayOf(React.PropTypes.string),
	version: React.PropTypes.object
});


const GeneralTabContent = ({data, genericFieldInfo, onDataChanged, validateName, validateLTChoice}) => {
	let {name, description, requirementsAndConstrains, licenseTerm} = data;
	return (
		<GridSection>
			<GridItem colSpan={2}>
				<Input
					isValid={genericFieldInfo.name.isValid}
					errorText={genericFieldInfo.name.errorText}
					onChange={name => onDataChanged({name}, LA_EDITOR_FORM, { name: validateName })}
					label={i18n('Name')}
					value={name}
					data-test-id='create-la-name'
					name='license-agreement-name'
					isRequired={true}
					type='text'/>
				<Input
					isValid={genericFieldInfo.requirementsAndConstrains.isValid}
					errorText={genericFieldInfo.requirementsAndConstrains.errorText}
					onChange={requirementsAndConstrains => onDataChanged({requirementsAndConstrains}, LA_EDITOR_FORM)}
					label={i18n('Requirements and Constraints')}
					value={requirementsAndConstrains}
					data-test-id='create-la-requirements-constants'
					name='license-agreement-requirements-and-constraints'
					type='textarea'/>
				<Input
					label={i18n('License Term')}
					type='select'
					value={licenseTerm && licenseTerm.choice}
					isRequired={true}
					onChange={e => {
						const selectedIndex = e.target.selectedIndex;
						const licenseTerm = e.target.options[selectedIndex].value;
						onDataChanged({licenseTerm:{choice: licenseTerm, other: ''}}, LA_EDITOR_FORM, { licenseTerm: validateLTChoice });
					}}
					isValid={genericFieldInfo.licenseTerm.isValid}
					errorText={genericFieldInfo.licenseTerm.errorText}
					className='input-options-select'
					groupClassName='bootstrap-input-options'
					data-test-id='create-la-license-term' >
					{LicenseAgreementOptionsInputValues.LICENSE_MODEL_TYPE.map(mtype =>
						<option key={mtype.enum} value={mtype.enum}>{`${mtype.title}`}</option>)}
				</Input>
			</GridItem>
			<GridItem colSpan={2} stretch>
				<Input
					isValid={genericFieldInfo.description.isValid}
					errorText={genericFieldInfo.description.errorText}
					onChange={description => onDataChanged({description}, LA_EDITOR_FORM)}
					label={i18n('Description')}
					value={description}
					overlayPos='bottom'
					data-test-id='create-la-description'
					name='license-agreement-description'
					isRequired={true}
					type='textarea'/>
			</GridItem>
		</GridSection>
	);
};

class LicenseAgreementEditorView extends React.Component {

	static propTypes = {
		data: LicenseAgreementPropType,
		previousData: LicenseAgreementPropType,
		LANames: React.PropTypes.object,
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
		data: {}
	};

	state = {
		localFeatureGroupsListFilter: ''
	};

	render() {
		let {selectedTab, onTabSelect, isReadOnlyMode, featureGroupsList, data, onDataChanged, genericFieldInfo} = this.props;
		return (
			<div>
				{genericFieldInfo && <Form
					ref='validationForm'
					hasButtons={true}
					onSubmit={ () => this.submit() }
					onReset={ () => this.props.onCancel() }
					labledButtons={true}
					isReadOnlyMode={isReadOnlyMode}
					isValid={this.props.isFormValid}
					formReady={this.props.formReady}
					onValidateForm={() => this.props.onValidateForm(LA_EDITOR_FORM) }
					className='license-agreement-form'>
					<Tabs activeKey={onTabSelect ? selectedTab : undefined} onSelect={onTabSelect} invalidTabs={this.props.invalidTabs} >
						<Tab
							eventKey={LicenseAgreementEnums.SELECTED_LICENSE_AGREEMENT_TAB.GENERAL}
							data-test-id='general-tab'
							title={i18n('General')}>
								<fieldset disabled={isReadOnlyMode}>
									<GeneralTabContent data={data} genericFieldInfo={genericFieldInfo} onDataChanged={onDataChanged} validateLTChoice={(value)=>this.validateLTChoice(value)}
										   validateName={(value)=>this.validateName(value)}/>
								</fieldset>
						</Tab>
						<Tab
							eventKey={LicenseAgreementEnums.SELECTED_LICENSE_AGREEMENT_TAB.FEATURE_GROUPS}
							data-test-id='feature-group-tab'
							title={i18n('Feature Groups')}>
								<fieldset disabled={isReadOnlyMode}>
							{featureGroupsList.length > 0 ?
									<DualListboxView
										isReadOnlyMode={isReadOnlyMode}
										filterTitle={dualBoxFilterTitle}
										selectedValuesList={data.featureGroupsIds}
										availableList={featureGroupsList}
										onChange={ selectedValuesList => onDataChanged( { featureGroupsIds: selectedValuesList }, LA_EDITOR_FORM )}/> :
									<p>{i18n('There is no available feature groups')}</p>}
								</fieldset>
						</Tab>
					</Tabs>
				</Form>}
			</div>
		);
	}

	submit() {
		const {data: licenseAgreement, previousData: previousLicenseAgreement} = this.props;
		this.props.onSubmit({licenseAgreement, previousLicenseAgreement});
	}

	validateLTChoice(value) {
		if (!value.choice) {
			return {isValid: false, errorText: i18n('Field is required')};
		}
		return {isValid: true, errorText: ''};
	}

	validateName(value) {
		const {data: {id}, LANames} = this.props;
		const isExists = Validator.isItemNameAlreadyExistsInList({itemId: id, itemName: value, list: LANames});

		return !isExists ?  {isValid: true, errorText: ''} :
			{isValid: false, errorText: i18n('License Agreement by the name \'' + value + '\' already exists. License agreement name must be unique')};
	}
}

export default LicenseAgreementEditorView;

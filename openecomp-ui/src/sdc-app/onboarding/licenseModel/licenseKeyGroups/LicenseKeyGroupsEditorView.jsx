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
import Validator from 'nfvo-utils/Validator.js';

import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {optionsInputValues as licenseKeyGroupOptionsInputValues, LKG_FORM_NAME} from './LicenseKeyGroupsConstants.js';
import {other as optionInputOther} from 'nfvo-components/input/validation/InputOptions.jsx';
import InputOptions from 'nfvo-components/input/validation/InputOptions.jsx';

const LicenseKeyGroupPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	operationalScope: React.PropTypes.shape({
		choices: React.PropTypes.array,
		other: React.PropTypes.string
	}),
	type: React.PropTypes.string
});

const LicenseKeyGroupFormContent = ({data, onDataChanged, genericFieldInfo, validateName, validateOperationalScope}) => {
	let {name, description, operationalScope, type} = data;
	return (
		<GridSection>
			<GridItem colSpan={2}>
				<Input
					onChange={name => onDataChanged({name}, LKG_FORM_NAME, {name: validateName})}
					label={i18n('Name')}
					data-test-id='create-lkg-name'
					value={name}
					isValid={genericFieldInfo.name.isValid}
					errorText={genericFieldInfo.name.errorText}
					isRequired={true}
					type='text'/>
			</GridItem>
			<GridItem colSpan={2}>
				<InputOptions
					onInputChange={()=>{}}
					isMultiSelect={true}
					isRequired={true}
					onEnumChange={operationalScope => onDataChanged({operationalScope:{choices: operationalScope, other: ''}},
						LKG_FORM_NAME, {operationalScope: validateOperationalScope})}
					onOtherChange={operationalScope => onDataChanged({operationalScope:{choices: [optionInputOther.OTHER],
						other: operationalScope}}, LKG_FORM_NAME, {operationalScope: validateOperationalScope})}
					label={i18n('Operational Scope')}
					data-test-id='create-lkg-operational-scope'
					type='select'
					multiSelectedEnum={operationalScope && operationalScope.choices}
					otherValue={operationalScope && operationalScope.other}
					values={licenseKeyGroupOptionsInputValues.OPERATIONAL_SCOPE}
					isValid={genericFieldInfo.operationalScope.isValid}
					errorText={genericFieldInfo.operationalScope.errorText} />
			</GridItem>
			<GridItem colSpan={2}>
				<Input
					onChange={description => onDataChanged({description}, LKG_FORM_NAME)}
					label={i18n('Description')}
					data-test-id='create-lkg-description'
					value={description}
					isValid={genericFieldInfo.description.isValid}
					errorText={genericFieldInfo.description.errorText}
					isRequired={true}
					type='textarea'
					overlayPos='bottom' />
			</GridItem>
			<GridItem colSpan={2}>
				<Input
					isRequired={true}
					onChange={e => { const selectedIndex = e.target.selectedIndex;
						const val = e.target.options[selectedIndex].value;
						onDataChanged({type: val}, LKG_FORM_NAME);}}
					value={type}
					label={i18n('Type')}
					data-test-id='create-lkg-type'
					isValid={genericFieldInfo.type.isValid}
					errorText={genericFieldInfo.type.errorText}
					groupClassName='bootstrap-input-options'
					className='input-options-select'
					type='select' >
					{
						licenseKeyGroupOptionsInputValues.TYPE.map(type =>
						(<option key={type.enum} value={type.enum}>{type.title}</option>))
					}
				</Input>
			</GridItem>
		</GridSection>
	);
};

class LicenseKeyGroupsEditorView extends React.Component {
	static propTypes = {
		data: LicenseKeyGroupPropType,
		previousData: LicenseKeyGroupPropType,
		LKGNames: React.PropTypes.object,
		isReadOnlyMode: React.PropTypes.bool,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired
	};

	static defaultProps = {
		data: {}
	};

	render() {
		let {data = {}, onDataChanged, isReadOnlyMode, genericFieldInfo} = this.props;
		return (
			<div>
		{ genericFieldInfo &&
			<Form
				ref='validationForm'
				hasButtons={true}
				onSubmit={ () => this.submit() }
				onReset={ () => this.props.onCancel() }
				isValid={this.props.isFormValid}
				formReady={this.props.formReady}
				onValidateForm={() => this.props.onValidateForm(LKG_FORM_NAME) }
				labledButtons={true}
				isReadOnlyMode={isReadOnlyMode}
				className='license-key-groups-form'>
				<LicenseKeyGroupFormContent
					data={data}
					onDataChanged={onDataChanged}
					genericFieldInfo={genericFieldInfo}
					validateName={(value)=> this.validateName(value)}
					validateOperationalScope={this.validateOperationalScope}/>
			</Form>}
			</div>
		);
	}

	submit() {
		const {data: licenseKeyGroup, previousData: previousLicenseKeyGroup} = this.props;
		this.props.onSubmit({licenseKeyGroup, previousLicenseKeyGroup});
	}

	validateName(value) {
		const {data: {id}, LKGNames} = this.props;
		const isExists = Validator.isItemNameAlreadyExistsInList({itemId: id, itemName: value, list: LKGNames});

		return !isExists ?  {isValid: true, errorText: ''} :
			{isValid: false, errorText: i18n('License key group by the name \'' + value + '\' already exists. License key group name must be unique')};
	}

	validateOperationalScope(value) {
		if (value && value.choices && value.choices.length > 0) {
			if (value.choices[0] !== optionInputOther.OTHER)
			{
				return {
					isValid: true,
					errorText: ''
				};
			} else {
				if ( value.other ) {
					return {
						isValid: true,
						errorText: ''
					};
				}
			}
		}
		return {
			isValid: false,
			errorText: 'Field is required'
		};
	}
}

export default LicenseKeyGroupsEditorView;

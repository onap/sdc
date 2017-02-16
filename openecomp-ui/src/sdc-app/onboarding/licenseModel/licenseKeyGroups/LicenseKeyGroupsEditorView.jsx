import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';

import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';
import {optionsInputValues as licenseKeyGroupOptionsInputValues} from './LicenseKeyGroupsConstants.js';
import {other as optionInputOther} from 'nfvo-components/input/inputOptions/InputOptions.jsx';

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

class LicenseKeyGroupsEditorView extends React.Component {
	static propTypes = {
		data: LicenseKeyGroupPropType,
		previousData: LicenseKeyGroupPropType,
		isReadOnlyMode: React.PropTypes.bool,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired
	};

	static defaultProps = {
		data: {}
	};

	render() {
		let {data = {}, onDataChanged, isReadOnlyMode} = this.props;
		let {name, description, operationalScope, type} = data;
		return (
			<ValidationForm
				ref='validationForm'
				hasButtons={true}
				onSubmit={ () => this.submit() }
				onReset={ () => this.props.onCancel() }
				labledButtons={true}
				isReadOnlyMode={isReadOnlyMode}
				className='license-key-groups-form'>
				<div className='license-key-groups-form-row'>
					<ValidationInput
						onChange={name => onDataChanged({name})}
						ref='name'
						label={i18n('Name')}
						value={name}
						validations={{maxLength: 120, required: true}}
						type='text'/>
					<ValidationInput
						isMultiSelect={true}
						isRequired={true}
						onEnumChange={operationalScope => onDataChanged({operationalScope:{choices: operationalScope, other: ''}})}
						onOtherChange={operationalScope => onDataChanged({operationalScope:{choices: [optionInputOther.OTHER], other: operationalScope}})}
						label={i18n('Operational Scope')}
						validations={{required: true}}
						multiSelectedEnum={operationalScope && operationalScope.choices}
						otherValue={operationalScope && operationalScope.other}
						values={licenseKeyGroupOptionsInputValues.OPERATIONAL_SCOPE}/>
				</div>
				<div className='license-key-groups-form-row'>
					<ValidationInput
						onChange={description => onDataChanged({description})}
						ref='description'
						label={i18n('Description')}
						value={description}
						validations={{maxLength: 1000, required: true}}
						type='textarea'/>
						<ValidationInput
							isRequired={true}
							onEnumChange={type => onDataChanged({type})}
							selectedEnum={type}
							label={i18n('Type')}
							type='select'
							validations={{required: true}}
							values={licenseKeyGroupOptionsInputValues.TYPE}/>
					</div>
			</ValidationForm>
		);
	}

	submit() {
		const {data: licenseKeyGroup, previousData: previousLicenseKeyGroup} = this.props;
		this.props.onSubmit({licenseKeyGroup, previousLicenseKeyGroup});
	}
}

export default LicenseKeyGroupsEditorView;

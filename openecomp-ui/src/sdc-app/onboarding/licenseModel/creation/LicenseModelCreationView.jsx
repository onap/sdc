import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';
import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';

const LicenseModelPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	vendorName: React.PropTypes.string,
	description: React.PropTypes.string
});

class LicenseModelCreationView extends React.Component {

	static propTypes = {
		data: LicenseModelPropType,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired
	};

	render() {
		let {data = {}, onDataChanged} = this.props;
		let {vendorName, description} = data;
		return (
			<div>
				<ValidationForm
					ref='validationForm'
					hasButtons={true}
					onSubmit={ () => this.submit() }
					onReset={ () => this.props.onCancel() }
					labledButtons={true}>
					<ValidationInput
						value={vendorName}
						label={i18n('Vendor Name')}
						ref='vendor-name'
						onChange={vendorName => onDataChanged({vendorName})}
						validations={{maxLength: 25, required: true}}
						type='text'
						className='field-section'/>
					<ValidationInput
						value={description}
						label={i18n('Description')}
						ref='description'
						onChange={description => onDataChanged({description})}
						validations={{maxLength: 1000, required: true}}
						type='textarea'
						className='field-section'/>
				</ValidationForm>
			</div>
		);
	}


	submit() {
		const {data:licenseModel} = this.props;
		this.props.onSubmit(licenseModel);
	}
}

export default LicenseModelCreationView;

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
import Input from 'nfvo-components/input/validation/Input.jsx';
import Form from 'nfvo-components/input/validation/Form.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';

const NICPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	networkId: React.PropTypes.string
});

class NICCreationView extends React.Component {

	static propTypes = {
		data: NICPropType,
		onDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired
	};

	render() {
		let {data = {}, onDataChanged, genericFieldInfo, isFormValid, onValidateForm, formReady} = this.props;
		let {name, description, networkDescription} = data;
		return (
			<div>
			{genericFieldInfo && <Form
				ref={(form) => this.form = form}
				hasButtons={true}
				onSubmit={ () => this.submit() }
				submitButtonText={data.id ? i18n('Save') : i18n('Create')}
				onReset={ () => this.props.onCancel() }
				labledButtons={true}
				isValid={isFormValid}
				onValidateForm={() => onValidateForm()}
				formReady={formReady} >
				<GridSection>
					<GridItem colSpan={4}>
						<Input
							value={name}
							label={i18n('Name')}
							data-test-id='nic-name'
							onChange={name => onDataChanged({name})}
							isRequired={true}
							type='text'
							isValid={genericFieldInfo['name'].isValid}
							errorText={genericFieldInfo['name'].errorText}
							className='field-section'/>
						<Input
							value={description}
							label={i18n('Description')}
							data-test-id='nic-description'
							onChange={description => onDataChanged({description})}
							isValid={genericFieldInfo['description'].isValid}
							errorText={genericFieldInfo['description'].errorText}
							type='textarea'
							className='field-section'/>
					</GridItem>
				</GridSection>
				<GridSection title={i18n('Network')}>
					<GridItem colSpan={2}>
						<div className='form-group'>
							<label className='control-label'>{i18n('Network Type')}</label>
							<div className='network-type-radio'>
								<Input
									label={i18n('Internal')}
									disabled
									checked={false}
									data-test-id='nic-internal'
									className='network-radio disabled'
									type='radio'/>
								<Input
									label={i18n('External')}
									disabled
									checked={true}
									data-test-id='nic-external'
									className='network-radio disabled'
									type='radio'/>
							</div>
						</div>
					</GridItem>
					<GridItem colSpan={2}>
						<Input
							value={networkDescription}
							label={i18n('Network Description')}
							data-test-id='nic-network-description'
							onChange={networkDescription => onDataChanged({networkDescription})}
							isValid={genericFieldInfo['networkDescription'].isValid}
							errorText={genericFieldInfo['networkDescription'].errorText}
							type='text'
							className='field-section'/>
					</GridItem>
				</GridSection>
			</Form>}
			</div>
		);
	}


	submit() {
		const {data: nic, softwareProductId, componentId, currentSoftwareProduct} = this.props;
		this.props.onSubmit({nic, softwareProductId, componentId, version: currentSoftwareProduct.version});
	}
}

export default NICCreationView;

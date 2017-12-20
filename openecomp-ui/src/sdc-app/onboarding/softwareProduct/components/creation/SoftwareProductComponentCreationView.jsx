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

import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import {forms} from '../SoftwareProductComponentsConstants.js';

class ComponentCreationView extends React.Component {
	render() {
		let {data = {}, onDataChanged, onCancel, genericFieldInfo} = this.props;
		let {displayName, description} = data;
		return(
			<div>
				{
					genericFieldInfo && <Form
						ref='validationForm'
						hasButtons={true}
						onSubmit={ () => this.submit() }
						onReset={ () => onCancel() }
						submitButtonText={i18n('Create')}
						labledButtons={true}
						isValid={this.props.isFormValid}
						formReady={this.props.formReady}
						onValidateForm={() => this.props.onValidateForm(forms.CREATE_FORM) }
						className='entitlement-pools-form'>
						<GridSection hasLastColSet>
							<GridItem colSpan={4} lastColInRow>
								<Input
									data-test-id='name'
									onChange={displayName => onDataChanged({displayName})}
									label={i18n('Name')}
									isRequired={true}
									isValid={genericFieldInfo.displayName.isValid}
									errorText={genericFieldInfo.displayName.errorText}
									value={displayName}
									type='text'/>
							</GridItem>
							<GridItem colSpan={4} lastColInRow>
								<Input
									label={i18n('Description')}
									onChange={description => onDataChanged({description})}
									value={description}
									isValid={genericFieldInfo.description.isValid}
									errorText={genericFieldInfo.description.errorText}
									data-test-id='description'
									type='textarea'/>
							</GridItem>
						</GridSection>
					</Form>
				}
			</div>
		);
	}

	submit() {
		const {onSubmit, data} = this.props;
		onSubmit(data);
	}
}

export default ComponentCreationView;

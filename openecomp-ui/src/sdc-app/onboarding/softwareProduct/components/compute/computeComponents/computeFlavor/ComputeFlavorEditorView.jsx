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
import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';
import VmSizing from './VmSizing.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';

class ComputeEditorView extends React.Component {

	static propTypes = {
		data: React.PropTypes.object,
		qdata: React.PropTypes.object,
		qschema: React.PropTypes.object,
		isReadOnlyMode: React.PropTypes.bool,
		isManual: React.PropTypes.bool,
		onDataChanged: React.PropTypes.func.isRequired,
		onQDataChanged: React.PropTypes.func.isRequired,
		onSubmit: React.PropTypes.func.isRequired,
		onCancel: React.PropTypes.func.isRequired
	};

	render() {
		let {data = {}, qdata = {}, qgenericFieldInfo, dataMap, genericFieldInfo, isReadOnlyMode, isManual, isFormValid, formReady,
			onDataChanged, onQDataChanged, onSubmit, onCancel, onValidateForm} = this.props;
		const {id, name, description} = data;
		const edittingComputeMode = Boolean(id);

		return (
			<div className='vsp-component-computeFlavor-view'>
				{genericFieldInfo && <Form
					ref={(form) => {
						this.form = form;
					}}
					hasButtons={true}
					onSubmit={ () => onSubmit({data, qdata}) }
					onReset={ () => onCancel() }
					labledButtons={true}
					isReadOnlyMode={isReadOnlyMode}
					isValid={isFormValid}
					formReady={formReady}
					onValidateForm={() => onValidateForm() }
					className='component-questionnaire-validation-form'
					submitButtonText={edittingComputeMode ? i18n('Save') : i18n('Create')}>
					<GridSection>
						<GridItem colSpan={edittingComputeMode ? 2 : 4}>
							<Input
								disabled={!isManual}
								data-test-id='name'
								type='text'
								label={i18n('Flavor Name')}
								value={name}
								onChange={name => onDataChanged({name})}
								isValid={genericFieldInfo['name'].isValid}
								errorText={genericFieldInfo['name'].errorText}
								isRequired/>
							</GridItem>
							<GridItem colSpan={edittingComputeMode ? 2 : 4}>
							<Input
								data-test-id='description'
								type='textarea'
								label={i18n('Description')}
								value={description}
								onChange={description => onDataChanged({description})}
								isValid={genericFieldInfo['description'].isValid}
								errorText={genericFieldInfo['description'].errorText}/>
						</GridItem>
					</GridSection>
					{edittingComputeMode && <VmSizing qgenericFieldInfo={qgenericFieldInfo} dataMap={dataMap} onQDataChanged={onQDataChanged}/>}
				</Form>
				}
			</div>
		);
	}

	save(){
		return this.form.handleFormSubmit(new Event('dummy'));
	}
}

export default ComputeEditorView;

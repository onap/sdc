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
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';

import DraggableUploadFileBox from 'nfvo-components/fileupload/DraggableUploadFileBox.jsx';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';

const SoftwareProductProcessEditorPropType = React.PropTypes.shape({
	id: PropTypes.string,
	name: PropTypes.string,
	description: PropTypes.string,
	artifactName: PropTypes.string,
	type: PropTypes.string
});



class SoftwareProductProcessesEditorForm extends React.Component {


	static propTypes = {
		data: SoftwareProductProcessEditorPropType,
		previousData: SoftwareProductProcessEditorPropType,
		isReadOnlyMode: React.PropTypes.bool,
		onDataChanged: React.PropTypes.func,
		onSubmit: React.PropTypes.func,
		onCancel: React.PropTypes.func
	};
	state = {
		dragging: false,
		files: []
	};

	render() {
		let {data = {}, isReadOnlyMode, onDataChanged, onCancel, genericFieldInfo, optionsInputValues} = this.props;
		let {name, description, artifactName, type} = data;

		return (
			<div>
				{genericFieldInfo && <Form
					ref='validationForm'
					hasButtons={true}
					labledButtons={true}
					isReadOnlyMode={isReadOnlyMode}
					onSubmit={ () => this.submit() }
					onReset={ () => onCancel() }
					isValid={this.props.isFormValid}
					formReady={this.props.formReady}
					onValidateForm={() => this.props.onValidateForm() }
					className='vsp-processes-editor'>
					<div className={`vsp-processes-editor-data${isReadOnlyMode ? ' disabled' : '' }`}>
						<Dropzone
							className={`vsp-process-dropzone-view ${this.state.dragging ? 'active-dragging' : ''}`}
							onDrop={(acceptedFiles, rejectedFiles) => this.handleImportSubmit(acceptedFiles, rejectedFiles)}
							onDragEnter={() => this.setState({dragging: true})}
							onDragLeave={() => this.setState({dragging: false})}
							multiple={false}
							disableClick={true}
							ref='processEditorFileInput'
							name='processEditorFileInput'>
							<GridSection hasLastColSet={true}>
								<GridItem colSpan={2}>
									<Input
										onChange={name => onDataChanged({name})}
										isValid={genericFieldInfo.name.isValid}
										isRequired={true}
										data-test-id='name'
										errorText={genericFieldInfo.name.errorText}
										label={i18n('Name')}
										value={name}
										type='text'/>
								</GridItem>
								<GridItem colSpan={2} lastColInRow={true}>
									<label>&nbsp;</label>
									<DraggableUploadFileBox className='process-editor-file-box' isReadOnlyMode={isReadOnlyMode} onClick={() => this.refs.processEditorFileInput.open()}/>
								</GridItem>
							</GridSection>
							<GridSection hasLastColSet={true}>
								<GridItem colSpan={2}>
									<Input
										name='vsp-process-description'
										groupClassName='vsp-process-description'
										onChange={description => onDataChanged({description})}
										isValid={genericFieldInfo.description.isValid}
										errorText={genericFieldInfo.description.errorText}
										label={i18n('Notes')}
										value={description}
										data-test-id='vsp-process-description'
										type='textarea'/>
								</GridItem>
								<GridItem colSpan={2} lastColInRow={true}>
									<Input
										label={i18n('Artifacts')}
										value={artifactName}
										type='text'
										disabled/>
									<Input
										onChange={e => {
											// setting the unit to the correct value
											const selectedIndex = e.target.selectedIndex;
											const val = e.target.options[selectedIndex].value;
											onDataChanged({type: val});}
										}
										value={type}
										label={i18n('Process Type')}
										className='process-type'
										data-test-id='process-type'
										isValid={genericFieldInfo.type.isValid}
										errorText={genericFieldInfo.type.errorText}
										type='select'>
										{optionsInputValues.PROCESS_TYPE.map(mtype =>
											<option key={mtype.enum} value={mtype.enum}>{`${mtype.title}`}</option>)}
									</Input>
								</GridItem>
							</GridSection>
						</Dropzone>
					</div>
				</Form>}
			</div>
		);
	}

	submit() {
		const {data: process, previousData: previousProcess} = this.props;
		let {files} = this.state;
		let formData = false;
		if (files.length) {
			let file = files[0];
			formData = new FormData();
			formData.append('upload', file);
		}

		let updatedProcess = {
			...process,
			formData
		};
		this.props.onSubmit({process: updatedProcess, previousProcess});
	}


	handleImportSubmit(files, rejectedFiles) {
		if (files.length > 0) {
			let {onDataChanged} = this.props;
			this.setState({
				dragging: false,
				complete: '0',
				files
			});
			onDataChanged({artifactName: files[0].name});
		}
		else if (rejectedFiles.length > 0) {
			this.setState({
				dragging: false
			});
			if (DEBUG) {
				console.log('file was rejected.' + rejectedFiles[0].name);
			}
		}
	}
}

export default SoftwareProductProcessesEditorForm;

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
import Dropzone from 'react-dropzone';
import classnames from 'classnames';

import i18n from 'nfvo-utils/i18n/i18n.js';
import  {optionsInputValues as ProcessesOptionsInputValues} from './SoftwareProductProcessesConstants.js';
import Form from 'nfvo-components/input/validation/Form.jsx';
import Input from 'nfvo-components/input/validation/Input.jsx';
import GridSection from 'nfvo-components/grid/GridSection.jsx';
import GridItem from 'nfvo-components/grid/GridItem.jsx';

const SoftwareProductProcessEditorPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	artifactName: React.PropTypes.string,
	type: React.PropTypes.string
});

const FileUploadBox = ({onClick}) => {
	return (
		<div className='file-upload-box'>
			<div className='drag-text'>{i18n('Drag & drop for upload')}</div>
			<div className='or-text'>{i18n('or')}</div>
			<div className='upload-btn primary-btn' onClick={onClick}>
				<span className='primary-btn-text'>{i18n('Select file')}</span>
			</div>
		</div>
	);
};


class SoftwareProductProcessesEditorView extends React.Component {

	state = {
		dragging: false,
		files: []
	};

	static propTypes = {
		data: SoftwareProductProcessEditorPropType,
		previousData: SoftwareProductProcessEditorPropType,
		isReadOnlyMode: React.PropTypes.bool,
		onDataChanged: React.PropTypes.func,
		onSubmit: React.PropTypes.func,
		onClose: React.PropTypes.func
	};

	render() {
		let {data = {}, isReadOnlyMode, onDataChanged, onClose, genericFieldInfo} = this.props;
		let {name, description, artifactName, type} = data;

		return (
			<div>
				{genericFieldInfo && <Form
					ref='validationForm'
					hasButtons={true}
					labledButtons={true}
					isReadOnlyMode={isReadOnlyMode}
					onSubmit={ () => this.submit() }
					onReset={ () => onClose() }
					isValid={this.props.isFormValid}
					formReady={this.props.formReady}
					onValidateForm={() => this.props.onValidateForm() }
					className='vsp-processes-editor'>
					<div className={classnames('vsp-processes-editor-data', {'disabled': isReadOnlyMode})}>
						<Dropzone
							className={classnames('vsp-process-dropzone-view', {'active-dragging': this.state.dragging})}
							onDrop={(acceptedFiles, rejectedFiles) => this.handleImportSubmit(acceptedFiles, rejectedFiles)}
							onDragEnter={() => this.setState({dragging: true})}
							onDragLeave={() => this.setState({dragging: false})}
							multiple={false}
							disableClick={true}
							ref='processEditorFileInput'
							name='processEditorFileInput'>
							<GridSection>
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
								<GridItem colSpan={2}>
									<FileUploadBox onClick={() => this.refs.processEditorFileInput.open()}/>
								</GridItem>
							</GridSection>
							<GridSection>
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
								<GridItem colSpan={2}>
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
										data-test-id='process-type'
										isValid={genericFieldInfo.type.isValid}
										errorText={genericFieldInfo.type.errorText}
										type='select'>
										{ProcessesOptionsInputValues.PROCESS_TYPE.map(mtype =>
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

export default SoftwareProductProcessesEditorView;

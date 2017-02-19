import React from 'react';
import Dropzone from 'react-dropzone';
import classnames from 'classnames';

import i18n from 'nfvo-utils/i18n/i18n.js';
import ValidationForm from 'nfvo-components/input/validation/ValidationForm.jsx';
import ValidationInput from 'nfvo-components/input/validation/ValidationInput.jsx';

const SoftwareProductProcessEditorPropType = React.PropTypes.shape({
	id: React.PropTypes.string,
	name: React.PropTypes.string,
	description: React.PropTypes.string,
	artifactName: React.PropTypes.string
});

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
		let {data = {}, isReadOnlyMode, onDataChanged, onClose} = this.props;
		let {name, description, artifactName} = data;
		return (
			<ValidationForm
				ref='validationForm'
				hasButtons={true}
				labledButtons={true}
				isReadOnlyMode={isReadOnlyMode}
				onSubmit={ () => this.submit() }
				onReset={ () => onClose() }
				className='vsp-processes-editor'>
				<div className={classnames('vsp-processes-editor-data', {'disabled': isReadOnlyMode})}>
					<Dropzone
						className={classnames('vsp-process-dropzone-view', {'active-dragging': this.state.dragging})}
						onDrop={files => this.handleImportSubmit(files)}
						onDragEnter={() => this.setState({dragging: true})}
						onDragLeave={() => this.setState({dragging: false})}
						multiple={false}
						disableClick={true}
						ref='processEditorFileInput'
						name='processEditorFileInput'
						accept='*.*'>
						<div className='row'>
							<div className='col-md-6'>
								<ValidationInput
									onChange={name => onDataChanged({name})}
									label={i18n('Name')}
									value={name}
									validations={{validateName: true, maxLength: 120, required: true}}
									type='text'/>
								<ValidationInput
									label={i18n('Artifacts')}
									value={artifactName}
									type='text'
									disabled/>
							</div>
							<div className='col-md-6'>
								<div className='file-upload-box'>
									<div className='drag-text'>{i18n('Drag & drop for upload')}</div>
									<div className='or-text'>{i18n('or')}</div>
									<div className='upload-btn primary-btn' onClick={() => this.refs.processEditorFileInput.open()}>
										<span className='primary-btn-text'>{i18n('Select file')}</span>
									</div>
								</div>
							</div>
						</div>
						<ValidationInput
							onChange={description => onDataChanged({description})}
							label={i18n('Notes')}
							value={description}
							name='vsp-process-description'
							className='vsp-process-description'
							validations={{maxLength: 1000}}
							type='textarea'/>
					</Dropzone>
				</div>
			</ValidationForm>
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


	handleImportSubmit(files) {
		let {onDataChanged} = this.props;
		this.setState({
			dragging: false,
			complete: '0',
			files
		});
		onDataChanged({artifactName: files[0].name});
	}
}

export default SoftwareProductProcessesEditorView;

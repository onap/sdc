import React from 'react';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Dropzone from 'react-dropzone';


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
		onCancel: React.PropTypes.func
	};

	render() {
		let {isReadOnlyMode, onCancel, onDataChanged, data = {}} = this.props;
		let {name, description, artifactName} = data;

		return (
			<div>
				<ValidationForm
					ref='validationForm'
					isReadOnlyMode={isReadOnlyMode}
					hasButtons={true}
					labledButtons={true}
					onSubmit={ () => this.submit() }
					onReset={ () => onCancel() }
					className='vsp-processes-editor'>
				<div className={`vsp-processes-editor-data${isReadOnlyMode ? ' disabled' : '' }`}>
					<Dropzone
						className={`vsp-process-dropzone-view ${this.state.dragging ? 'active-dragging' : ''}`}
						onDrop={files => this.handleImportSubmit(files)}
						onDragEnter={() => this.setState({dragging:true})}
						onDragLeave={() => this.setState({dragging:false})}
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
			</div>
		);
	}

	submit() {
		const {data: process, previousData: previousProcess} = this.props;
		let {files} = this.state;
		let formData = new FormData();
		if (files.length) {
			let file = files[0];
			formData.append('upload', file);
		}

		let updatedProcess = {
			...process,
			formData: files.length ? formData : false
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

import React from 'react';
import {connect} from 'react-redux';
import Button from 'react-bootstrap/lib/Button.js';
import Dropzone from 'react-dropzone';
import i18n from 'nfvo-utils/i18n/i18n.js';
import ProgressBar from 'nfvo-components/progressBar/ProgressBar.jsx';
import Modal from 'nfvo-components/modal/Modal.jsx';
import UploadScreenActionHelper from './UploadScreenActionHelper.js';
import Attachments from  './attachments/Attachments.js';

const mapStateToProps = ({uploadScreen}) => {
	let {upload} = uploadScreen;
	return {uploadScreen: upload};
};


const mapActionsToProps = dispatch => {
	return {
		onUpload: (formData) => UploadScreenActionHelper.uploadFile(dispatch, formData),
		openMainScreen: () => UploadScreenActionHelper.openMainScreen(dispatch)
	};
};


class UploadScreen extends React.Component {

	state = {
		complete: '10',
		showModal: false,
		fileName: '',
		dragging: false,
		files: []
	};

	interval = '';

	render() {
		let {uploadScreen} = this.props;
		let {showAttachments} = uploadScreen;
		return(
			<div className='heat-validation-stand-alone'>
				{showAttachments ? this.renderTree() : this.renderUploadScreen()}
			</div>
		);
	}

	renderUploadModal() {
		let {complete, showModal, fileName} = this.state;
		return (
			<Modal show={showModal} animation={true}>
				<Modal.Header>
					<Modal.Title>{i18n('Uploading attachments')}</Modal.Title>
				</Modal.Header>
				<Modal.Body>
					<div className='upload-modal-body-content'>
						<div>
							<span className='title'>{i18n('File:')}</span>
							<span className='file-name'>{fileName}</span>
						</div>
						<ProgressBar now={complete} label={`${complete}%`}/>
						<div>{i18n('Upload in progress')}</div>
					</div>
					<Modal.Footer>
						<Button bsStyle='primary' onClick={() => this.onRunBackground()}>
							{i18n('Run in Background')}
						</Button>
						<Button bsStyle='primary' onClick={() => this.onCancel()}>{i18n('Cancel')}</Button>
					</Modal.Footer>
				</Modal.Body>
			</Modal>
		);
	}

	renderUploadScreen() {
		return(
			<div className='upload-screen'>
				<div className='row'>
					<div className='title'>
						<h1>HEAT VALIDATION APPLICATION</h1>
					</div>
				</div>
				<div className='row'>
					<div className='col-md-2 col-md-offset-5'>
						<Dropzone
							className={`upload-screen-drop-zone ${this.state.dragging ? 'active-dragging' : ''}`}
							onDrop={files => this.handleImportSubmit(files)}
							onDragEnter={() => this.setState({dragging:true})}
							onDragLeave={() => this.setState({dragging:false})}
							multiple={false}
							disableClick={true}
							ref='fileInput'
							name='fileInput'
							accept='.zip'>
							<div
								className='upload-screen-upload-block'>
								<div className='drag-text'>{i18n('Drag & drop for upload')}</div>
								<div className='or-text'>{i18n('or')}</div>
								<div className='upload-btn primary-btn' onClick={() => this.refs.fileInput.open()}>
									<span className='primary-btn-text'>{i18n('Select file')}</span>
								</div>
							</div>
						</Dropzone>
					</div>
					{this.renderUploadModal()}
				</div>
			</div>
		);
	}

	renderTree() {
		let {openMainScreen} = this.props;
		return(
			<div className='attachments-screen'>
				<Attachments/>
				<div className='back-button'>
					<div className='upload-btn primary-btn' onClick={() => openMainScreen()}>
						<span className='primary-btn-text'>{i18n('Back')}</span>
					</div>
				</div>
			</div>
		);
	}

	handleImportSubmit(files) {
		this.setState({
			showModal: true,
			fileName: files[0].name,
			dragging: false,
			complete: '0',
			files
		});


		this.interval = setInterval(() => {
			if (this.state.complete >= 90) {
				clearInterval(this.interval);
				this.setState({
					showModal: false,
					fileName: ''
				});
				this.startUploading(files);
			} else {
				this.setState({
					complete: (parseInt(this.state.complete) + 10).toString()
				});
			}
		}, 20);

	}

	onRunBackground() {
		let {files} = this.state;
		clearInterval(this.interval);
		this.startUploading(files);
		this.setState({showModal: false, files: []});
	}

	onCancel() {
		clearInterval(this.interval);
		this.setState({
			showModal: false,
			fileName: '',
			files: []
		});

	}

	startUploading(files) {
		let {onUpload} = this.props;
		if (!(files && files.length)) {
			return;
		}
		let file = files[0];
		let formData = new FormData();
		formData.append('upload', file);
		this.refs.fileInput.value = '';
		onUpload(formData);
	}

}

export default  connect(mapStateToProps, mapActionsToProps)(UploadScreen);

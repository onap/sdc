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
import React, {Component, PropTypes} from 'react';
import Dropzone from 'react-dropzone';
import ButtonGroup from 'react-bootstrap/lib/ButtonGroup.js';
import ButtonToolbar from 'react-bootstrap/lib/ButtonToolbar.js';
import Button from 'react-bootstrap/lib/Button.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {fileTypes, type2Title, type2Name} from './SoftwareProductComponentsMonitoringConstants.js';



class SoftwareProductComponentsMonitoringView extends Component {

	static propTypes = {
		isReadOnlyMode: PropTypes.bool,
		filenames: PropTypes.object,
		softwareProductId: PropTypes.string,

		onDropMibFileToUpload: PropTypes.func,
		onDeleteSnmpFile: PropTypes.func
	};

	state = {
		dragging: false
	};




	render() {
		return (
			<div className='vsp-component-monitoring'>
				{this.renderDropzoneWithType(fileTypes.VES_EVENT)}
				{this.renderDropzoneWithType(fileTypes.SNMP_TRAP)}
				{this.renderDropzoneWithType(fileTypes.SNMP_POLL)}
			</div>
		);
	}

	renderDropzoneWithType(type) {
		let {isReadOnlyMode, filenames} = this.props;
		let fileByType = type2Name[type];
		let fileName = (filenames) ? filenames[fileByType] : undefined;
		let refAndName = `fileInput${type.toString()}`;
		let typeDisplayName = type2Title[type];
		return (
			<Dropzone
				className={`snmp-dropzone ${this.state.dragging ? 'active-dragging' : ''}`}
				onDrop={(acceptedFiles, rejectedFiles) => this.handleImport(acceptedFiles, rejectedFiles, {isReadOnlyMode, type, refAndName})}
				onDragEnter={() => this.handleOnDragEnter(isReadOnlyMode)}
				onDragLeave={() => this.setState({dragging:false})}
				multiple={false}
				disableClick={true}
				ref={refAndName}
				name={refAndName}
				accept='.zip'
				disabled>
				<div className='draggable-wrapper'>
					<div className='section-title'>{typeDisplayName}</div>
					{fileName ? this.renderUploadedFileName(fileName, type) : this.renderUploadButton(refAndName)}
				</div>
			</Dropzone>
		);
	}

	renderUploadButton(refAndName) {
		let {isReadOnlyMode} = this.props;
		return (
			<div
				className={`software-product-landing-view-top-block-col-upl${isReadOnlyMode ? ' disabled' : ''}`}>
				<div className='drag-text'>{i18n('Drag & drop for upload')}</div>
				<div className='or-text'>{i18n('or')}</div>
				<div className='upload-btn primary-btn' data-test-id={`monitoring-upload-${refAndName}`} onClick={() => this.refs[refAndName].open()}>
					<span className='primary-btn-text'>{i18n('Select file')}</span>
				</div>
			</div>
		);
	}

	renderUploadedFileName(filename, type) {
		return (
			<ButtonToolbar>
				<ButtonGroup>
					<Button disabled>{filename}</Button>
					<Button data-test-id={`monitoring-delete-${type}`} className='delete-button' onClick={()=>this.props.onDeleteFile(type)}>X</Button>
				</ButtonGroup>
			</ButtonToolbar>
		);
	}


	handleOnDragEnter(isReadOnlyMode) {
		if (!isReadOnlyMode) {
			this.setState({dragging: true});
		}
	}

	handleImport(files, rejectedFiles, {isReadOnlyMode, type, refAndName}) {
		if (isReadOnlyMode) {
			return;
		}
		if (files.length > 0) {
			this.setState({dragging: false});
			let file = files[0];
			let formData = new FormData();
			formData.append('upload', file);
			this.refs[refAndName].value = '';
			this.props.onDropMibFileToUpload(formData, type);
		} else if (rejectedFiles.length > 0) {
			this.setState({dragging: false});
			this.props.onFileUploadError();
		}
	}
}

export default SoftwareProductComponentsMonitoringView;

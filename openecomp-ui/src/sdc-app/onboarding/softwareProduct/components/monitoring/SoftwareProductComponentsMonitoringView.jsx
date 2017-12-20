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
import React, {Component} from 'react';
import PropTypes from 'prop-types';
import Dropzone from 'react-dropzone';
import Button from 'sdc-ui/lib/react/Button.js';
import DraggableUploadFileBox from 'nfvo-components/fileupload/DraggableUploadFileBox.jsx';
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
				className={`dropzone ${this.state.dragging ? 'active-dragging' : ''}`}
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
					{fileName ? this.renderUploadedFileName(fileName, type, isReadOnlyMode) : this.renderUploadButton(refAndName)}
				</div>
			</Dropzone>
		);
	}

	renderUploadButton(refAndName) {
		let {isReadOnlyMode} = this.props;
		return (
			<DraggableUploadFileBox
				dataTestId={`monitoring-upload-${refAndName}`}
				className='software-product-landing-view-top-block-col-upl'
				isReadOnlyMode={isReadOnlyMode}
				onClick={() => this.refs[refAndName].open()}/>
		);
	}

	renderUploadedFileName(filename, type, isReadOnlyMode) {
		return (
				<div className='monitoring-file'>
					<Button
						color='white'
						disabled={true}
						className='filename'>
						{filename}
					</Button>

					<Button
						color='gray'
						data-test-id={`monitoring-delete-${type}`}
						disabled={isReadOnlyMode}
						onClick={()=>this.props.onDeleteFile(type)}
						iconName='close'
						className='delete'/>
				</div>
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

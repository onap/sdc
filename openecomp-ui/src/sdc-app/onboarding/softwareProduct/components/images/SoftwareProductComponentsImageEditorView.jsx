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

import i18n from 'nfvo-utils/i18n/i18n.js';
import Form from 'nfvo-components/input/validation/Form.jsx';

import FileDetails from './imagesEditorComponents/FileDetails.jsx';
import ImageDetails from './imagesEditorComponents/ImageDetails.jsx';
import {imageCustomValidations} from './ImageValidations.js';

class SoftwareProductComponentsImageEditorView extends React.Component {
	static propTypes = {
		onDataChanged: PropTypes.func.isRequired,
		onSubmit: PropTypes.func.isRequired,
		onCancel: PropTypes.func.isRequired
	};

	render() {
		let {onCancel, onValidateForm, isReadOnlyMode, isFormValid, formReady, data = {}, genericFieldInfo, qgenericFieldInfo, dataMap, onDataChanged, isManual, onQDataChanged} = this.props;
		let {id, fileName} = data;
		let editingMode = Boolean(id);
		return (
			<div>
				{genericFieldInfo && <Form
					ref={(form) => { this.form = form; }}
					hasButtons={true}
					onSubmit={ () => this.submit() }
					onReset={ () => onCancel() }
					labledButtons={true}
					isReadOnlyMode={isReadOnlyMode}
					isValid={isFormValid}
					formReady={formReady}
					submitButtonText={editingMode ? i18n('Save') : i18n('Create')}
					onValidateForm={() => onValidateForm(imageCustomValidations) }
					className='vsp-components-image-editor'>
					<div className='editor-data'>
						<FileDetails
							editingMode={editingMode}
							genericFieldInfo={genericFieldInfo}
							qgenericFieldInfo={qgenericFieldInfo}
							fileName={fileName}
							onDataChanged={onDataChanged}
							isManual={isManual}
							dataMap={dataMap}
							onQDataChanged={onQDataChanged}/>
						{editingMode && <ImageDetails dataMap={dataMap} qgenericFieldInfo={qgenericFieldInfo} onQDataChanged={onQDataChanged}/>}
					</div>
				</Form>}
			</div>
		);
	}
	submit() {
		let {data, qdata, onSubmit, version} = this.props;
		onSubmit({data, qdata, version});
	}
}

export default SoftwareProductComponentsImageEditorView;

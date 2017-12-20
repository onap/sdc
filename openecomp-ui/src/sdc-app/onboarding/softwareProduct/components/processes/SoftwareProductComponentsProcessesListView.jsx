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
import Modal from 'nfvo-components/modal/Modal.jsx';
import SoftwareProductProcessesEditor from './SoftwareProductComponentProcessesEditor.js';
import SoftwareProductProcessListView from 'sdc-app/onboarding/softwareProduct/processes/SoftwareProductProcessListView.jsx';

class SoftwareProductProcessesView extends React.Component {

	state = {
		localFilter: ''
	};

	static propTypes = {
		onAddProcess: PropTypes.func,
		onEditProcess: PropTypes.func,
		onDeleteProcess: PropTypes.func,
		isDisplayModal: PropTypes.bool,
		isModalInEditMode: PropTypes.bool,
		onStorageSelect: PropTypes.func,
		componentId: PropTypes.string,
		softwareProductId: PropTypes.string,
		currentSoftwareProduct: PropTypes.object
	};

	render() {
		return (
			<div className='vsp-processes-page'>
				<div className='software-product-view'>
					<div className='software-product-landing-view-right-side vsp-components-processes-page flex-column'>
						{this.renderEditor()}
						<SoftwareProductProcessListView addButtonTitle={i18n('Add Component Process Details')} {...this.props}/>
					</div>
				</div>
			</div>
		);
	}

	renderEditor() {
		let {softwareProductId, version, componentId, isReadOnlyMode, isDisplayModal, isModalInEditMode} = this.props;
		return (
			<Modal show={isDisplayModal} bsSize='large' animation={true} className='onborading-modal'>
				<Modal.Header>
					<Modal.Title>{isModalInEditMode ? i18n('Edit Process Details') : i18n('Create New Process Details')}</Modal.Title>
				</Modal.Header>
				<Modal.Body className='edit-process-modal'>
					<SoftwareProductProcessesEditor
						componentId={componentId}
						softwareProductId={softwareProductId}
						version={version}
						isReadOnlyMode={isReadOnlyMode}/>
				</Modal.Body>
			</Modal>

		);
	}

}

export default SoftwareProductProcessesView;

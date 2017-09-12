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
import Tabs from 'react-bootstrap/lib/Tabs.js';
import Tab from 'react-bootstrap/lib/Tab.js';
import {tabsMapping} from './SoftwareProductAttachmentsConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import HeatValidation from './validation/HeatValidation.js';
import {onboardingOriginTypes} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';

class HeatScreenView extends Component {

	static propTypes = {
		isValidationAvailable: PropTypes.bool,
		goToOverview: PropTypes.bool,
		setActiveTab: PropTypes.function
	};

	render() {
		let {isValidationAvailable, isReadOnlyMode, heatDataExist, onDownload, softwareProductId, onProcessAndValidate,
			heatSetup, HeatSetupComponent, onGoToOverview, version, onboardingOrigin, activeTab, setActiveTab, ...other} = this.props;

		return (
			<div className='vsp-attachments-view'>
				<div className='attachments-view-controllers'>
					{(activeTab === tabsMapping.SETUP) &&
						<SVGIcon
							disabled={heatDataExist ? false : true}
							name='download'
							className='icon-component'
							label={i18n('Export Validation')}
							labelPosition='right'
							color='secondary'
							onClick={heatDataExist ? () => onDownload({heatCandidate: heatSetup, isReadOnlyMode, version}) : undefined}
							data-test-id='download-heat'/>}
					{(activeTab === tabsMapping.VALIDATION && softwareProductId) &&
						<SVGIcon
							disabled={this.props.goToOverview !== true}
							onClick={this.props.goToOverview ? () => onGoToOverview({version}) : undefined}
							name='proceedToOverview'
							className='icon-component'
							label={i18n('Go to Overview')}
							labelPosition='right'
							color={this.props.goToOverview ? 'primary' : 'secondary'}
							data-test-id='go-to-overview'/>}
					<SVGIcon
						name='upload'
						className='icon-component'
						label={i18n('Upload New File')}
						labelPosition='right'
						color='secondary'
						disabled={isReadOnlyMode}
						onClick={evt => {this.refs.hiddenImportFileInput.click(evt);}}
						data-test-id='upload-heat'/>
					<input
						ref='hiddenImportFileInput'
						type='file'
						name='fileInput'
						accept='.zip, .csar'
						onChange={evt => this.handleImport(evt)}/>
				</div>
				<Tabs id='attachments-tabs' activeKey={activeTab} onSelect={key => this.handleTabPress(key)}>
					<Tab eventKey={tabsMapping.SETUP} title='Setup' disabled={onboardingOrigin === onboardingOriginTypes.CSAR}>
						<HeatSetupComponent
							heatDataExist={heatDataExist}
							changeAttachmentsTab={tab => setActiveTab({activeTab: tab})}
							onProcessAndValidate={onProcessAndValidate}
							softwareProductId={softwareProductId}
							isReadOnlyMode={isReadOnlyMode}
							version={version}/>
					</Tab>
					<Tab eventKey={tabsMapping.VALIDATION} title='Validation' disabled={!isValidationAvailable}>
						<HeatValidation {...other}/>
					</Tab>
				</Tabs>
			</div>
		);
	}

	handleTabPress(key) {
		let {heatSetup, heatSetupCache, onProcessAndValidate, isReadOnlyMode, version, setActiveTab} = this.props;
		switch (key) {
			case tabsMapping.VALIDATION:
				onProcessAndValidate({heatData: heatSetup, heatDataCache: heatSetupCache, isReadOnlyMode, version}).then(
					() => setActiveTab({activeTab: tabsMapping.VALIDATION})
				);
				return;
			case tabsMapping.SETUP:
				setActiveTab({activeTab: tabsMapping.SETUP});
				return;
		}
	}

	handleImport(evt) {
		evt.preventDefault();
		let {version} = this.props;
		let formData = new FormData();
		formData.append('upload', this.refs.hiddenImportFileInput.files[0]);
		this.refs.hiddenImportFileInput.value = '';
		this.props.onUpload(formData, version);
	}

	save() {

		return this.props.onboardingOrigin === onboardingOriginTypes.ZIP ?
			this.props.onSave(this.props.heatSetup, this.props.version) :
			Promise.resolve();
	}

}

export default HeatScreenView;

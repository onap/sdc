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
import Icon from 'nfvo-components/icon/Icon.jsx';
import HeatValidation from './validation/HeatValidation.js';

class HeatScreenView extends Component {

	static propTypes = {
		isValidationAvailable: PropTypes.bool,
		goToOverview: PropTypes.bool
	};

	state = {
		activeTab: tabsMapping.SETUP
	};

	render() {
		let {isValidationAvailable, isReadOnlyMode, heatDataExist, onDownload, softwareProductId, onProcessAndValidate, heatSetup, HeatSetupComponent, onGoToOverview, version, ...other} = this.props;
		return (
			<div className='vsp-attachments-view'>
				<div className='attachments-view-controllers'>
					{(this.state.activeTab === tabsMapping.SETUP) &&
						<Icon
							iconClassName={heatDataExist ? '' : 'disabled'}
							className={heatDataExist ? '' : 'disabled'}
							image='download'
							label={i18n('Download HEAT')}
							onClick={heatDataExist ? () => onDownload({heatCandidate: heatSetup, isReadOnlyMode, version}) : undefined}
							data-test-id='download-heat'/>}
					{(this.state.activeTab === tabsMapping.VALIDATION && softwareProductId) &&
						<Icon
							iconClassName={this.props.goToOverview ? '' : 'disabled'}
							className={`go-to-overview-icon ${this.props.goToOverview ? '' : 'disabled'}`}
							labelClassName='go-to-overview-label'
							onClick={this.props.goToOverview ? () => onGoToOverview({version}) : undefined}
							image='go-to-overview'
							label={i18n('Go to Overview')}
							data-test-id='go-to-overview'/>}
					<Icon
						image='upload'
						label={i18n('Upload New HEAT')}
						className={isReadOnlyMode ? 'disabled' : ''}
						iconClassName={isReadOnlyMode ? 'disabled' : ''}
						onClick={evt => {this.refs.hiddenImportFileInput.click(evt);}}
						data-test-id='upload-heat'/>
					<input
						ref='hiddenImportFileInput'
						type='file'
						name='fileInput'
						accept='.zip'
						onChange={evt => this.handleImport(evt)}/>
				</div>
				<Tabs id='attachments-tabs' activeKey={this.state.activeTab} onSelect={key => this.handleTabPress(key)}>
					<Tab  eventKey={tabsMapping.SETUP} title='HEAT Setup'>
						<HeatSetupComponent
							heatDataExist={heatDataExist}
							changeAttachmentsTab={tab => this.setState({activeTab: tab})}
							onProcessAndValidate={onProcessAndValidate}
							softwareProductId={softwareProductId}
							isReadOnlyMode={isReadOnlyMode}
							version={version}/>
					</Tab>
					<Tab eventKey={tabsMapping.VALIDATION} title='Heat Validation' disabled={!isValidationAvailable}>
						<HeatValidation {...other}/>
					</Tab>
				</Tabs>
			</div>
		);
	}

	handleTabPress(key) {
		let {heatSetup, heatSetupCache, onProcessAndValidate, isReadOnlyMode, version} = this.props;		
		switch (key) {
			case tabsMapping.VALIDATION:				
				onProcessAndValidate({heatData: heatSetup, heatDataCache: heatSetupCache, isReadOnlyMode, version}).then(
					() => this.setState({activeTab: tabsMapping.VALIDATION})
				);
				return;
			case tabsMapping.SETUP:
				this.setState({activeTab: tabsMapping.SETUP});
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
		this.setState({activeTab: tabsMapping.SETUP});
	}

	save() {
		return this.props.onSave(this.props.heatSetup, this.props.version);
	}

}

export default HeatScreenView;

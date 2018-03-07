/*!
 * Copyright © 2016-2018 European Support Limited
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
import accept from 'attr-accept';
import {SVGIcon, Tab, Tabs} from 'sdc-ui/lib/react';
import {tabsMapping} from './SoftwareProductAttachmentsConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import HeatValidation from './validation/HeatValidation.js';
import {onboardingOriginTypes} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import Button from 'sdc-ui/lib/react/Button.js';

class HeatScreenView extends Component {

	static propTypes = {
		isValidationAvailable: PropTypes.bool,
		goToOverview: PropTypes.bool,
		setActiveTab: PropTypes.func
	};

	componentDidMount() {		
		 if (!this.props.goToOverview && this.props.candidateInProcess) {			
			this.props.setActiveTab({activeTab: tabsMapping.VALIDATION});
		 }
	}

	render() {
		let {isValidationAvailable, isReadOnlyMode, heatDataExist, onDownload, softwareProductId, onProcessAndValidate, onUploadAbort,
			candidateInProcess, heatSetup, HeatSetupComponent, onGoToOverview, version, onboardingOrigin, activeTab, setActiveTab, ...other} = this.props;

		return (
			<div className='vsp-attachments-view'>
				<div className='attachments-view-controllers'>
					{(activeTab === tabsMapping.SETUP) && candidateInProcess &&	
						<Button  
							data-test-id='proceed-to-validation-btn'
							disabled={!isValidationAvailable} 
							className='proceed-to-validation-btn'
							onClick={()=>this.validate()}>{i18n('PROCEED TO VALIDATION')}</Button>					
					}		
					{candidateInProcess && <SVGIcon							
						onClick={onUploadAbort}
						name='close'
						className='icon-component abort-btn'
						label={i18n('ABORT')}
						labelPosition='right'
						color='secondary'
						data-test-id='abort-btn'/>
					}	
					
					{(activeTab === tabsMapping.VALIDATION && softwareProductId) &&
						<Button btnType='outline' 
							data-test-id='go-to-overview'
							disabled={this.props.goToOverview !== true}
							className='go-to-overview-btn'
							onClick={this.props.goToOverview ? () => onGoToOverview({version}) : undefined}>{i18n('GO TO OVERVIEW')}</Button>}						
					<div className='separator'></div>		
					<SVGIcon
						disabled={heatDataExist ? false : true}
						name='download'
						className='icon-component'														
						color='dark-gray'
						onClick={heatDataExist ? () => onDownload({heatCandidate: heatSetup, isReadOnlyMode, version}) : undefined}
						data-test-id='download-heat'/>

					<SVGIcon
						name='upload'
						className='icon-component'						
						color='dark-gray'
						disabled={isReadOnlyMode || candidateInProcess}
						onClick={isReadOnlyMode ? undefined : evt => this.refs.hiddenImportFileInput.click(evt)}
						data-test-id='upload-heat'/>
					<input
						ref='hiddenImportFileInput'
						type='file'
						name='fileInput'
						accept='.zip, .csar'
						onChange={evt => this.handleImport(evt)}/>
				</div>
				<Tabs
					className='attachments-tabs'
					type='header'
					activeTab={activeTab}
					onTabClick={key => this.handleTabPress(key)}>
					<Tab tabId={tabsMapping.SETUP} title='Setup' disabled={onboardingOrigin === onboardingOriginTypes.CSAR}>
						<HeatSetupComponent
							heatDataExist={heatDataExist}
							changeAttachmentsTab={tab => setActiveTab({activeTab: tab})}
							onProcessAndValidate={onProcessAndValidate}
							softwareProductId={softwareProductId}
							isReadOnlyMode={isReadOnlyMode}
							version={version}/>
					</Tab>
					<Tab tabId={tabsMapping.VALIDATION} title='Validation' disabled={!isValidationAvailable || candidateInProcess}>
						<HeatValidation {...other}/>
					</Tab>
				</Tabs>
			</div>
		);
	}

	handleTabPress(key) {
		let {setActiveTab} = this.props;
		switch (key) {
			case tabsMapping.VALIDATION:
				setActiveTab({activeTab: tabsMapping.VALIDATION});		
				return;
			case tabsMapping.SETUP:
				setActiveTab({activeTab: tabsMapping.SETUP});
				return;
		}
	}

	handleImport(evt) {
		evt.preventDefault();
		let file = this.refs.hiddenImportFileInput.files[0];
		if(! (file && file.size && accept(file, ['.zip', '.csar'])) ) {
			this.props.onInvalidFileUpload();
			return;
		}
		let {version} = this.props;
		let formData = new FormData();
		formData.append('upload', file);
		this.refs.hiddenImportFileInput.value = '';
		this.props.onUpload(formData, version);
	}
	validate() {
		let {heatSetup, heatSetupCache, onProcessAndValidate, isReadOnlyMode, version, setActiveTab} = this.props;
		onProcessAndValidate({heatData: heatSetup, heatDataCache: heatSetupCache, isReadOnlyMode, version}).then(
			() => setActiveTab({activeTab: tabsMapping.VALIDATION})
		);
	}
	save() {

		return this.props.onboardingOrigin === onboardingOriginTypes.ZIP ?
			this.props.onSave(this.props.heatSetup, this.props.version) :
			Promise.resolve();
	}

}

export default HeatScreenView;

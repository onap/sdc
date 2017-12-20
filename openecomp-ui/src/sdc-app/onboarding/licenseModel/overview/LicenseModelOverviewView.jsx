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
import classNames from 'classnames';

import EntitlementPoolsEditor from '../entitlementPools/EntitlementPoolsEditor.js';
import FeatureGroupEditor from '../featureGroups/FeatureGroupEditor.js';
import LicenseAgreementEditor from '../licenseAgreement/LicenseAgreementEditor.js';
import LicenseKeyGroupsEditor from '../licenseKeyGroups/LicenseKeyGroupsEditor.js';
import {overviewEditorHeaders, selectedButton} from './LicenseModelOverviewConstants.js';

import SummaryView from './SummaryView.jsx';
import VLMListView from './VLMListView.jsx';
import ListButtons from './summary/ListButtons.jsx';


const setModalClassName = (modalHeader) => {
	switch (modalHeader) {
		case overviewEditorHeaders.ENTITLEMENT_POOL:
			return 'entitlement-pools-modal';
		case overviewEditorHeaders.LICENSE_AGREEMENT:
			return 'license-agreement-modal';
		case overviewEditorHeaders.FEATURE_GROUP:
			return 'feature-group-modal';
		case overviewEditorHeaders.LICENSE_KEY_GROUP:
			return 'license-key-groups-modal';
		default:
			return '';
	}
};

class LicenseModelOverviewView extends React.Component {

	static propTypes = {
		isDisplayModal: PropTypes.bool,
		isReadOnlyMode: PropTypes.bool,
		licenseModelId: PropTypes.string,
		licensingDataList: PropTypes.array,
		orphanDataList: PropTypes.array,
		modalHeader: PropTypes.string,
		selectedTab: PropTypes.string,
		onTabSelect: PropTypes.func,
		onCallVCAction: PropTypes.func,
		onClose: PropTypes.func
	};

	render() {
		let {isDisplayModal, modalHeader, licensingDataList, selectedTab, onTabSelect, orphanDataList, isReadOnlyMode} = this.props;
		let selectedInUse = selectedTab !== selectedButton.NOT_IN_USE;
		let dataList = selectedInUse ? licensingDataList : orphanDataList;
		return(
			<div className='license-model-overview'>
				<SummaryView  isReadOnlyMode={isReadOnlyMode}/>
				<div className={classNames('overview-list-section ', !selectedInUse ? 'overview-list-orphans' : '' )}>
					<div className='vlm-list-tab-panel'>
						<ListButtons onTabSelect={onTabSelect}
							 selectedTab={selectedTab}
							 hasOrphans={orphanDataList.length > 0}
							 hasLicensing={licensingDataList.length > 0}/>
					</div>
					<VLMListView licensingDataList={dataList} showInUse={selectedInUse}/>
				</div>
				{
					isDisplayModal &&
					<Modal show={isDisplayModal} bsSize='large' animation={true} className={classNames('onborading-modal license-model-modal', setModalClassName(modalHeader))}>
						<Modal.Header>
							<Modal.Title>{`${i18n('Create New ')}${i18n(modalHeader)}`}</Modal.Title>
						</Modal.Header>
						<Modal.Body>
							{this.renderModalBody(modalHeader)}
						</Modal.Body>
					</Modal>
				}
			</div>
		);
	}

	renderModalBody(modalHeader) {
		let {licenseModelId, version, isReadOnlyMode} = this.props;
		switch (modalHeader) {
			case overviewEditorHeaders.ENTITLEMENT_POOL:
				return <EntitlementPoolsEditor version={version} licenseModelId={licenseModelId} isReadOnlyMode={isReadOnlyMode}/>;
			case overviewEditorHeaders.LICENSE_AGREEMENT:
				return <LicenseAgreementEditor version={version} licenseModelId={licenseModelId} isReadOnlyMode={isReadOnlyMode}/>;
			case overviewEditorHeaders.FEATURE_GROUP:
				return <FeatureGroupEditor version={version} licenseModelId={licenseModelId} isReadOnlyMode={isReadOnlyMode}/>;
			case overviewEditorHeaders.LICENSE_KEY_GROUP:
				return <LicenseKeyGroupsEditor version={version} licenseModelId={licenseModelId} isReadOnlyMode={isReadOnlyMode}/>;
		}
	}
}

export default  LicenseModelOverviewView;

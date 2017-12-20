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
import {Collapse} from 'react-bootstrap';
import LicenseAgreement from './listItems/LicenseAgreement.jsx';
import EntitlementPool from './listItems/EntitlementPool.jsx';
import FeatureGroup from './listItems/FeatureGroup.jsx';
import LicenseKeyGroup from './listItems/LicenseKeyGroup.jsx';
import {overviewEditorHeaders} from './LicenseModelOverviewConstants.js';

class VLMListView extends Component {

	static propTypes = {
		licensingDataList: PropTypes.array,
		showInUse: PropTypes.bool
	};

	state = {

	};

	render() {
		let {licensingDataList = []} = this.props;
		return (
			<div className='vlm-list-view'>
				<div>
					<ul className='vlm-list' data-test-id='vlm-list'>
						{licensingDataList.map(item => this.renderLicensingItem(item))}
					</ul>
				</div>
			</div>
		);
	}

	renderLicensingItem(item) {
		switch (item.itemType) {
			case overviewEditorHeaders.LICENSE_AGREEMENT :
				return this.renderLicenseAgreementItem(item);
			case overviewEditorHeaders.FEATURE_GROUP :
				return this.renderFeatureGroupItem(item);
			case overviewEditorHeaders.LICENSE_KEY_GROUP :
				return this.renderLicenseKeyGroupItem(item);
			case overviewEditorHeaders.ENTITLEMENT_POOL:
				return this.renderEntitlementPoolItem(item);
			default:
				return;
		}
	}

	renderLicenseAgreementItem(licenseAgreement) {
		return (
			<li  key={licenseAgreement.id}>
				<LicenseAgreement
					laData={licenseAgreement}
					isCollapsed={this.state[licenseAgreement.id]}
					onClick={event => this.updateCollapsable(event, licenseAgreement.id) }
					isOrphan={!this.props.showInUse}/>
				<Collapse in={this.state[licenseAgreement.id]}>
					<ul>
						{licenseAgreement.children && licenseAgreement.children.map(item => this.renderLicensingItem(item))}
					</ul>
				</Collapse>
			</li>
		);
	}

	renderFeatureGroupItem(featureGroup) {
		const {showInUse} = this.props;
		return (
			<li key={featureGroup.id}>
				<FeatureGroup
					fgData={featureGroup}
					isCollapsed={this.state[featureGroup.id]}
					onClick={event=> this.updateCollapsable(event, featureGroup.id) }
					isOrphan={!this.props.showInUse}/>
				{
					showInUse && <Collapse in={this.state[featureGroup.id]}>
					<ul>
						{featureGroup.children && featureGroup.children.map(item => this.renderLicensingItem(item))}

					</ul>
				</Collapse>
				}
			</li>
		);
	}

	renderEntitlementPoolItem(entitlementPool) {
		return (
			<li key={entitlementPool.id}>
				<EntitlementPool epData={entitlementPool} isOrphan={!this.props.showInUse}/>
			</li>
		);
	}

	renderLicenseKeyGroupItem(licenseKeyGroup) {
		return (
			<li key={licenseKeyGroup.id}>
				<LicenseKeyGroup lkgData={licenseKeyGroup} isOrphan={!this.props.showInUse}/>
			</li>
		);
	}

	updateCollapsable(event, id) {
		event.stopPropagation();
		let obj = {};
		obj[id] = !this.state[id];
		this.setState(obj);
	}
}

export default VLMListView;

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
import {connect} from 'react-redux';

import Tooltip from 'react-bootstrap/lib/Tooltip.js';
import OverlayTrigger from 'react-bootstrap/lib/OverlayTrigger.js';
import SVGIcon from 'sdc-ui/lib/react/SVGIcon.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import licenseModelOverviewActionHelper from '../licenseModelOverviewActionHelper.js';
import LicenseModelActionHelper from '../../LicenseModelActionHelper.js';
import LicenseModelDescriptionEdit from './LicenseModelDescriptionEdit.jsx';
import {VLM_DESCRIPTION_FORM} from '../LicenseModelOverviewConstants.js';

export const mapStateToProps = ({
	licenseModel: {
		licenseModelEditor: {data},
		licenseModelOverview: {descriptionEditor: {data: descriptionData = {}, genericFieldInfo}}
	}
}) => {
	let {description} = descriptionData;
	return {
		data,
		description,
		genericFieldInfo
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onDataChanged: (deltaData) => ValidationHelper.dataChanged(dispatch, {deltaData, formName: VLM_DESCRIPTION_FORM}),
		onCancel: () => licenseModelOverviewActionHelper.editDescriptionClose(dispatch),
		onSubmit: (licenseModel) => {
			licenseModelOverviewActionHelper.editDescriptionClose(dispatch);
			LicenseModelActionHelper.saveLicenseModel(dispatch, {licenseModel});
		},
		onVendorDescriptionEdit: description => licenseModelOverviewActionHelper.editDescriptionOpen(dispatch,{description})
	};
};

export class VendorDataView extends Component {
	render() {
		let {data: {vendorName}, description, isReadOnlyMode} = this.props;
		return (
			<div className='vendor-data-view'>
				<div className='vendor-title'>vendor</div>
				<div className='vendor-name' data-test-id='vlm-summary-vendor-name'>{vendorName}</div>
				{
					description !== undefined && !isReadOnlyMode ? this.renderDescriptionEdit() : this.renderDescription()
				}
			</div>
		);
	}

	componentWillUnmount() {
		this.props.onCancel();
	}


	renderDescription() {
		let {data: {description}, onVendorDescriptionEdit, isReadOnlyMode} = this.props;
		return (
			<div onClick={() => {if (!isReadOnlyMode) {onVendorDescriptionEdit(description);}}} className={!isReadOnlyMode ? 'vendor-description' : 'vendor-description-readonly'}>
				{this.renderOverlay(
					<div className='description-data' data-test-id='vlm-summary-vendor-description'>
						{description}
						{!isReadOnlyMode && <SVGIcon name='pencil'/>}
					</div>
				)}
			</div>
		);
	}

	renderDescriptionEdit() {
		let {onCancel, onDataChanged, onSubmit, description, genericFieldInfo, data} = this.props;
		return(
			<LicenseModelDescriptionEdit onClose={onCancel} onDataChanged={onDataChanged} onSubmit={onSubmit} data={data} genericFieldInfo={genericFieldInfo} description={description}/>
		);
	}

	renderOverlay(children) {
		let {data: {description}, isReadOnlyMode} = this.props;
		if (isReadOnlyMode) {
			return (
				<OverlayTrigger
					placement='bottom'
					overlay={<Tooltip className='vendor-description-tooltip' id='tooltip-bottom'>{description}</Tooltip>}
					delayShow={400}>
					{children}
				</OverlayTrigger>
			);
		}
		return children;
	}

}

export default connect(mapStateToProps, mapActionsToProps)(VendorDataView);

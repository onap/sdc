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
import {modalMapper, catalogItemTypes, catalogItemTypeClasses } from './onboardingCatalog/OnboardingCatalogConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import Modal from 'nfvo-components/modal/Modal.jsx';
import LicenseModelCreation from '../licenseModel/creation/LicenseModelCreation.js';
import SoftwareProductCreation from '../softwareProduct/creation/SoftwareProductCreation.js';

class CatalogModal extends React.Component{

	getModalDetails(){
		const {modalToShow} = this.props;
		switch (modalToShow) {
			case catalogItemTypes.LICENSE_MODEL:
				return {
					title: i18n('New License Model'),
					element: <LicenseModelCreation/>
				};
			case catalogItemTypes.SOFTWARE_PRODUCT:
				return {
					title: i18n('New Software Product'),
					element: <SoftwareProductCreation/>
				};
		}
	}

	render(){
		const {modalToShow} = this.props;
		const modalDetails = this.getModalDetails(modalToShow);

		return (
			<Modal
				show={Boolean(modalDetails)}
				className={`${catalogItemTypeClasses[modalMapper[modalToShow]]}-modal`}>
				<Modal.Header>
					<Modal.Title>{modalDetails && modalDetails.title}</Modal.Title>
				</Modal.Header>
				<Modal.Body>
					{
						modalDetails && modalDetails.element
					}
				</Modal.Body>
			</Modal>
		);
	}
}

export default CatalogModal;

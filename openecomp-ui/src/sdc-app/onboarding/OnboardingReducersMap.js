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
import currentScreenReducer from './OnboardingReducers.js';
import licenseModelListReducer from './licenseModel/LicenseModelListReducer.js';
import finalizedLicenseModelListReducer from './licenseModel/FinalizedLicenseModelListReducer.js';
import licenseModelReducer from './licenseModel/LicenseModelReducer.js';
import softwareProductReducer from './softwareProduct/SoftwareProductReducer.js';
import softwareProductListReducer from './softwareProduct/SoftwareProductListReducer.js';
import finalizedSoftwareProductReducer from './softwareProduct/FinalizedSoftwareProductReducer.js';
import onboardReducer from './onboard/OnboardReducer.js';
import versionsPageReducer from './versionsPage/VersionsPageReducer.js';
import usersReducer from './users/UsersReducers.js';
import mergeEditorReducer from 'sdc-app/common/merge/MergeEditorReducer.js';
import revisionsReducer from './revisions/RevisionsReducer.js';

export default {
	currentScreen: currentScreenReducer,
	licenseModel: licenseModelReducer,
	licenseModelList: licenseModelListReducer,
	finalizedLicenseModelList: finalizedLicenseModelListReducer,
	finalizedSoftwareProductList: finalizedSoftwareProductReducer,
	mergeEditor: mergeEditorReducer,
	onboard: onboardReducer,
	softwareProduct: softwareProductReducer,
	softwareProductList: softwareProductListReducer,
	users: usersReducer,
	versionsPage: versionsPageReducer,
	revisions: revisionsReducer
};

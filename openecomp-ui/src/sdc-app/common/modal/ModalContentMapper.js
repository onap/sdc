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

import SoftwareProductCreation from 'sdc-app/onboarding/softwareProduct/creation/SoftwareProductCreation.js';
import LicenseModelCreation from 'sdc-app/onboarding/licenseModel/creation/LicenseModelCreation.js';
import SubmitErrorResponse from 'nfvo-components/SubmitErrorResponse.jsx';

export const modalContentMapper = {	
	SOFTWARE_PRODUCT_CREATION: 'SOFTWARE_PRODUCT_CREATION',
	LICENSE_MODEL_CREATION: 'LICENSE_MODEL_CREATION',
	SUMBIT_ERROR_RESPONSE: 'SUMBIT_ERROR_RESPONSE'
};

export const modalContentComponents = {
	SUMBIT_ERROR_RESPONSE: SubmitErrorResponse,
	SOFTWARE_PRODUCT_CREATION: SoftwareProductCreation,
	LICENSE_MODEL_CREATION: LicenseModelCreation,
};
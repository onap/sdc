/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.sdc.backend.ci.tests.datatypes;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = false)
@NoArgsConstructor
public class VendorSoftwareProductObject extends VendorSoftwareProductObjectReqDetails {

	private String vspId;
	private String componentId;
	private String attContact;
	private String version;
	private String versionId;

	public VendorSoftwareProductObject(String vspId, String componentId, String attContact, String version) {
		this.vspId = vspId;
		this.componentId = componentId;
		this.attContact = attContact;
		this.version = version;
	}

	public VendorSoftwareProductObject(String name, String description, String category, String subCategory, String vendorId, String vendorName, String licensingVersion, LicensingData licensingData, String onboardingMethod, String networkPackageName, String onboardingOrigin, String icon, String vspId, String componentId, String attContact, String version) {
		super(name, description, category, subCategory, vendorId, vendorName, licensingVersion, licensingData, onboardingMethod, networkPackageName, onboardingOrigin, icon);
		this.vspId = vspId;
		this.componentId = componentId;
		this.attContact = attContact;
		this.version = version;
	}

}

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

package org.openecomp.sdc.ci.tests.datatypes;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
public class VendorLicenseModel {

	private final String vendorId;
	private final String vendorLicenseName;
	private final String vendorLicenseAgreementId;
	private final String featureGroupId;
	private String licenseVersionId;
	private String licenseVersionLabel;
	@Setter
	private String version;

	public VendorLicenseModel(final String vendorId, final String vendorLicenseName,
							  final String vendorLicenseAgreementId, final String featureGroupId,
							  final String licenseVersionId, final String licenseVersionLabel) {
		this.vendorId = vendorId;
		this.vendorLicenseName = vendorLicenseName;
		this.vendorLicenseAgreementId = vendorLicenseAgreementId;
		this.featureGroupId = featureGroupId;
		this.licenseVersionId = licenseVersionId;
		this.licenseVersionLabel = licenseVersionLabel;
	}

	public VendorLicenseModel(String vendorId, String vendorLicenseName, String vendorLicenseAgreementId, String featureGroupId) {
		this.vendorId = vendorId;
		this.vendorLicenseName = vendorLicenseName;
		this.vendorLicenseAgreementId = vendorLicenseAgreementId;
		this.featureGroupId = featureGroupId;
	}

}

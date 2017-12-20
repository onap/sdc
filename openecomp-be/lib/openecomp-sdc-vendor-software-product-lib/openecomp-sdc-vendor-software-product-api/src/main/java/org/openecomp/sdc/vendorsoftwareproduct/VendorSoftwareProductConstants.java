/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.vendorsoftwareproduct;

public final class VendorSoftwareProductConstants {
  public static final String VENDOR_SOFTWARE_PRODUCT_VERSIONABLE_TYPE = "VendorSoftwareProduct";
  public static final String VSP_PACKAGE_ZIP = "VSPPackage.zip";
  public static final String CSAR = "CSAR";
  public static final String UPLOAD_RAW_DATA = "UPLOAD_RAW_DATA";
  public static final String INFORMATION_ARTIFACT_NAME = "VSP_%s_Information.txt";
  public static final String UNSUPPORTED_OPERATION_ERROR =
      "An error has occurred: Unsupported operation for 1707 release.";
  public static final String NAME_PATTERN = "^[a-zA-Z0-9_]*$";
  public static final String VALIDATION_VSP_NAME = "validationOnlyVspName";

  public final class UniqueValues {
    public static final String VENDOR_SOFTWARE_PRODUCT_NAME = "Vendor Software Product name";
    public static final String PROCESS_NAME = "Process name";
    public static final String NETWORK_NAME = "Network name";
    public static final String COMPONENT_NAME = "ComponentData name";
    public static final String NIC_NAME = "NIC name";
    public static final String COMPUTE_NAME = "Compute name";
    public static final String ORCHESTRATION_CANDIDATE_NAME = "Orchestration Candidate name";
    //public static final String COMPONENT_ARTIFACT_NAME = "ComponentArtifact name";
    public static final String DEPLOYMENT_FLAVOR_NAME = "Deployment Flavor name";
    public static final String IMAGE_NAME = "Image name";
  }
}

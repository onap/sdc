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

import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.VersionedVendorSoftwareProductInfo;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.openecomp.sdc.versioning.types.VersionInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface  VendorSoftwareProductManager {

  Version checkout(String vendorSoftwareProductId, String user);

  Version undoCheckout(String vendorSoftwareProductId, String user);

  Version checkin(String vendorSoftwareProductId, String user);

  ValidationResponse submit(String vendorSoftwareProductId, String user) throws IOException;


  List<VersionedVendorSoftwareProductInfo> listVsps(String versionFilter, String user);

  VspDetails createVsp(VspDetails vspDetails, String user);

  void updateVsp(VspDetails vspDetails, String user);

  VspDetails getVsp(String vspId, Version version, String user);

  Version callAutoHeal(String vspId, VersionInfo versionInfo,
                       VspDetails vendorSoftwareProductInfo, String user) throws Exception;

  void deleteVsp(String vspIdToDelete, String user);

  QuestionnaireResponse getVspQuestionnaire(String vspId, Version version, String user);

  void updateVspQuestionnaire(String vspId, Version version, String questionnaireData, String user);


  byte[] getOrchestrationTemplateFile(String vspId, Version version, String user);

  PackageInfo createPackage(String vspId, Version version, String user) throws IOException;

  List<PackageInfo> listPackages(String category, String subCategory);

  File getTranslatedFile(String vspId, Version version, String user);

  void heal(String vspId, Version version, String user);

  File getInformationArtifact(String vspId, Version version, String user);


  String fetchValidationVsp(String user);
}

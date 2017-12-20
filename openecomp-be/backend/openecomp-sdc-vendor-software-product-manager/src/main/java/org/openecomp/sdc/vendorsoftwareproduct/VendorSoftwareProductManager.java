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

import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface VendorSoftwareProductManager {

  VspDetails createVsp(VspDetails vspDetails);

  void updateVsp(VspDetails vspDetails);

  VspDetails getVsp(String vspId, Version version);

  void deleteVsp(String vspIdToDelete);

  ValidationResponse validate(String vspId, Version version) throws IOException;

  Map<String, List<ErrorMessage>> compile(String vspId, Version version);


  QuestionnaireResponse getVspQuestionnaire(String vspId, Version version);

  void updateVspQuestionnaire(String vspId, Version version, String questionnaireData);


  byte[] getOrchestrationTemplateFile(String vspId, Version version);

  OrchestrationTemplateEntity getOrchestrationTemplateInfo(String vspId, Version version);


  PackageInfo createPackage(String vspId, Version version) throws IOException;

  List<PackageInfo> listPackages(String category, String subCategory);


  File getTranslatedFile(String vspId, Version version);

  File getInformationArtifact(String vspId, Version version);


  Collection<ComputeEntity> getComputeByVsp(String vspId, Version version);
}

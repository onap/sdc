/*
 * Copyright © 2016-2018 European Support Limited
 *
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
 */
package org.openecomp.sdc.vendorsoftwareproduct;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateEntity;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.PackageInfo;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.VspDetails;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ValidationResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface VendorSoftwareProductManager {

    VspDetails createVsp(VspDetails vspDetails);

    void updateVsp(VspDetails vspDetails);

    VspDetails getVsp(String vspId, Version version);

    void deleteVsp(String vspId, Version version);

    ValidationResponse validate(VspDetails vspDetails) throws IOException;

    Map<String, List<ErrorMessage>> compile(String vspId, Version version);

    QuestionnaireResponse getVspQuestionnaire(String vspId, Version version);

    void updateVspQuestionnaire(String vspId, Version version, String questionnaireData);

    byte[] getOrchestrationTemplateFile(String vspId, Version version);

    OrchestrationTemplateEntity getOrchestrationTemplateInfo(String vspId, Version version);

    Optional<FilesDataStructure> getOrchestrationTemplateStructure(String vspId, Version version);

    PackageInfo createPackage(String vspId, Version version) throws IOException;

    void updatePackage(PackageInfo packageInfo);

    List<PackageInfo> listPackages(String category, String subCategory);

    File getTranslatedFile(String vspId, Version version);

    File getInformationArtifact(String vspId, Version version);

    public Optional<Pair<String, byte[]>> get(String vspId, Version version) throws IOException;

    Collection<ComputeEntity> getComputeByVsp(String vspId, Version version);
}

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
package org.openecomp.sdc.vendorsoftwareproduct.dao;

import java.util.Optional;
import org.openecomp.sdc.heat.datatypes.structure.ValidationStructureList;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.OrchestrationTemplateCandidateData;
import org.openecomp.sdc.vendorsoftwareproduct.types.candidateheat.FilesDataStructure;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface OrchestrationTemplateCandidateDao extends VersionableDao {

    Optional<OrchestrationTemplateCandidateData> get(String vspId, Version version);

    Optional<OrchestrationTemplateCandidateData> getInfo(String vspId, Version version);

    void delete(String vspId, Version version);

    void update(String vspId, Version version, OrchestrationTemplateCandidateData candidateData);

    void updateStructure(String vspId, Version version, FilesDataStructure fileDataStructure);

    Optional<String> getStructure(String vspId, Version version);

    void updateValidationData(String vspId, Version version, ValidationStructureList validationData);
}

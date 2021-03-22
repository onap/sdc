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
package org.openecomp.sdc.vendorsoftwareproduct;

import java.util.Collection;
import org.openecomp.sdc.vendorsoftwareproduct.dao.type.ComputeEntity;
import org.openecomp.sdc.vendorsoftwareproduct.types.CompositionEntityResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.ListComputeResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.QuestionnaireResponse;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.CompositionEntityValidationData;
import org.openecomp.sdc.vendorsoftwareproduct.types.composition.ComputeData;
import org.openecomp.sdc.versioning.dao.types.Version;

public interface ComputeManager {

    Collection<ListComputeResponse> listComputes(String vspId, Version version, String componentId);

    ComputeEntity createCompute(ComputeEntity compute);

    CompositionEntityResponse<ComputeData> getCompute(String vspId, Version version, String componentId, String computeFlavorId);

    QuestionnaireResponse getComputeQuestionnaire(String vspId, Version version, String componentId, String computeFlavorId);

    void updateComputeQuestionnaire(String vspId, Version version, String componentId, String computeId, String questionnaireData);

    CompositionEntityValidationData updateCompute(ComputeEntity compute);

    void deleteCompute(String vspId, Version version, String componentId, String computeFlavorId);
}

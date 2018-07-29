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

package org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.impl;

import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.UnifiedSubstitutionNodeTemplateIdGenerator;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.to.UnifiedCompositionTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;

import java.util.Objects;
import java.util.Optional;

import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getConnectedComputeConsolidationData;
import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getNewPortNodeTemplateId;

public class PortNewNodeTemplateIdGenerator implements UnifiedSubstitutionNodeTemplateIdGenerator {

  @Override
  public Optional<String> generate(UnifiedCompositionTo unifiedCompositionTo, String originalNodeTemplateId) {
    ComputeTemplateConsolidationData connectedComputeConsolidationData =
        getConnectedComputeConsolidationData(unifiedCompositionTo.getUnifiedCompositionDataList(),
            originalNodeTemplateId);
    if (Objects.nonNull(connectedComputeConsolidationData)) {
      NodeTemplate connectedComputeNodeTemplate = DataModelUtil.getNodeTemplate(unifiedCompositionTo
              .getServiceTemplate(), connectedComputeConsolidationData.getNodeTemplateId());
      return Optional.of(getNewPortNodeTemplateId(originalNodeTemplateId, connectedComputeNodeTemplate.getType(),
          connectedComputeConsolidationData));
    }
    return Optional.empty();
  }
}

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
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.commands.UnifiedSubstitutionNodeTemplateIdGenerator;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.to.UnifiedCompositionTo;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.ComputeTemplateConsolidationData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.SubInterfaceTemplateConsolidationData;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getConnectedComputeConsolidationData;
import static org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionUtil.getNewSubInterfaceNodeTemplateId;

public class SubInterfaceNewNodeTemplateIdGenerator implements UnifiedSubstitutionNodeTemplateIdGenerator {

  @Override
  public Optional<String> generate(UnifiedCompositionTo unifiedCompositionTo, String originalNodeTemplateId) {
    SubInterfaceTemplateConsolidationData subInterfaceTemplateConsolidationData =
        getSubInterfaceTemplateConsolidationDataById(unifiedCompositionTo.getUnifiedCompositionDataList(),
            originalNodeTemplateId);
    if (Objects.nonNull(subInterfaceTemplateConsolidationData)) {
      String parentPortNodeTemplateId = subInterfaceTemplateConsolidationData.getParentPortNodeTemplateId();
      ComputeTemplateConsolidationData connectedComputeConsolidationData =
          getConnectedComputeConsolidationData(unifiedCompositionTo.getUnifiedCompositionDataList(),
              parentPortNodeTemplateId);
      if (Objects.nonNull(connectedComputeConsolidationData)) {
        NodeTemplate connectedComputeNodeTemplate = DataModelUtil.getNodeTemplate(unifiedCompositionTo
            .getServiceTemplate(), connectedComputeConsolidationData.getNodeTemplateId());
        return Optional.of(getNewSubInterfaceNodeTemplateId(unifiedCompositionTo.getServiceTemplate(),
            connectedComputeNodeTemplate.getType(), connectedComputeConsolidationData,
            subInterfaceTemplateConsolidationData, unifiedCompositionTo.getContext()));
      }
    }
    return Optional.empty();
  }

  private static SubInterfaceTemplateConsolidationData getSubInterfaceTemplateConsolidationDataById(
      List<UnifiedCompositionData> unifiedCompositionDataList,
      String subInterfaceNodeTemplateId) {
    return unifiedCompositionDataList.stream()
        .map(UnifiedCompositionUtil::getSubInterfaceTemplateConsolidationDataList)
        .flatMap(Collection::stream)
        .filter(subInterfaceTemplateConsolidationData -> subInterfaceNodeTemplateId
            .equals(subInterfaceTemplateConsolidationData.getNodeTemplateId()))
        .findFirst().orElse(null);
  }
}

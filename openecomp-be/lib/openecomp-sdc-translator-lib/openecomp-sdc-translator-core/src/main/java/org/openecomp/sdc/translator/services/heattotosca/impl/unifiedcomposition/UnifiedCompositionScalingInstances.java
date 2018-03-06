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

package org.openecomp.sdc.translator.services.heattotosca.impl.unifiedcomposition;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedComposition;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionService;

import java.util.List;
import java.util.Optional;

public class UnifiedCompositionScalingInstances implements UnifiedComposition {

  private UnifiedCompositionService unifiedCompositionService = new UnifiedCompositionService();

  @Override
  public void createUnifiedComposition(ServiceTemplate serviceTemplate,
                                       ServiceTemplate nestedServiceTemplate,
                                       List<UnifiedCompositionData> unifiedCompositionDataList,
                                       TranslationContext context) {
    if (CollectionUtils.isEmpty(unifiedCompositionDataList)
        || context.isUnifiedHandledServiceTemplate(serviceTemplate)) {
      return;
    }

    unifiedCompositionService.handleComplexVfcType(serviceTemplate, context);

    String substitutionNodeTypeId =
        unifiedCompositionService.getSubstitutionNodeTypeId(serviceTemplate,
            unifiedCompositionDataList.get(0), null, context);
    Optional<ServiceTemplate> substitutionServiceTemplate =
        unifiedCompositionService.createUnifiedSubstitutionServiceTemplate(serviceTemplate,
            unifiedCompositionDataList, context, substitutionNodeTypeId, null);

    if (!substitutionServiceTemplate.isPresent()) {
      return;
    }

    String abstractSubstituteNodeTemplateId = unifiedCompositionService
        .createAbstractSubstituteNodeTemplate(serviceTemplate, substitutionServiceTemplate.get(),
            unifiedCompositionDataList, substitutionNodeTypeId, context, null);

    unifiedCompositionService.createVfcInstanceGroup(abstractSubstituteNodeTemplateId,
        serviceTemplate, unifiedCompositionDataList);

    unifiedCompositionService
        .updateCompositionConnectivity(serviceTemplate, unifiedCompositionDataList, context);

    unifiedCompositionService
        .cleanUnifiedCompositionEntities(serviceTemplate, unifiedCompositionDataList, context);

    unifiedCompositionService.cleanNodeTypes(serviceTemplate, unifiedCompositionDataList, context);

    unifiedCompositionService.updateSubstitutionNodeTypePrefix(substitutionServiceTemplate.get());
  }
}

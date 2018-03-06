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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * The type Unified composition single substitution.
 */
public class UnifiedCompositionSingleSubstitution implements UnifiedComposition {

  private UnifiedCompositionService unifiedCompositionService = new UnifiedCompositionService();

  // There is no consolidation in SingleSubstitution implementation.
  // In case of single substitution, if there is more than one entry in the
  // unifiedCompositionDataList, they all should contain the same compute type but the
  // consolidation between them was canceled.
  // For different compute type, this implementation will be called more than once, each time
  // per diff compute type, while sending one entry in the unifiedCompositionDataList.
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

    for (int i = 0; i < unifiedCompositionDataList.size(); i++) {
      List<UnifiedCompositionData> singleSubstitutionUnifiedList = new ArrayList<>();
      singleSubstitutionUnifiedList.add(unifiedCompositionDataList.get(i));

      String substitutionNodeTypeId =
          unifiedCompositionService.getSubstitutionNodeTypeId(serviceTemplate,
              singleSubstitutionUnifiedList.get(0), null, context);

      Optional<ServiceTemplate> substitutionServiceTemplate =
          unifiedCompositionService.createUnifiedSubstitutionServiceTemplate(serviceTemplate,
              singleSubstitutionUnifiedList, context, substitutionNodeTypeId, null);

      if (!substitutionServiceTemplate.isPresent()) {
        continue;
      }

      String abstractSubstituteNodeTemplateId = unifiedCompositionService
          .createAbstractSubstituteNodeTemplate(serviceTemplate, substitutionServiceTemplate.get(),
              singleSubstitutionUnifiedList, substitutionNodeTypeId, context, null);

      unifiedCompositionService.createVfcInstanceGroup(abstractSubstituteNodeTemplateId,
          serviceTemplate, singleSubstitutionUnifiedList);

      unifiedCompositionService
          .updateCompositionConnectivity(serviceTemplate, singleSubstitutionUnifiedList, context);

      unifiedCompositionService
          .cleanUnifiedCompositionEntities(serviceTemplate, singleSubstitutionUnifiedList, context);

      unifiedCompositionService.updateSubstitutionNodeTypePrefix(substitutionServiceTemplate.get());
    }

    unifiedCompositionService
        .cleanNodeTypes(serviceTemplate, unifiedCompositionDataList, context);

  }
}

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

import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedComposition;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnifiedCompositionCatalogInstance implements UnifiedComposition {

  // There is consolidation in ScalingInstance implementation.
  // In case of scaling instance, if there is more than one entry in the
  // unifiedCompositionDataList, we should have consolidation between them.
  // (all entries in the list are the once which need to be consolidated)
  @Override
  public void createUnifiedComposition(ServiceTemplate serviceTemplate,
                                       ServiceTemplate nestedServiceTemplate,
                                       List<UnifiedCompositionData> unifiedCompositionDataList,
                                       TranslationContext context) {

    UnifiedCompositionService unifiedCompositionService = new UnifiedCompositionService();
    unifiedCompositionService.handleComplexVfcType(serviceTemplate, context);

    String substitutionNodeTypeId =
        unifiedCompositionService.getSubstitutionNodeTypeId(serviceTemplate,
            unifiedCompositionDataList.get(0), null, context);
    // create one substitution ST for all computes
    Optional<ServiceTemplate> substitutionServiceTemplate =
        unifiedCompositionService.createUnifiedSubstitutionServiceTemplate(serviceTemplate,
            unifiedCompositionDataList, context, substitutionNodeTypeId, null);

    if (!substitutionServiceTemplate.isPresent()) {
      return;
    }


    // create abstract NT for each compute
    for (int i = 0; i < unifiedCompositionDataList.size(); i++) {
      List<UnifiedCompositionData> catalogInstanceUnifiedList = new ArrayList<>();
      catalogInstanceUnifiedList.add(unifiedCompositionDataList.get(i));

      Integer index = unifiedCompositionDataList.size() > 1 ? i : null;

      String abstractSubstituteNodeTemplateId = unifiedCompositionService
          .createAbstractSubstituteNodeTemplate(serviceTemplate, substitutionServiceTemplate.get(),
              catalogInstanceUnifiedList, substitutionNodeTypeId, context, index);

      unifiedCompositionService.createVfcInstanceGroup(abstractSubstituteNodeTemplateId,
          serviceTemplate, catalogInstanceUnifiedList);

      unifiedCompositionService
          .updateCompositionConnectivity(serviceTemplate, catalogInstanceUnifiedList, context);

      unifiedCompositionService
          .cleanUnifiedCompositionEntities(serviceTemplate, catalogInstanceUnifiedList, context);
    }

    unifiedCompositionService.cleanNodeTypes(serviceTemplate, unifiedCompositionDataList, context);
    unifiedCompositionService.updateSubstitutionNodeTypePrefix(substitutionServiceTemplate.get());
  }
}

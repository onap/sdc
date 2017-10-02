package org.openecomp.sdc.translator.services.heattotosca.impl.unifiedcomposition;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedComposition;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionService;

import java.util.ArrayList;
import java.util.List;

public class UnifiedCompositionNestedSingleCompute implements UnifiedComposition {

  // There is no consolidation in NestedSingleCompute implemetation.
  // In case of  nested single compute, if there is more than one entry in the
  // unifiedComposotionDataList, each one should be handed seperatly, no consolidation between
  // them.
  @Override
  public void createUnifiedComposition(ServiceTemplate serviceTemplate,
                                       ServiceTemplate nestedServiceTemplate,
                                       List<UnifiedCompositionData> unifiedCompositionDataList,
                                       TranslationContext context) {
    UnifiedCompositionService unifiedCompositionService = new UnifiedCompositionService();
    if (CollectionUtils.isEmpty(unifiedCompositionDataList)) {
      return;
    }

    for (int i = 0; i < unifiedCompositionDataList.size(); i++) {
      List<UnifiedCompositionData> nestedUnifiedCompositionDataList = new ArrayList<>();
      nestedUnifiedCompositionDataList.add(unifiedCompositionDataList.get(i));

      unifiedCompositionService
          .handleUnifiedNestedDefinition(serviceTemplate, nestedServiceTemplate,
              unifiedCompositionDataList.get(i), context);
      unifiedCompositionService
          .updateUnifiedNestedConnectivity(serviceTemplate, nestedServiceTemplate,
              unifiedCompositionDataList.get(i), context);
      unifiedCompositionService
          .cleanUnifiedNestedEntities(serviceTemplate, unifiedCompositionDataList.get(i), context);
      unifiedCompositionService.updateSubstitutionNodeTypePrefix(nestedServiceTemplate);
    }
  }
}

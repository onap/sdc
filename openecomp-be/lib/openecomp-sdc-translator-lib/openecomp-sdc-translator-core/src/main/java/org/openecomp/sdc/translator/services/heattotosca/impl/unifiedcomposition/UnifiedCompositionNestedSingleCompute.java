package org.openecomp.sdc.translator.services.heattotosca.impl.unifiedcomposition;

import org.apache.commons.collections4.CollectionUtils;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.UnifiedCompositionData;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.composition.to.UnifiedCompositionTo;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedComposition;
import org.openecomp.sdc.translator.services.heattotosca.UnifiedCompositionService;

import java.util.List;

public class UnifiedCompositionNestedSingleCompute implements UnifiedComposition {

  // There is no consolidation in NestedSingleCompute implementation.
  // In case of  nested single compute, if there is more than one entry in the
  // unifiedCompositionDataList, each one should be handed separately, no consolidation between
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

    UnifiedCompositionTo unifiedCompositionTo = new UnifiedCompositionTo(serviceTemplate, nestedServiceTemplate,
        unifiedCompositionDataList, context);
    for (UnifiedCompositionData unifiedCompositionData : unifiedCompositionDataList) {
      unifiedCompositionService.handleUnifiedNestedDefinition(unifiedCompositionTo, unifiedCompositionData);
      String nestedNodeTemplateId = unifiedCompositionData.getNestedTemplateConsolidationData().getNodeTemplateId();
      unifiedCompositionService
          .createNestedVfcInstanceGroup(nestedNodeTemplateId, unifiedCompositionTo, unifiedCompositionData);
      unifiedCompositionService.updateUnifiedNestedConnectivity(unifiedCompositionTo, unifiedCompositionData);
      unifiedCompositionService.cleanUnifiedNestedEntities(unifiedCompositionTo, unifiedCompositionData);
      unifiedCompositionService.updateSubstitutionNodeTypePrefix(nestedServiceTemplate);
    }
  }
}

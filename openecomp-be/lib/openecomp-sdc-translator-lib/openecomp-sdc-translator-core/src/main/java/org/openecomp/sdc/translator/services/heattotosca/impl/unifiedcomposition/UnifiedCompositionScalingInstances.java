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

public class UnifiedCompositionScalingInstances implements UnifiedComposition {

  private UnifiedCompositionService unifiedCompositionService = new UnifiedCompositionService();

  @Override
  public void createUnifiedComposition(ServiceTemplate serviceTemplate,
                                       ServiceTemplate nestedServiceTemplate,
                                       List<UnifiedCompositionData> unifiedCompositionDataList,
                                       TranslationContext context) {
    if (CollectionUtils.isEmpty(unifiedCompositionDataList)) {
      return;
    }

    Integer index = null;
    Optional<ServiceTemplate> substitutionServiceTemplate =
        unifiedCompositionService.createUnifiedSubstitutionServiceTemplate(serviceTemplate,
            unifiedCompositionDataList, context, index);

    if (!substitutionServiceTemplate.isPresent()) {
      return;
    }

    String abstractNodeTemplateId = unifiedCompositionService
        .createAbstractSubstituteNodeTemplate(serviceTemplate, substitutionServiceTemplate.get(),
            unifiedCompositionDataList, context, index);

    unifiedCompositionService
        .updateCompositionConnectivity(serviceTemplate, unifiedCompositionDataList, context);

    unifiedCompositionService
        .cleanUnifiedCompositionEntities(serviceTemplate, unifiedCompositionDataList, context);

    unifiedCompositionService.cleanNodeTypes(serviceTemplate, unifiedCompositionDataList, context);
  }
}

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

  UnifiedCompositionService unifiedCompositionService = new UnifiedCompositionService();

  // There is consolidation in ScalingInstance implemetation.
  // In case of scaling instance, if there is more than one entry in the
  // unifiedComposotionDataList, we should have consolidation between them.
  // (all entries in the list are the once which need to be consolidated)
  @Override
  public void createUnifiedComposition(ServiceTemplate serviceTemplate,
                                       ServiceTemplate nestedServiceTemplate,
                                       List<UnifiedCompositionData> unifiedComposotionDataList,
                                       TranslationContext context) {

    UnifiedCompositionService unifiedCompositionService = new UnifiedCompositionService();
    unifiedCompositionService.handleComplexVfcType(serviceTemplate, context);

    UnifiedCompositionSingleSubstitution unifiedCompositionSingleSubstitution =
        new UnifiedCompositionSingleSubstitution();

    String substitutionNodeTypeId =
        unifiedCompositionService.getSubstitutionNodeTypeId(serviceTemplate,
            unifiedComposotionDataList.get(0), null, context);
    // create one substitution ST for all computes
    Optional<ServiceTemplate> substitutionServiceTemplate =
        unifiedCompositionService.createUnifiedSubstitutionServiceTemplate(serviceTemplate,
            unifiedComposotionDataList, context, substitutionNodeTypeId, null);

    if (!substitutionServiceTemplate.isPresent()) {
      return;
    }


    // create abstract NT for each compute
    for(int i = 0; i < unifiedComposotionDataList.size(); i++){
      List<UnifiedCompositionData> catalogInstanceUnifiedList = new ArrayList<>();
      catalogInstanceUnifiedList.add(unifiedComposotionDataList.get(i));

      Integer index = unifiedComposotionDataList.size() > 1 ? i : null;

      unifiedCompositionService
          .createAbstractSubstituteNodeTemplate(serviceTemplate, substitutionServiceTemplate.get(),
              catalogInstanceUnifiedList, substitutionNodeTypeId, context, index);

      unifiedCompositionService
          .updateCompositionConnectivity(serviceTemplate, catalogInstanceUnifiedList, context);

      unifiedCompositionService
          .cleanUnifiedCompositionEntities(serviceTemplate, catalogInstanceUnifiedList, context);
    }

    unifiedCompositionService.cleanNodeTypes(serviceTemplate, unifiedComposotionDataList, context);
  }
}

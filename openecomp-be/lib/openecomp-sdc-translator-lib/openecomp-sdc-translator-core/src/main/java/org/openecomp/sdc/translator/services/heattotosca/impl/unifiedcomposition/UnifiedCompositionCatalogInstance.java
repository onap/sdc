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
  @Override
  public void createUnifiedComposition(ServiceTemplate serviceTemplate,
                                       ServiceTemplate nestedServiceTemplate,
                                       List<UnifiedCompositionData> unifiedComposotionDataList,
                                       TranslationContext context) {

    UnifiedCompositionSingleSubstitution unifiedCompositionSingleSubstitution =
        new UnifiedCompositionSingleSubstitution();

    unifiedCompositionSingleSubstitution
        .createUnifiedComposition(serviceTemplate, null, unifiedComposotionDataList, context);
  }


}

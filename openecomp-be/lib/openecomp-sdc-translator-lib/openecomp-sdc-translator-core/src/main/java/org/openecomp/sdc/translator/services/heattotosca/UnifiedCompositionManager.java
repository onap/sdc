package org.openecomp.sdc.translator.services.heattotosca;

import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileNestedConsolidationData;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class UnifiedCompositionManager {

  private MdcDataDebugMessage mdcDataDebugMessage = new MdcDataDebugMessage();
  private ConsolidationService consolidationService;
  private TranslationService translationService = new TranslationService();
  private UnifiedCompositionService unifiedCompositionService = new UnifiedCompositionService();

  public UnifiedCompositionManager(ConsolidationService consolidationService) {
    this.consolidationService = consolidationService;
  }

  public UnifiedCompositionManager() {

  }

  /**
   * Create unified composition.
   *
   * @param toscaServiceModel  the tosca service model
   * @param translationContext the translation context
   * @return the tosca service model
   */
  public ToscaServiceModel createUnifiedComposition(ToscaServiceModel toscaServiceModel,
                                                    TranslationContext translationContext) {

    mdcDataDebugMessage.debugEntryMessage(null, null);
    Map<String, ServiceTemplate> serviceTemplates = toscaServiceModel.getServiceTemplates();
    ServiceTemplate mainServiceTemplate =
        serviceTemplates.get(toscaServiceModel.getEntryDefinitionServiceTemplate());
    consolidationService.mainServiceTemplateConsolidation(mainServiceTemplate, translationContext);
    ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();


    FileNestedConsolidationData mainFileNestedConsolidationData =
        translationContext.getConsolidationData().getNestedConsolidationData()
            .getFileNestedConsolidationData(toscaServiceModel.getEntryDefinitionServiceTemplate());
    Set<String> substituteNodeTemplatesId =
        mainFileNestedConsolidationData == null
            || mainFileNestedConsolidationData.getAllNestedNodeTemplateIds() == null
            ? new HashSet<>() : mainFileNestedConsolidationData.getAllNestedNodeTemplateIds();

    for (String substituteNodeTemplateId : substituteNodeTemplatesId) {
      NodeTemplate subNodeTemplate = mainServiceTemplate.getTopology_template().getNode_templates()
          .get(substituteNodeTemplateId);
      Optional<String> substituteServiceTemplateName = toscaAnalyzerService
          .getSubstituteServiceTemplateName(substituteNodeTemplateId, subNodeTemplate);
      if (substituteServiceTemplateName.isPresent()) {
        ServiceTemplate substituteServiceTemplate =
            serviceTemplates.get(substituteServiceTemplateName.get());

        consolidationService.substitutionServiceTemplateConsolidation(substituteNodeTemplateId,
                mainServiceTemplate, substituteServiceTemplate, translationContext);
      }
    }
    unifiedCompositionService
        .updateUnifiedAbstractNodesConnectivity(mainServiceTemplate, translationContext);
    ToscaServiceModel unifiedToscaServiceModel =
        HeatToToscaUtil.createToscaServiceModel(mainServiceTemplate, translationContext);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return unifiedToscaServiceModel;
  }
}


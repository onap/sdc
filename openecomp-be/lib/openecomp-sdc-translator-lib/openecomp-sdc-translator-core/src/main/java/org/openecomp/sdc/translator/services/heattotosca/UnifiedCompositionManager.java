package org.openecomp.sdc.translator.services.heattotosca;

import org.openecomp.sdc.logging.context.impl.MdcDataDebugMessage;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileNestedConsolidationData;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
                                                    TranslationContext translationContext)
      throws IOException {

    mdcDataDebugMessage.debugEntryMessage(null, null);
    Map<String, ServiceTemplate> serviceTemplates = toscaServiceModel.getServiceTemplates();
    ServiceTemplate mainServiceTemplate =
        serviceTemplates.get(toscaServiceModel.getEntryDefinitionServiceTemplate());
    createUnifiedComposition(toscaServiceModel, mainServiceTemplate, translationContext);
    ToscaServiceModel unifiedToscaServiceModel =
        HeatToToscaUtil.createToscaServiceModel(mainServiceTemplate, translationContext);

    mdcDataDebugMessage.debugExitMessage(null, null);
    return unifiedToscaServiceModel;
  }

  private void createUnifiedComposition(ToscaServiceModel toscaServiceModel,
                                        ServiceTemplate serviceTemplate,
                                        TranslationContext translationContext) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    handleNestedServiceTemplates(toscaServiceModel, serviceTemplate, translationContext);
    consolidationService.serviceTemplateConsolidation(serviceTemplate, translationContext);
    unifiedCompositionService
        .updateUnifiedAbstractNodesConnectivity(serviceTemplate, translationContext);
    translationContext.addUnifiedHandledServiceTeamplte(serviceTemplate);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void handleNestedServiceTemplates(ToscaServiceModel toscaServiceModel,
                                            ServiceTemplate serviceTemplate,
                                            TranslationContext translationContext) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
    FileNestedConsolidationData fileNestedConsolidationData =
        translationContext.getConsolidationData().getNestedConsolidationData()
            .getFileNestedConsolidationData(serviceTemplateFileName);

    if (Objects.nonNull(fileNestedConsolidationData)) {
      ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
      for (String substitutedNodeTemplateId : fileNestedConsolidationData
          .getAllNestedNodeTemplateIds()) {
        if (translationContext
            .isNestedNodeWasHandled(serviceTemplateFileName, substitutedNodeTemplateId)) {
          continue;
        }
        NodeTemplate nestedNodeTemplate =
            DataModelUtil.getNodeTemplate(serviceTemplate, substitutedNodeTemplateId);
        Optional<String> substituteServiceTemplateName =
            toscaAnalyzerService.getSubstituteServiceTemplateName(substitutedNodeTemplateId,
                nestedNodeTemplate);
        if (substituteServiceTemplateName.isPresent()) {
          ServiceTemplate substitutionServiceTemplate =
              toscaServiceModel.getServiceTemplates().get(substituteServiceTemplateName.get());
          createUnifiedCompositionForNestedServiceTemplate(toscaServiceModel, serviceTemplate,
              substitutionServiceTemplate, substitutedNodeTemplateId, translationContext);
        }
        translationContext.addNestedNodeAsHandled(serviceTemplateFileName,
            substitutedNodeTemplateId);
      }
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void createUnifiedCompositionForNestedServiceTemplate(
      ToscaServiceModel toscaServiceModel,
      ServiceTemplate serviceTemplate,
      ServiceTemplate substitutionServiceTemplate,
      String substitutedNodeTemplateId,
      TranslationContext translationContext) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    handleNestedServiceTemplates(toscaServiceModel, substitutionServiceTemplate,
        translationContext);
    consolidationService.substitutionServiceTemplateConsolidation(substitutedNodeTemplateId,
        serviceTemplate, substitutionServiceTemplate, translationContext);
    unifiedCompositionService
        .updateUnifiedAbstractNodesConnectivity(substitutionServiceTemplate, translationContext);
    translationContext.addUnifiedHandledServiceTeamplte(substitutionServiceTemplate);
    mdcDataDebugMessage.debugExitMessage(null, null);
  }
}


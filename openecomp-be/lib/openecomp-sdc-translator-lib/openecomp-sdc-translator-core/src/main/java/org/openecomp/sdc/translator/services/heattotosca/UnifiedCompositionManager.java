/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */
package org.openecomp.sdc.translator.services.heattotosca;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.onap.sdc.tosca.datatypes.model.NodeTemplate;
import org.onap.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.unifiedmodel.consolidation.FileNestedConsolidationData;

public class UnifiedCompositionManager {

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
    public ToscaServiceModel createUnifiedComposition(ToscaServiceModel toscaServiceModel, TranslationContext translationContext) {
        Map<String, ServiceTemplate> serviceTemplates = toscaServiceModel.getServiceTemplates();
        ServiceTemplate mainServiceTemplate = serviceTemplates.get(toscaServiceModel.getEntryDefinitionServiceTemplate());
        createUnifiedComposition(toscaServiceModel, mainServiceTemplate, translationContext);
        ToscaServiceModel unifiedToscaServiceModel = HeatToToscaUtil.createToscaServiceModel(mainServiceTemplate, translationContext);
        return unifiedToscaServiceModel;
    }

    private void createUnifiedComposition(ToscaServiceModel toscaServiceModel, ServiceTemplate serviceTemplate,
                                          TranslationContext translationContext) {
        handleNestedServiceTemplates(toscaServiceModel, serviceTemplate, translationContext);
        consolidationService.serviceTemplateConsolidation(serviceTemplate, translationContext);
        unifiedCompositionService.updateUnifiedAbstractNodesConnectivity(serviceTemplate, translationContext);
        translationContext.addUnifiedHandledServiceTeamplte(serviceTemplate);
    }

    private void handleNestedServiceTemplates(ToscaServiceModel toscaServiceModel, ServiceTemplate serviceTemplate,
                                              TranslationContext translationContext) {
        String serviceTemplateFileName = ToscaUtil.getServiceTemplateFileName(serviceTemplate);
        FileNestedConsolidationData fileNestedConsolidationData = translationContext.getConsolidationData().getNestedConsolidationData()
            .getFileNestedConsolidationData(serviceTemplateFileName);
        if (Objects.nonNull(fileNestedConsolidationData)) {
            ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
            for (String substitutedNodeTemplateId : fileNestedConsolidationData.getAllNestedNodeTemplateIds()) {
                NodeTemplate nestedNodeTemplate = DataModelUtil.getNodeTemplate(serviceTemplate, substitutedNodeTemplateId);
                if (Objects.isNull(nestedNodeTemplate) || translationContext
                    .isNestedNodeWasHandled(serviceTemplateFileName, substitutedNodeTemplateId)) {
                    continue;
                }
                Optional<String> substituteServiceTemplateName = toscaAnalyzerService
                    .getSubstituteServiceTemplateName(substitutedNodeTemplateId, nestedNodeTemplate);
                if (substituteServiceTemplateName.isPresent()) {
                    ServiceTemplate substitutionServiceTemplate = toscaServiceModel.getServiceTemplates().get(substituteServiceTemplateName.get());
                    createUnifiedCompositionForNestedServiceTemplate(toscaServiceModel, serviceTemplate, substitutionServiceTemplate,
                        substitutedNodeTemplateId, translationContext);
                }
                translationContext.addNestedNodeAsHandled(serviceTemplateFileName, substitutedNodeTemplateId);
            }
        }
    }

    private void createUnifiedCompositionForNestedServiceTemplate(ToscaServiceModel toscaServiceModel, ServiceTemplate serviceTemplate,
                                                                  ServiceTemplate substitutionServiceTemplate, String substitutedNodeTemplateId,
                                                                  TranslationContext translationContext) {
        handleNestedServiceTemplates(toscaServiceModel, substitutionServiceTemplate, translationContext);
        consolidationService
            .substitutionServiceTemplateConsolidation(substitutedNodeTemplateId, serviceTemplate, substitutionServiceTemplate, translationContext);
        unifiedCompositionService.updateUnifiedAbstractNodesConnectivity(substitutionServiceTemplate, translationContext);
        translationContext.addUnifiedHandledServiceTeamplte(substitutionServiceTemplate);
    }
}

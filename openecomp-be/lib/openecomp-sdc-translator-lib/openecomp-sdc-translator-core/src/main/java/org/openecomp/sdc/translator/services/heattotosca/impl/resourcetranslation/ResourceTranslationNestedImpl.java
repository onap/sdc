/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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

package org.openecomp.sdc.translator.services.heattotosca.impl.resourcetranslation;

import org.openecomp.core.utilities.file.FileUtils;
import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.ConsolidationDataUtil;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.HeatToToscaUtil;
import org.openecomp.sdc.translator.services.heattotosca.TranslationService;

public class ResourceTranslationNestedImpl extends ResourceTranslationBase {

  protected static Logger logger =
      (Logger) LoggerFactory.getLogger(ResourceTranslationNestedImpl.class);

  @Override
  public void translate(TranslateTo translateTo) {
    mdcDataDebugMessage.debugEntryMessage(null, null);

    TranslationContext context = translateTo.getContext();
    FileData nestedFileData =
        HeatToToscaUtil.getFileData(translateTo.getResource().getType(), context);
    if (nestedFileData == null) {
      logger.warn("Nested File '" + translateTo.getResource().getType()
          + "' is not exist, therefore, the nested resource with the ID '"
          + translateTo.getResourceId() + "' will be ignored in TOSCA translation");
      return;
    }
    String templateName = FileUtils.getFileWithoutExtention(translateTo.getResource().getType());
    String substitutionNodeTypeKey = ToscaNodeType.ABSTRACT_NODE_TYPE_PREFIX + "heat."
        + templateName;

    if (!context.getTranslatedServiceTemplates()
        .containsKey(translateTo.getResource().getType())) {

      //substitution service template
      ServiceTemplate nestedSubstitutionServiceTemplate =
          createSubstitutionServiceTemplate(translateTo, nestedFileData, templateName);

      //global substitution service template
      ServiceTemplate globalSubstitutionServiceTemplate = new HeatToToscaUtil()
          .fetchGlobalSubstitutionServiceTemplate(translateTo.getServiceTemplate(),
              context);

      //substitution node type
      NodeType substitutionNodeType = new ToscaAnalyzerServiceImpl()
          .createInitSubstitutionNodeType(nestedSubstitutionServiceTemplate,
              ToscaNodeType.ABSTRACT_SUBSTITUTE);
      DataModelUtil.addNodeType(globalSubstitutionServiceTemplate, substitutionNodeTypeKey,
          substitutionNodeType);
      //substitution mapping
      HeatToToscaUtil
          .handleSubstitutionMapping(context, substitutionNodeTypeKey,
              nestedSubstitutionServiceTemplate, substitutionNodeType);

      //add new nested service template
      context.getTranslatedServiceTemplates()
          .put(translateTo.getResource().getType(), nestedSubstitutionServiceTemplate);
    }

    ServiceTemplate substitutionServiceTemplate = context.getTranslatedServiceTemplates()
        .get(translateTo.getResource().getType());

    if(DataModelUtil.isNodeTemplateSectionMissingFromServiceTemplate(substitutionServiceTemplate)){
      handleSubstitutionServiceTemplateWithoutNodeTemplates(
          templateName, translateTo.getTranslatedId(), translateTo, context);
      mdcDataDebugMessage.debugExitMessage(null, null);
      return;
    }
    NodeTemplate substitutionNodeTemplate =
        HeatToToscaUtil.createAbstractSubstitutionNodeTemplate(translateTo, templateName,
            substitutionNodeTypeKey);
    manageSubstitutionNodeTemplateConnectionPoint(translateTo, nestedFileData,
        substitutionNodeTemplate, substitutionNodeTypeKey);
    DataModelUtil.addNodeTemplate(translateTo.getServiceTemplate(), translateTo.getTranslatedId(),
        substitutionNodeTemplate);

    //Add nested node template id to consolidation data
    ConsolidationDataUtil.updateNestedNodeTemplateId(translateTo);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void handleSubstitutionServiceTemplateWithoutNodeTemplates(String templateName,
                                                                     String translatedId,
                                                                     TranslateTo translateTo,
                                                                     TranslationContext context) {
    context.addServiceTemplateWithoutNodeTemplates(templateName);
    context.addNestedNodeTemplateIdPointsToStWithoutNodeTemplates(translatedId);
    context.getTranslatedServiceTemplates().remove(translateTo.getResource().getType());
  }

  private ServiceTemplate createSubstitutionServiceTemplate(TranslateTo translateTo,
                                                            FileData nestedFileData,
                                                            String templateName) {
    ServiceTemplate nestedSubstitutionServiceTemplate =
        HeatToToscaUtil.createInitSubstitutionServiceTemplate(templateName);
    translateTo.getContext()
        .addNestedHeatFileName(ToscaUtil.getServiceTemplateFileName(templateName),
            translateTo.getResource().getType());
    new TranslationService().translateHeatFile(nestedSubstitutionServiceTemplate, nestedFileData,
        translateTo.getContext());
    return nestedSubstitutionServiceTemplate;
  }


  private void manageSubstitutionNodeTemplateConnectionPoint(TranslateTo translateTo,
                                                             FileData nestedFileData,
                                                             NodeTemplate substitutionNodeTemplate,
                                                             String substitutionNodeTypeId) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ServiceTemplate globalSubstitutionTemplate =
        translateTo.getContext().getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    NodeType nodeType = globalSubstitutionTemplate.getNode_types().get(substitutionNodeTypeId);
    handlePortToNetConnections(translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
    handleSecurityRulesToPortConnections(translateTo, nestedFileData, substitutionNodeTemplate,
        nodeType);
    handleNovaToVolConnection(translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
    handleContrailV2VmInterfaceToNetworkConnection(translateTo, nestedFileData,
        substitutionNodeTemplate, nodeType);
    handleContrailPortToNetConnections(translateTo, nestedFileData, substitutionNodeTemplate,
        nodeType);
    handleVlanSubInterfaceToInterfaceConnections(translateTo, nestedFileData,
        substitutionNodeTemplate, nodeType);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void handleVlanSubInterfaceToInterfaceConnections(TranslateTo translateTo,
                                                            FileData nestedFileData,
                                                            NodeTemplate substitutionNodeTemplate,
                                                            NodeType nodeType) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ContrailV2VlanToInterfaceResourceConnection linker =
        new ContrailV2VlanToInterfaceResourceConnection(this, translateTo, nestedFileData,
            substitutionNodeTemplate, nodeType);
    linker.connect();

    mdcDataDebugMessage.debugExitMessage(null, null);
  }


  private void handleContrailV2VmInterfaceToNetworkConnection(TranslateTo translateTo,
                                                              FileData nestedFileData,
                                                              NodeTemplate substitutionNodeTemplate,
                                                              NodeType nodeType) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ContrailV2VmInterfaceToNetResourceConnection linker =
        new ContrailV2VmInterfaceToNetResourceConnection(this, translateTo, nestedFileData,
            substitutionNodeTemplate, nodeType);
    linker.connect();

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void handleNovaToVolConnection(TranslateTo translateTo, FileData nestedFileData,
                                         NodeTemplate substitutionNodeTemplate, NodeType nodeType) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    NovaToVolResourceConnection linker =
        new NovaToVolResourceConnection(this, translateTo, nestedFileData, substitutionNodeTemplate,
            nodeType);
    linker.connect();

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void handleSecurityRulesToPortConnections(TranslateTo translateTo,
                                                    FileData nestedFileData,
                                                    NodeTemplate substitutionNodeTemplate,
                                                    NodeType nodeType) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    SecurityRulesToPortResourceConnection linker =
        new SecurityRulesToPortResourceConnection(this, translateTo, nestedFileData,
            substitutionNodeTemplate, nodeType);
    linker.connect();

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void handlePortToNetConnections(TranslateTo translateTo, FileData nestedFileData,
                                          NodeTemplate substitutionNodeTemplate,
                                          NodeType nodeType) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    PortToNetResourceConnection linker =
        new PortToNetResourceConnection(this, translateTo, nestedFileData, substitutionNodeTemplate,
            nodeType);
    linker.connect();

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void handleContrailPortToNetConnections(TranslateTo translateTo, FileData nestedFileData,
                                                  NodeTemplate substitutionNodeTemplate,
                                                  NodeType nodeType) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ContrailPortToNetResourceConnection linker =
        new ContrailPortToNetResourceConnection(this, translateTo, nestedFileData,
            substitutionNodeTemplate, nodeType);
    linker.connect();

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

}

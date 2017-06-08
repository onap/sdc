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

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.datatypes.error.ErrorLevel;
import org.openecomp.sdc.logging.context.impl.MdcDataErrorMessage;
import org.openecomp.sdc.logging.types.LoggerConstants;
import org.openecomp.sdc.logging.types.LoggerErrorCode;
import org.openecomp.sdc.logging.types.LoggerErrorDescription;
import org.openecomp.sdc.logging.types.LoggerTragetServiceName;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.ToscaTopologyTemplateElements;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.TranslationContext;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.translator.services.heattotosca.helper.ContrailTranslationHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ResourceTranslationContrailServiceTemplateImpl extends ResourceTranslationBase {

  static String getContrailSubstitutedNodeTypeId(String serviceTemplateTranslatedId) {
    return ToscaNodeType.ABSTRACT_NODE_TYPE_PREFIX + "heat." + serviceTemplateTranslatedId;
  }

  @Override
  public void translate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ServiceTemplate globalSubstitutionServiceTemplate =
        getGlobalSubstitutionTypesServiceTemplate(translateTo);
    addSubstitutedNodeType(translateTo, globalSubstitutionServiceTemplate);
    addComputeNodeType(translateTo, globalSubstitutionServiceTemplate, translateTo.getContext());
    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  @Override
  protected boolean isEssentialRequirementsValid(TranslateTo translateTo) {
    Map<String, Object> properties = translateTo.getResource().getProperties();
    if (Objects.isNull(properties) || Objects.isNull(properties.get("image_name"))) {
      MdcDataErrorMessage.createErrorMessageAndUpdateMdc(LoggerConstants.TARGET_ENTITY_DB,
          LoggerTragetServiceName.GENERATE_TRANSLATED_ID, ErrorLevel.ERROR.name(),
          LoggerErrorCode.DATA_ERROR.getErrorCode(),
          LoggerErrorDescription.MISSING_MANDATORY_PROPERTY);
      throw new CoreException(new MissingMandatoryPropertyErrorBuilder("image_name").build());
    }
    return true;
  }

  private void addComputeNodeType(TranslateTo translateTo,
                                  ServiceTemplate globalSubstitutionServiceTemplate,
                                  TranslationContext context) {
    mdcDataDebugMessage.debugEntryMessage(null, null);
    NodeType computeNodeType = new NodeType();
    computeNodeType.setDerived_from(ToscaNodeType.CONTRAIL_COMPUTE);
    String computeNodeTypeId = new ContrailTranslationHelper()
        .getComputeNodeTypeId(translateTo.getResource(), translateTo.getResourceId(),
            translateTo.getTranslatedId(), context);
    DataModelUtil
        .addNodeType(globalSubstitutionServiceTemplate, computeNodeTypeId, computeNodeType);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  private void addSubstitutedNodeType(TranslateTo translateTo,
                                      ServiceTemplate globalSubstitutionServiceTemplate) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    NodeType substitutedNodeType = new NodeType();
    substitutedNodeType
        .setDerived_from(ToscaNodeType.CONTRAIL_ABSTRACT_SUBSTITUTE);
    DataModelUtil.addNodeType(globalSubstitutionServiceTemplate,
        getContrailSubstitutedNodeTypeId(translateTo.getTranslatedId()), substitutedNodeType);

    mdcDataDebugMessage.debugExitMessage(null, null);
  }

  @Override
  protected Optional<ToscaTopologyTemplateElements> getTranslatedToscaTopologyElement(
      TranslateTo translateTo) {
    return Optional.empty();
  }

  private ServiceTemplate getGlobalSubstitutionTypesServiceTemplate(TranslateTo translateTo) {


    mdcDataDebugMessage.debugEntryMessage(null, null);

    ServiceTemplate globalSubstitutionServiceTemplate =
        translateTo.getContext().getTranslatedServiceTemplates().get(
            Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    if (globalSubstitutionServiceTemplate == null) {
      globalSubstitutionServiceTemplate = new ServiceTemplate();
      Map<String, String> templateMetadata = new HashMap<>();
      templateMetadata.put(ToscaConstants.ST_METADATA_TEMPLATE_NAME, Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
      globalSubstitutionServiceTemplate.setMetadata(templateMetadata);
      globalSubstitutionServiceTemplate.setImports(GlobalTypesGenerator.getGlobalTypesImportList());
      globalSubstitutionServiceTemplate
          .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
      translateTo.getContext().getTranslatedServiceTemplates()
          .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
              globalSubstitutionServiceTemplate);
    }

    mdcDataDebugMessage.debugExitMessage(null, null);
    return globalSubstitutionServiceTemplate;
  }

}

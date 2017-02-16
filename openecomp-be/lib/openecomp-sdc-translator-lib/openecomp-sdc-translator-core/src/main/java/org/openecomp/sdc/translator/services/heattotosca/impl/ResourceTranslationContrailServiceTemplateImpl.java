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

package org.openecomp.sdc.translator.services.heattotosca.impl;

import org.openecomp.sdc.common.errors.CoreException;
import org.openecomp.sdc.tosca.datatypes.ToscaNodeType;
import org.openecomp.sdc.tosca.datatypes.model.Metadata;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.tosca.services.DataModelUtil;
import org.openecomp.sdc.tosca.services.ToscaConstants;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;
import org.openecomp.sdc.translator.services.heattotosca.Constants;
import org.openecomp.sdc.translator.services.heattotosca.errors.MissingMandatoryPropertyErrorBuilder;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.GlobalTypesGenerator;
import org.openecomp.sdc.translator.services.heattotosca.helper.ContrailTranslationHelper;

import java.util.Map;
import java.util.Objects;

public class ResourceTranslationContrailServiceTemplateImpl extends ResourceTranslationBase {

  static String getContrailSubstitutedNodeTypeId(String serviceTemplateTranslatedId) {
    return ToscaConstants.NODES_SUBSTITUTION_PREFIX + serviceTemplateTranslatedId;
  }

  @Override
  protected boolean isEssentialRequirementsValid(TranslateTo translateTo) {
    Map<String, Object> properties = translateTo.getResource().getProperties();
    if (Objects.isNull(properties) || Objects.isNull(properties.get("image_name"))) {
      throw new CoreException(new MissingMandatoryPropertyErrorBuilder("image_name").build());
    }
    return true;
  }

  @Override
  public void translate(TranslateTo translateTo) {

    ServiceTemplate globalSubstitutionServiceTemplate =
        getGlobalSubstitutionTypesServiceTemplate(translateTo);
    addSubstitutedNodeType(translateTo, globalSubstitutionServiceTemplate);
    addComputeNodeType(translateTo, globalSubstitutionServiceTemplate);
  }

  private void addComputeNodeType(TranslateTo translateTo,
                                  ServiceTemplate globalSubstitutionServiceTemplate) {
    NodeType computeNodeType = new NodeType();
    computeNodeType.setDerived_from(ToscaNodeType.CONTRAIL_COMPUTE.getDisplayName());
    String computeNodeTypeId = new ContrailTranslationHelper()
        .getComputeNodeTypeId(translateTo.getTranslatedId(), translateTo.getResource());
    DataModelUtil
        .addNodeType(globalSubstitutionServiceTemplate, computeNodeTypeId, computeNodeType);
  }

  private void addSubstitutedNodeType(TranslateTo translateTo,
                                      ServiceTemplate globalSubstitutionServiceTemplate) {
    NodeType substitutedNodeType = new NodeType();
    substitutedNodeType
        .setDerived_from(ToscaNodeType.CONTRAIL_ABSTRACT_SUBSTITUTE.getDisplayName());
    DataModelUtil.addNodeType(globalSubstitutionServiceTemplate,
        getContrailSubstitutedNodeTypeId(translateTo.getTranslatedId()), substitutedNodeType);
  }

  private ServiceTemplate getGlobalSubstitutionTypesServiceTemplate(TranslateTo translateTo) {
    ServiceTemplate globalSubstitutionServiceTemplate =
        translateTo.getContext().getTranslatedServiceTemplates()
            .get(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
    if (globalSubstitutionServiceTemplate == null) {
      globalSubstitutionServiceTemplate = new ServiceTemplate();
      Metadata templateMetadata = new Metadata();
      templateMetadata.setTemplate_name(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME);
      globalSubstitutionServiceTemplate.setMetadata(templateMetadata);
      globalSubstitutionServiceTemplate.setImports(GlobalTypesGenerator.getGlobalTypesImportList());
      globalSubstitutionServiceTemplate
          .setTosca_definitions_version(ToscaConstants.TOSCA_DEFINITIONS_VERSION);
      translateTo.getContext().getTranslatedServiceTemplates()
          .put(Constants.GLOBAL_SUBSTITUTION_TYPES_TEMPLATE_NAME,
              globalSubstitutionServiceTemplate);
    }
    return globalSubstitutionServiceTemplate;
  }
}

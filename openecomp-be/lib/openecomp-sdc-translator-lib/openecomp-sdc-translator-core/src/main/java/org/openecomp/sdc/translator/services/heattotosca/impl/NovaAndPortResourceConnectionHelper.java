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

import org.openecomp.sdc.heat.datatypes.manifest.FileData;
import org.openecomp.sdc.heat.datatypes.model.HeatOrchestrationTemplate;
import org.openecomp.sdc.heat.datatypes.model.Resource;
import org.openecomp.sdc.tosca.datatypes.model.NodeTemplate;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.datatypes.model.RequirementDefinition;
import org.openecomp.sdc.tosca.datatypes.model.ServiceTemplate;
import org.openecomp.sdc.translator.datatypes.heattotosca.to.TranslateTo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class NovaAndPortResourceConnectionHelper
    extends BaseResourceConnection<RequirementDefinition> {
  public NovaAndPortResourceConnectionHelper(ResourceTranslationBase resourceTranslationBase,
                                             TranslateTo translateTo, FileData nestedFileData,
                                             NodeTemplate substitutionNodeTemplate,
                                             NodeType nodeType) {
    super(resourceTranslationBase, translateTo, nestedFileData, substitutionNodeTemplate, nodeType);
  }

  @Override
  boolean isDesiredNodeTemplateType(NodeTemplate nodeTemplate) {
    return false;
  }

  @Override
  List<Predicate<RequirementDefinition>> getPredicatesListForConnectionPoints() {
    return null;
  }

  @Override
  Optional<List<String>> getConnectorParamName(String heatResourceId,
                                               Resource heatResource,
                                               HeatOrchestrationTemplate
                                                       nestedHeatOrchestrationTemplate) {
    return null;
  }

  @Override
  String getDesiredResourceType() {
    return null;
  }

  @Override
  String getTranslatedResourceIdFromSubstitutionMapping(
          ServiceTemplate nestedServiceTemplate,Map.Entry<String,
          RequirementDefinition> entry) {
    return null;
  }

  @Override
  void addRequirementToConnectResources(Map.Entry<String, RequirementDefinition> entry,
                                        List<String> paramNames) {

  }

  @Override
  List<Map<String, RequirementDefinition>> getAllConnectionPoints() {
    List<Map<String, RequirementDefinition>> exposedRequirementsList = new ArrayList<>();
    List<Predicate<RequirementDefinition>> predicates = getPredicatesListForConnectionPoints();
    List<Map<String, RequirementDefinition>> requirements = this.nodeType.getRequirements();
    if (requirements == null) {
      return exposedRequirementsList;
    }
    requirements.stream()
        .map(Map::entrySet)
        .forEach(x -> x.stream()
            .filter(entry -> predicates
                .stream()
                .anyMatch(p -> p.test(entry.getValue())))
            .forEach(entry -> {
              Map<String, RequirementDefinition> exposedRequirementsMap = new HashMap<>();
              exposedRequirementsMap.put(entry.getKey(), entry.getValue());
              exposedRequirementsList.add(exposedRequirementsMap);
            }));

    return exposedRequirementsList;
  }
}

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

package org.openecomp.sdc.enrichment.impl.tosca;

import org.apache.commons.collections4.MapUtils;
import org.openecomp.core.enrichment.types.CeilometerInfo;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.EnrichmentInfo;
import org.openecomp.sdc.tosca.datatypes.ToscaCapabilityType;
import org.openecomp.sdc.tosca.datatypes.ToscaElementTypes;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityDefinition;
import org.openecomp.sdc.tosca.datatypes.model.CapabilityType;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.services.ToscaAnalyzerService;
import org.openecomp.sdc.tosca.services.ToscaUtil;
import org.openecomp.sdc.tosca.services.impl.ToscaAnalyzerServiceImpl;
import org.openecomp.sdc.translator.services.heattotosca.globaltypes.CommonGlobalTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CeilometerEnricher {
  /**
   * Enrich map.
   *
   * @param toscaModel    the tosca model
   * @param modelNodeType the model node type
   * @param input         the input
   * @return the map
   */
  public static Map<String, List<ErrorMessage>> enrich(ToscaServiceModel toscaModel,
                                                       Map<String, List<NodeType>> modelNodeType,
                                                       EnrichmentInfo input) {
    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    input.getEntityInfo().entrySet().stream().forEach(
        entry -> enrichNodeType(toscaModel, (ComponentInfo) entry.getValue(),
            modelNodeType.get(entry.getKey())));

    return errors;
  }

  private static void enrichNodeType(ToscaServiceModel toscaModel, ComponentInfo componentInfo,
                                     List<NodeType> nodeTypes) {

    for (CeilometerInfo ceilometerInfo : componentInfo.getCeilometerInfo()
        .getCeilometerInfoList()) {
      String capabilityId = ceilometerInfo.getName();

      Map<String, Object> properties = getCeilometerProperties(ceilometerInfo);

      //CapabilityType capabilityType = CommonGlobalTypes.createServiceTemplate()
      // .getCapability_types().
      // get(ToscaCapabilityType.METRIC_CEILOMETER.getDisplayName());
      //CapabilityType metricCapabilityType = CommonGlobalTypes.createServiceTemplate().
      // getCapability_types().get(ToscaCapabilityType.METRIC.getDisplayName());
      ToscaAnalyzerService toscaAnalyzerService = new ToscaAnalyzerServiceImpl();
      CapabilityType capabilityType = (CapabilityType) toscaAnalyzerService
          .getFlatEntity(ToscaElementTypes.CAPABILITY_TYPE,
              ToscaCapabilityType.METRIC_CEILOMETER.getDisplayName(),
              CommonGlobalTypes.createServiceTemplate(), toscaModel);


      nodeTypes.stream().forEach(nodeType ->
          addCapability(nodeType, capabilityId, ToscaUtil
              .convertTypeToDefinition(ToscaCapabilityType.METRIC_CEILOMETER.getDisplayName(),
                  capabilityType, properties, ceilometerInfo.getDescription())));
    }
  }

  private static Map<String, Object> getCeilometerProperties(CeilometerInfo ceilometerInfo) {
    Map<String, Object> properties = new HashMap<>();
    properties.put("name", ceilometerInfo.getName());
    properties.put("type", ceilometerInfo.getType());
    properties.put("unit", ceilometerInfo.getUnit());
    if (ceilometerInfo.getCategory() != null) {
      properties.put("category", ceilometerInfo.getCategory());
    }
    return properties;
  }

  private static void addCapability(NodeType nodeType, String capabilityId,
                                    CapabilityDefinition capabilityDefinition) {
    if (MapUtils.isEmpty(nodeType.getCapabilities())) {
      nodeType.setCapabilities(new HashMap<>());
    }
    //clean unnecessary info
    capabilityDefinition.setAttributes(null);
    capabilityDefinition.setOccurrences(null);

    nodeType.getCapabilities().put(capabilityId, capabilityDefinition);
  }
}

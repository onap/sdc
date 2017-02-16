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

import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.inter.Enricher;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.tosca.datatypes.model.NodeType;
import org.openecomp.sdc.tosca.services.ToscaUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToscaEnricher extends Enricher {

  Map<String, List<NodeType>> componentTypNodeTypeMap;

  @Override
  public Map<String, List<ErrorMessage>> enrich() {
    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    Map<String, List<ErrorMessage>> enrichResponse;
    enrichResponse = enrichCilometer();
    errors.putAll(enrichResponse);
    enrichResponse = enrichSnmp();
    errors.putAll(enrichResponse);


    return errors;
  }

  private Map<String, List<ErrorMessage>> enrichCilometer() {
    Map<String, List<ErrorMessage>> enrichResponse;

    ToscaServiceModel toscaModel = (ToscaServiceModel) model;

    componentTypNodeTypeMap =
        ToscaUtil.normalizeComponentNameNodeType(toscaModel, input.getEntityInfo().keySet());

    enrichResponse = CeilometerEnricher.enrich(toscaModel, componentTypNodeTypeMap, this.input);

    return enrichResponse;
  }


  private Map<String, List<ErrorMessage>> enrichSnmp() {
    Map<String, List<ErrorMessage>> enrichResponse;

    enrichResponse = SnmpEnricher.enrich(componentTypNodeTypeMap, this.input);

    return enrichResponse;
  }


}

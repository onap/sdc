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

package org.openecomp.sdc.enrichment.impl;

import org.openecomp.core.enrichment.types.EntityInfo;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.datatypes.model.AsdcModel;
import org.openecomp.sdc.enrichment.EnrichmentInfo;
import org.openecomp.sdc.enrichment.impl.external.artifact.ExternalArtifactEnricher;
import org.openecomp.sdc.enrichment.impl.tosca.ToscaEnricher;
import org.openecomp.sdc.enrichment.inter.Enricher;
import org.openecomp.sdc.enrichment.inter.EnricherHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type Enricher handler.
 */
public class EnricherHandlerImpl implements EnricherHandler {

  private static Logger logger = LoggerFactory.getLogger(EnricherHandlerImpl.class);
  private EnrichmentInfo input;
  private AsdcModel model;

  @Override
  public List<Enricher> getEnrichers() {
    List<Enricher> enricherList = new ArrayList<>();
    enricherList.add(new ToscaEnricher());
    enricherList.add(new ExternalArtifactEnricher());
    return enricherList;
  }

  @Override
  public Map<String, List<ErrorMessage>> enrich() {
    Map<String, List<ErrorMessage>> errors = new HashMap<>();
    Map<String, List<ErrorMessage>> enricherResponse;
    for (Enricher enricher : getEnrichers()) {
      enricher.setInput(this.input);
      enricher.setModel(this.model);
      enricherResponse = enricher.enrich();
      errors.putAll(enricherResponse);
    }
    return errors;
  }

  /**
   * Adds additional input.
   *
   * @param key   key
   * @param input input
   */
  public void addAdditionalInput(String key, Object input) {
    if (!this.input.getAdditionalInfo().containsKey(key)) {
      this.input.getAdditionalInfo().put(key, new ArrayList<>());
    }
    this.input.getAdditionalInfo().get(key).add(input);
  }

  public void addEntityInfo(String entityId, EntityInfo entityInfo) {
    this.input.getEntityInfo().put(entityId, entityInfo);
  }

  public void setModel(AsdcModel model) {
    this.model = model;
  }
}

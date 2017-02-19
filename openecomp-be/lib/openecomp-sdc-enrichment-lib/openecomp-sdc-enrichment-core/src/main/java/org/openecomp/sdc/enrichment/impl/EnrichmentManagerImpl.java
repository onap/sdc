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

import org.openecomp.core.enrichment.api.EnrichmentManager;
import org.openecomp.core.enrichment.types.EntityInfo;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.enrichment.EnrichmentInfo;
import org.openecomp.sdc.enrichment.factory.EnricherHandlerFactory;
import org.openecomp.sdc.enrichment.inter.Enricher;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.versioning.dao.types.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class EnrichmentManagerImpl implements EnrichmentManager<ToscaServiceModel> {

  private static Logger logger = LoggerFactory.getLogger(EnrichmentManagerImpl.class);

  private EnrichmentInfo input = null;
  private ToscaServiceModel model;


  @Override
  public Map<String, List<ErrorMessage>> enrich() {
    List<Enricher> enricherList =
        EnricherHandlerFactory.getInstance().createInterface().getEnrichers();
    for (Enricher enricher : enricherList) {
      enricher.setInput(input);
      enricher.setModel(model);
      enricher.enrich();
    }

    return null;
  }

  @Override
  public void addEntityInput(String type, EntityInfo info) {
    this.input.addEntityInfo(type, info);
  }


  @Override
  public void initInput(String key, Version version) {
    input = new EnrichmentInfo();
    input.setKey(key);
    input.setVersion(version);
  }

  @Override
  public void addModel(ToscaServiceModel model) {

    this.model = model;
  }


  @Override
  public ToscaServiceModel getModel() {
    return this.model;
  }


}

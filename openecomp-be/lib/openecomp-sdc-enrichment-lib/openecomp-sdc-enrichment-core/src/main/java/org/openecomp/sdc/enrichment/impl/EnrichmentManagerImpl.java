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
import org.openecomp.sdc.logging.api.Logger;
import org.openecomp.sdc.logging.api.LoggerFactory;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrichmentManagerImpl implements EnrichmentManager<ToscaServiceModel> {

  private static Logger logger = (Logger) LoggerFactory.getLogger(EnrichmentManagerImpl.class);

  private EnrichmentInfo data = null;
  private ToscaServiceModel model;


  @Override
  public Map<String, List<ErrorMessage>> enrich() {
    Map<String, List<ErrorMessage>> enrichErrors = new HashMap<>();
    List<Enricher> enricherList =
        EnricherHandlerFactory.getInstance().createInterface().getEnrichers();
    for (Enricher enricher : enricherList) {
      enricher.setData(data);
      enricher.setModel(model);
      enrichErrors.putAll(enricher.enrich());
    }

    return enrichErrors;
  }

  @Override
  public void addEntityInfo(String entityKey, EntityInfo entityInfo) {
    this.data.addEntityInfo(entityKey, entityInfo);
  }


  @Override
  public void init(String key, Version version) {
    data = new EnrichmentInfo();
    data.setKey(key);
    data.setVersion(version);
  }

  @Override
  public ToscaServiceModel getModel() {
    return this.model;
  }

  @Override
  public void setModel(ToscaServiceModel model) {
    this.model = model;
  }


}

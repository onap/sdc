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

package org.openecomp.sdc.vendorsoftwareproduct.impl.mock;


import org.openecomp.core.enrichment.api.EnrichmentManager;
import org.openecomp.core.enrichment.factory.EnrichmentManagerFactory;
import org.openecomp.core.enrichment.types.EntityInfo;
import org.openecomp.sdc.datatypes.error.ErrorMessage;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.List;
import java.util.Map;


public class EnrichmentManagerFactoryImpl extends EnrichmentManagerFactory {

  @Override
  public EnrichmentManager createInterface() {
    return new EnrichmentManagerMock();
  }

  public class EnrichmentManagerMock implements EnrichmentManager {
    @Override
    public Map<String, List<ErrorMessage>> enrich() {
      return null;
    }

    @Override
    public void addEntityInfo(String entityKey, EntityInfo entityInfo) {

    }

    @Override
    public void init(String key, Version version) {

    }

    @Override
    public Object getModel() {
      return null;
    }

    @Override
    public void setModel(Object model) {

    }
  }
}

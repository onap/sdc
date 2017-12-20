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

package org.openecomp.sdc.model.impl;

import org.openecomp.core.model.dao.EnrichedServiceArtifactDaoFactory;
import org.openecomp.core.model.dao.EnrichedServiceModelDao;
import org.openecomp.core.model.dao.EnrichedServiceTemplateDaoFactory;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.versioning.dao.types.Version;

public class EnrichedServiceModelDaoImpl extends AbstractServiceModelDao
    implements EnrichedServiceModelDao<ToscaServiceModel, ServiceElement> {
  public EnrichedServiceModelDaoImpl() {
    templateDao = EnrichedServiceTemplateDaoFactory.getInstance().createInterface();
    artifactDao = EnrichedServiceArtifactDaoFactory.getInstance().createInterface();
  }

  /*@Override
  public List<ServiceArtifact> getExternalArtifacts(String vspId, Version version) {
    return (List<ServiceArtifact>) artifactDao.list(vspId, version);
  }
*/

  @Override
  public void deleteAll(String vspId, Version version) {
    templateDao.deleteAll(vspId, version);
    artifactDao.deleteAll(vspId, version);
  }
}

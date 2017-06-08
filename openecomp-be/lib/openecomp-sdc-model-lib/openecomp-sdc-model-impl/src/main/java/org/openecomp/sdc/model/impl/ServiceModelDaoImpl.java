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

import org.openecomp.core.model.dao.ServiceArtifactDaoFactory;
import org.openecomp.core.model.dao.ServiceModelDao;
import org.openecomp.core.model.dao.ServiceTemplateDaoFactory;
import org.openecomp.core.model.types.ServiceElement;
import org.openecomp.sdc.tosca.datatypes.ToscaServiceModel;
import org.openecomp.sdc.versioning.dao.types.Version;

public class ServiceModelDaoImpl extends AbstractServiceModelDao
    implements ServiceModelDao<ToscaServiceModel, ServiceElement> {

  public ServiceModelDaoImpl() {
    templateDao = ServiceTemplateDaoFactory.getInstance().createInterface();
    artifactDao = ServiceArtifactDaoFactory.getInstance().createInterface();
  }

  @Override
  public void deleteAll(String vspId, Version version) {

  }
}

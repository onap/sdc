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

package org.openecomp.core.model.dao;

import org.openecomp.core.model.types.ServiceTemplate;
import org.openecomp.sdc.versioning.dao.VersionableDao;
import org.openecomp.sdc.versioning.dao.types.Version;

import java.util.Collection;

public interface ServiceTemplateDaoInter extends VersionableDao {
  void create(ServiceTemplate entity);

  void update(ServiceTemplate entity);

  ServiceTemplate get(String vspId, Version version);

  void delete(String vspId, Version version);

  Object[] getKeys(String vspId, Version version);

  ServiceTemplate getTemplateInfo(String vspId, Version version, String name);

  Collection<ServiceTemplate> list(String vspId, Version version);

  String getBase(String vspId, Version version);

}
